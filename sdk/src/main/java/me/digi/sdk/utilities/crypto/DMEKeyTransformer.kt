package me.digi.sdk.utilities.crypto

import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec

object DMEKeyTransformer {

    fun javaPrivateKeyFromHex(hex: String): PrivateKey {
        val keyBytes = DMEByteTransformer.bytesFromHexString(hex)
        return javaPrivateKeyFromBytes(keyBytes)
    }

    fun javaPrivateKeyFromBytes(bytes: ByteArray): PrivateKey {
        val keyFactory = KeyFactory.getInstance("RSA", "SC")
        val keySpec = PKCS8EncodedKeySpec(bytes)
        return keyFactory.generatePrivate(keySpec)
    }

    fun hexFromJavaPrivateKey(pk: PrivateKey): String {
        return DMEByteTransformer.hexStringFromBytes(pk.encoded)
    }
}