/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.legacy.crypto;

import org.spongycastle.asn1.pkcs.PrivateKeyInfo;
import org.spongycastle.asn1.x509.SubjectPublicKeyInfo;
import org.spongycastle.crypto.BufferedBlockCipher;
import org.spongycastle.crypto.CipherParameters;
import org.spongycastle.crypto.Digest;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.digests.SHA512Digest;
import org.spongycastle.crypto.engines.AESEngine;
import org.spongycastle.crypto.io.DigestInputStream;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.openssl.PEMKeyPair;
import org.spongycastle.openssl.PEMParser;
import org.spongycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.spongycastle.util.io.pem.PemReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

@SuppressWarnings("WeakerAccess")
public class CryptoUtils {

    private static final Logger LOGGER = Logger.getLogger(CryptoUtils.class.getName());

    private static final String RSA = "RSA";
    private static final String DEFAULT_RSA_PADDING = "RSA/ECB/OAEPWithSHA1AndMGF1Padding";

    static {
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

    /**
     * RSA decrypt the given data using the default padding
     *
     * @param data       The data to decrypt
     * @param privateKey The PrivateKey to use for decryption
     * @return The decrypted bytes
     * @throws DGMCryptoFailureException the dgm crypto failure exception
     */
    static byte[] decryptRSA(byte[] data, PrivateKey privateKey) throws DGMCryptoFailureException {
        return decryptRSA(DEFAULT_RSA_PADDING, data, privateKey);
    }

    /**
     * RSA decrypt the given data using the default application key
     *
     * @param provider   the RSA provider
     * @param data       The data to decrypt
     * @param privateKey The PrivateKey to use for decryption
     * @return The decrypted bytes
     * @throws DGMCryptoFailureException the dgm crypto failure exception
     */
    static byte[] decryptRSA(@SuppressWarnings("SameParameterValue") final String provider, byte[] data, PrivateKey privateKey) throws DGMCryptoFailureException {
        if (!provider.startsWith(RSA)) {
            throw new IllegalArgumentException();
        }
        try {
            Cipher rsaCipher = Cipher.getInstance(provider, "SC");
            rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
            return rsaCipher.doFinal(data);
        } catch (Exception e) {
            FailureCause cause = FailureCause.RSA_DECRYPTION_FAILURE;
            if (e instanceof BadPaddingException || e instanceof NoSuchAlgorithmException || e instanceof NoSuchPaddingException) {
                cause = FailureCause.RSA_BAD_PROVIDER_FAILURE;
            }
            LOGGER.log(Level.WARNING, "Error while decrypting data: " + e.getMessage());
            throw new DGMCryptoFailureException(cause, e);
        }
    }

    /**
     * Extract a private key from the byte array.
     * Provided byte array can either represent a raw {@code PKCS8} coded key or a PEM encoded key blob.
     *
     * @param bytes Input bytes to process
     * @return A {@link PrivateKey} instance upon success
     * @throws NoSuchAlgorithmException If the requested provider is not available.
     * @throws InvalidKeySpecException  Input array has wrong format
     * @throws NoSuchProviderException  If the requested provider does not handle key specifications.
     */
    public static PrivateKey getPrivateKey(byte[] bytes) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        if (!isPEMFormatted(bytes)) {
            KeyFactory kf = KeyFactory.getInstance(RSA, "SC");
            return kf.generatePrivate(new PKCS8EncodedKeySpec(bytes));
        } else {
            return getPEMKeyPair(bytes).getPrivate();
        }
    }

    private static boolean isPEMFormatted(byte[] bytes) {
        Reader targetReader = new StringReader(new String(bytes));
        boolean isPem;
        try {
            PemReader reader = new PemReader(targetReader);
            isPem = reader.readPemObject() != null;
            reader.close();
            targetReader.close();
        } catch (IOException ex) {
            isPem = false;
        }

        return isPem;
    }

    private static KeyPair getPEMKeyPair(byte[] bytes) {
        Reader targetReader = new StringReader(new String(bytes));
        PEMParser pemParser = new PEMParser(targetReader);
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("SC");
        KeyPair kp = null;
        try {
            Object object = pemParser.readObject();
            if (object instanceof PEMKeyPair) {
                kp = converter.getKeyPair((PEMKeyPair) object);
            } else if (object instanceof PrivateKeyInfo) {
                kp = new KeyPair(null, converter.getPrivateKey((PrivateKeyInfo) object));
            } else if (object instanceof SubjectPublicKeyInfo) {
                kp = new KeyPair(converter.getPublicKey((SubjectPublicKeyInfo) object), null);
            }
            targetReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return kp;
    }

    /**
     * AES decrypt the given data with the given key
     *
     * @param data The data to decryptAES
     * @param key  The key to decryptAES with
     * @return The decrypted bytes
     */
    static byte[] decryptAES(byte[] data, byte[] key, byte[] ivBytes) throws DGMCryptoFailureException {
        try {
            return cipherData(blockCipher(key, ivBytes), data);
        } catch (InvalidCipherTextException e) {
            throw new DGMCryptoFailureException(FailureCause.AES_DECRYPTION_FAILURE, e);
        }
    }

    private static BufferedBlockCipher blockCipher(byte[] key, byte[] ivBytes) {
        PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(new CBCBlockCipher(
            new AESEngine()));
        CipherParameters ivAndKey = new ParametersWithIV(new KeyParameter(key), ivBytes);
        aes.init(false, ivAndKey);
        return aes;
    }

    private static byte[] cipherData(BufferedBlockCipher cipher, byte[] data) throws InvalidCipherTextException {
        int minSize = cipher.getOutputSize(data.length);
        byte[] outBuf = new byte[minSize];
        int length1 = cipher.processBytes(data, 0, data.length, outBuf, 0);
        int length2 = cipher.doFinal(outBuf, length1);
        int actualLength = length1 + length2;
        byte[] result = new byte[actualLength];
        System.arraycopy(outBuf, 0, result, 0, result.length);
        return result;
    }

    /**
     * Hashing
     */
    public static String hashSha512(@SuppressWarnings("SameParameterValue") String input) {
        byte[] hash = hashSha512(input.getBytes(StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        for (byte aByte : hash) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public static byte[] hashSha512(byte[] data) {
        final byte[] dataHashBytes = new byte[64];
        try (DigestInputStream in = new DigestInputStream(new ByteArrayInputStream(data),
            new SHA512Digest())) {
            // Read the stream and do nothing with it
            while (in.read() != -1) {}

            final Digest md = in.getDigest();
            md.doFinal(dataHashBytes, 0);
        } catch (IOException e) {
            //TODO report that calculating hash failed
            e.printStackTrace();
        }
        return dataHashBytes;
    }
}
