package me.digi.sdk.utilities.crypto

import android.util.Base64
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPublicKeySpec
import org.bouncycastle.asn1.ASN1InputStream

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

    fun publicKeyFromString(public: String): PublicKey {
        val publicKeyContent =
            public.replace("\n", "").replace("-----BEGIN RSA PUBLIC KEY-----", "")
                .replace("-----END RSA PUBLIC KEY-----", "")

        val rsaPublicKeyBytes = Base64.decode(publicKeyContent, Base64.DEFAULT)
        val rsaPublicKey =
            org.bouncycastle.asn1.pkcs.RSAPublicKey.getInstance(ASN1InputStream(rsaPublicKeyBytes).readObject())
        return KeyFactory.getInstance("RSA")
            .generatePublic(RSAPublicKeySpec(rsaPublicKey.modulus, rsaPublicKey.publicExponent))
    }
}