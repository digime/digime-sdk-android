package me.digi.sdk.utilities.crypto

import org.spongycastle.jcajce.provider.asymmetric.RSA
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec

object DMEKeyTransformer {

    fun javaPrivateKeyFromHex(hex: String): PrivateKey {
        val keyFactory = KeyFactory.getInstance("RSA", "SC")
        val keyBytes = DMEByteTransformer.bytesFromHexString(hex)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        return keyFactory.generatePrivate(keySpec)
    }

    fun hexFromJavaPrivateKey(pk: PrivateKey): String {
        return DMEByteTransformer.hexStringFromBytes(pk.encoded)
    }
}