/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.legacy.crypto;

import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Base64InputStream;

import org.spongycastle.util.Arrays;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.util.Iterator;

import static me.digi.sdk.legacy.crypto.CryptoUtils.*;

public class CACryptoProvider {
    private static final int HASH_LENGTH = 64;
    private static final int ENCRYPTED_DSK_LENGTH = 256;
    private static final int DIV_LENGTH = 16;

    private CAKeyStore providerKeys;

    public CACryptoProvider(@NonNull CAKeyStore kp) {
        this.providerKeys = kp;
    }

    public CACryptoProvider(@NonNull PrivateKey privateKey) {
        this.providerKeys = new CAKeyStore(privateKey);
    }

    public boolean hasValidKeys() {
        return !providerKeys.isEmpty();
    }

    public byte[] decryptStream(@NonNull InputStream fileInputStream) throws IOException, DGMCryptoFailureException {
        return decryptStream(fileInputStream, true);
    }

    public byte[] decryptStream(@NonNull InputStream fileInputStream, boolean streamBase64Encoded) throws IOException, DGMCryptoFailureException {
        byte[] encryptedDSK = new byte[ENCRYPTED_DSK_LENGTH];
        byte[] DIV = new byte[DIV_LENGTH];

        if (providerKeys.isEmpty()) {
            throw new DGMCryptoFailureException(FailureCause.INVALID_KEY_FAILURE);
        }
        InputStream dataStream = fileInputStream;
        if (streamBase64Encoded) {
            dataStream = new Base64InputStream(fileInputStream, Base64.DEFAULT);
        }

        if ( dataStream.read(encryptedDSK) != encryptedDSK.length //read DSK
             || dataStream.read(DIV) != DIV.length //read DIV header
                ) {
            throw new DGMCryptoFailureException(FailureCause.FILE_READING_FAILURE);
        }

        InputStream dataAndHash = null;
        Iterator<PrivateKey> keyIterator = providerKeys.iterator();
        boolean retry = true;
        while (keyIterator.hasNext() && retry) {
            try {
                PrivateKey currentKey = keyIterator.next();
                byte[] DSK = decryptRSA(encryptedDSK, currentKey);
                byte[] content = ByteUtils.readBytesFromStream(dataStream);
                int totalLength = content.length + ENCRYPTED_DSK_LENGTH + DIV_LENGTH;

                if (totalLength < 352 || totalLength % 16 != 0) {
                    throw new DGMCryptoFailureException(FailureCause.CHECKSUM_CORRUPTED_FAILURE);
                }

                dataAndHash = new ByteArrayInputStream(decryptAES(content, DSK, DIV));
                retry = false;
            } catch (Exception e) {
                if (!keyIterator.hasNext())
                    throw new DGMCryptoFailureException(FailureCause.DATA_CORRUPTED_FAILURE, e);
            }
        }

        return readAndVerify(dataAndHash);
    }

    private byte[] readAndVerify(InputStream dataAndHash) throws DGMCryptoFailureException, IOException {
        byte[] hash = new byte[HASH_LENGTH];

        if (dataAndHash.read(hash) != hash.length) {
            throw new DGMCryptoFailureException(FailureCause.CHECKSUM_CORRUPTED_FAILURE);
        }

        byte[] data = ByteUtils.readBytesFromStream(dataAndHash);
        verifyHashForData(data, hash);

        return data;
    }

    private void verifyHashForData(byte[] data, byte[] hash) throws DGMCryptoFailureException {
        byte[] newHash = hashSha512(data);

        if (!Arrays.areEqual(newHash, hash)) {
            throw new DGMCryptoFailureException(FailureCause.DATA_CORRUPTED_FAILURE);
        }
    }

}
