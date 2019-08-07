/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.legacy.crypto;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Base64InputStream;

import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Utility class to assist in reading, loading of, and extraction of private keys from PKCS12 compliant keystores.
 * Provided methods provide multiple input options.
 */
@SuppressWarnings("WeakerAccess")
public class PKCS12Utils {

    static {
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

    /**
     * Extracts and returns keys in a P12 keystore from the provided {@code assetPath} located in app assets.
     * If {@code keyAlias} is not set it will load all contained private keys.
     * Upon failure returns an empty list.
     *
     * @param context       Context to use for loading
     * @param assetPath     Path to the resource in assets
     * @param keyAlias      Alias of key to load from or null if all keys are needed
     * @param passphrase    Keystore passphrase
     * @param keyPassphrase Alias passphrase
     * @return @{code List} of Private keys if any were loaded. If no adequate keys, list will be empty.
     * @throws DGMCryptoFailureException Upon any failure to load the leys
     */
    public static List<PrivateKey> getPKCS12KeysFromAssets(Context context, String assetPath, String keyAlias, String passphrase, String keyPassphrase) throws DGMCryptoFailureException {
        if (context == null) {
            throw new NullPointerException("Can not fetch resource. Context is null!");
        }
        InputStream is = null;
        try {
            is = context.getAssets().open(assetPath);
        } catch (Exception e) {
            throw new DGMCryptoFailureException(FailureCause.KEY_LOAD_FAILURE, e);
        }
        return getKeysFromP12Stream(is, keyAlias, passphrase, keyPassphrase);
    }

    /**
     * Extracts and returns keys in a P12 keystore from the provided {@code resourceID} located in app resources.
     * Remarks: This can only be used with resources whose value is the name of an asset files -- that is, it can be
     * used to open drawable, sound, and raw resources; it will fail on string
     * and color resources.
     * If {@code keyAlias} is not set it will load all contained private keys.
     * Upon failure returns an empty list.
     *
     * @param context       Context to use for loading
     * @param resId         Resource ID
     * @param keyAlias      Alias of key to load from or null if all keys are needed
     * @param passphrase    Keystore passphrase
     * @param keyPassphrase Alias passphrase
     * @return @{code List} of Private keys if any were loaded. If no adequate keys, list will be empty.
     * @throws DGMCryptoFailureException Upon any failure to load the leys
     */
    public static List<PrivateKey> getPKCS12KeysFromRawResource(Context context, int resId, String keyAlias, String passphrase, String keyPassphrase) throws DGMCryptoFailureException {
        if (context == null) {
            throw new NullPointerException("Can not fetch resource. Context is null!");
        }
        InputStream is = null;
        try {
            is = context.getResources().openRawResource(resId);
        } catch (Exception e) {
            throw new DGMCryptoFailureException(FailureCause.KEY_LOAD_FAILURE, e);
        }
        return getKeysFromP12Stream(is, keyAlias, passphrase, keyPassphrase);
    }

    /**
     * Extracts and returns keys in a P12 keystore from the provided base64 encoded {@code String}
     * If {@code keyAlias} is not set it will load all contained private keys.
     * Upon failure returns an empty list.
     *
     * @param base64EncodedPKCS12 Base64 encoded content of PKCS12 keystore
     * @param keyAlias            Alias of key to load from or null if all keys are needed
     * @param passphrase          Keystore passphrase
     * @param keyPassphrase       Alias passphrase
     * @return @{code List} of Private keys if any were loaded. If no adequate keys, list will be empty.
     * @throws DGMCryptoFailureException Upon any failure to load the leys
     */
    public static List<PrivateKey> getPKCS12KeysFromBase64(String base64EncodedPKCS12, String keyAlias, String passphrase, String keyPassphrase) throws DGMCryptoFailureException {
        if (TextUtils.isEmpty(base64EncodedPKCS12)) {
            throw new IllegalArgumentException("Base64 string empty or bad format!");
        }
        InputStream is = null;
        try {
            is = new ByteArrayInputStream(base64EncodedPKCS12.getBytes(StandardCharsets.UTF_8.name()));
        } catch (Exception e) {
            throw new DGMCryptoFailureException(FailureCause.KEY_LOAD_FAILURE, e);
        }
        return getKeysFromP12Stream(new Base64InputStream(is, Base64.DEFAULT), keyAlias, passphrase, keyPassphrase);
    }

    /**
     * Extracts and returns keys in a P12 keystore from the provided {@code byte[]}
     * If {@code keyAlias} is not set it will load all contained private keys.
     * Upon failure returns an empty list.
     *
     * @param pkcs12Bytes   Byte array content of PKCS12 keystore
     * @param keyAlias      Alias of key to load from or null if all keys are needed
     * @param passphrase    Keystore passphrase
     * @param keyPassphrase Alias passphrase
     * @return @{code List} of Private keys if any were loaded. If no adequate keys, list will be empty.
     * @throws DGMCryptoFailureException Upon any failure to load the leys
     */
    public static List<PrivateKey> getPKCS12KeysFromByteArray(byte[] pkcs12Bytes, String keyAlias, String passphrase, String keyPassphrase) throws DGMCryptoFailureException {
        return getKeysFromP12Stream(new ByteArrayInputStream(pkcs12Bytes), keyAlias, passphrase, keyPassphrase);
    }

    /**
     * Extracts and returns ALL keys in a P12 keystore from the provided {@link InputStream}
     * Upon failure returns an empty list.
     *
     * @param pkcs12     PKCS12 store content {@code InputStream}
     * @param passphrase Keystore passphrase
     * @return @{code List} of Private keys if any were loaded. If no adequate keys, list will be empty.
     * @throws DGMCryptoFailureException Upon any failure to load the leys
     */
    public static List<PrivateKey> getKeysFromP12Stream(InputStream pkcs12, String passphrase) throws DGMCryptoFailureException {
        return getKeysFromP12Stream(pkcs12, null, passphrase, null);
    }

    /**
     * Extracts and returns keys in a P12 keystore from the provided {@link InputStream}
     * If {@code keyAlias} is not set it will load all contained private keys.
     * Upon failure returns an empty list.
     *
     * @param pkcs12        PKCS12 store content {@code InputStream}
     * @param keyAlias      Alias of key to load from or null if all keys are needed
     * @param passphrase    Keystore passphrase
     * @param keyPassphrase Alias passphrase
     * @return @{code List} of Private keys if any were loaded. If no adequate keys, list will be empty.
     * @throws DGMCryptoFailureException Upon any failure to load the leys
     */
    public static List<PrivateKey> getKeysFromP12Stream(InputStream pkcs12, String keyAlias, String passphrase, String keyPassphrase) throws DGMCryptoFailureException {
        String keyPass = keyPassphrase == null ? "" : keyPassphrase;
        ArrayList<PrivateKey> foundKeys = new ArrayList<>();
        try {
            KeyStore p12 = KeyStore.getInstance("pkcs12", "SC");
            p12.load(pkcs12, passphrase.toCharArray());
            Enumeration e = p12.aliases();
            while (e.hasMoreElements()) {
                String alias = (String) e.nextElement();
                if (!TextUtils.isEmpty(keyAlias)) {
                    if (!alias.equalsIgnoreCase(keyAlias)) {
                        continue;
                    }
                }
                if (p12.isKeyEntry(alias)) {
                    Key k  = p12.getKey(alias, keyPass.toCharArray());
                    if (k instanceof PrivateKey) {
                        foundKeys.add((PrivateKey) k);
                    }
                }
            }
        } catch (Exception e) {
            throw new DGMCryptoFailureException(FailureCause.INVALID_KEY_FAILURE, e);
        }
        return foundKeys;
    }
}
