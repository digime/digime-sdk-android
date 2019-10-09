/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.crypto;

import android.content.Context;

import java.security.PrivateKey;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A key container that allows for efficient wrapping of multiple RSA private keys from different sources.
 * It's primary usage is transparent iteration of decryption keys where separation is ambiguous and is loaded from different sources.
 */
@SuppressWarnings("WeakerAccess")
public class CAKeyStore {
    private final ConcurrentLinkedDeque<PrivateKey> storedKeys = new ConcurrentLinkedDeque<>();
    private static final Logger LOGGER = Logger.getLogger(CAKeyStore.class.getName());

    /**
     * Instantiates a new container with a root key (will always be at the end of deque)
     *
     * @param rootKey Root Private key
     */
    public CAKeyStore(PrivateKey rootKey) {
        addKey(rootKey);
    }

    /**
     * Instantiates a new container with a root key (will always be at the end of deque) in hexadecimal string format
     *
     * @param hexCodedKey Hex coded Root Private key
     */
    public CAKeyStore(String hexCodedKey) {
        try {
            PrivateKey priv = CryptoUtils.getPrivateKey(ByteUtils.hexToBytes(hexCodedKey));
            if (priv != null) {
                addKey(priv);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error loading key: " + ex.getMessage());
        }
    }

    /**
     * Add a private key to head of container.
     *
     * @param k Private key
     */
    public void addKey(PrivateKey k) {
        if (k != null) {
            storedKeys.addFirst(k);
        }
    }

    /**
     * Extracts and adds keys in a P12 keystore located in app assets.
     * If {@code keyAlias} is not set it will load all contained private keys.
     * Upon failure key will be ignored and not included in the container.
     *
     * @param context       Loading context
     * @param assetPath     P12 keystore asset path
     * @param keyAlias      Keystore alias to load from (optional)
     * @param passphrase    Keystore passphrase. Can be null or empty if keystore is not password protected.
     * @param keyPassphrase Passphrase for alias (optional)
     */
    public void addPKCS12KeyFromAssets(Context context, String assetPath, String keyAlias, String passphrase, String keyPassphrase) {
        List<PrivateKey> k = null;
        try {
            k = PKCS12Utils.getPKCS12KeysFromAssets(context, assetPath, keyAlias, passphrase, keyPassphrase);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error loading key: " + ex.getMessage());
        }
        addFromList(k);
    }

    /**
     * Extracts and adds keys in a P12 keystore located in app resources.
     * If {@code keyAlias} is not set it will load all contained private keys.
     * Remarks: This can only be used with resources whose value is the name of an asset files -- that is, it can be
     * used to open drawable, sound, and raw resources; it will fail on string
     * and color resources.
     * Upon failure key will be ignored and not included in the container
     *
     * @param context       Loading context
     * @param resId         the res id
     * @param keyAlias      Keystore alias to load from (optional)
     * @param passphrase    Keystore passphrase. Can be null or empty if keystore is not password protected.
     * @param keyPassphrase Passphrase for alias (optional)
     */
    public void addPKCS12KeyFromResources(Context context, int resId, String keyAlias, String passphrase, String keyPassphrase) {
        List<PrivateKey> k = null;
        try {
            k = PKCS12Utils.getPKCS12KeysFromRawResource(context, resId, keyAlias, passphrase, keyPassphrase);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error loading key: " + ex.getMessage());
        }
        addFromList(k);
    }

    /**
     * Adds keys from a list.
     *
     * @param keyList the key list
     */
    public void addFromList(List<PrivateKey> keyList) {
        if (keyList != null) {
            for (PrivateKey pk : keyList) {
                storedKeys.addFirst(pk);
            }
        }
    }

    /**
     * Returns a new key iterator.
     * Concurrent access operations execute safely
     * across multiple threads.
     *
     * @return Private key iterator.
     */
    public Iterator<PrivateKey> iterator() {
        return storedKeys.iterator();
    }

    /**
     * Check if container is empty.
     *
     * @return {@code true} if this container contains no elements
     */
    public boolean isEmpty() {
        return storedKeys.isEmpty();
    }
}
