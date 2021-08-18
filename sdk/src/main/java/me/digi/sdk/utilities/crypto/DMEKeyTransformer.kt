package me.digi.sdk.utilities.crypto

import android.util.Base64
import org.spongycastle.asn1.ASN1InputStream
import org.spongycastle.asn1.pkcs.RSAPublicKey
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPublicKeySpec

object DMEKeyTransformer {

    fun privateKeyFromString(key: String): PrivateKey {
        val privateKeyContent: String =
            key.replace("\n", "").replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
        val data: ByteArray = Base64.decode(privateKeyContent.toByteArray(), 0)
        val spec = PKCS8EncodedKeySpec(data)
        val fact = KeyFactory.getInstance("RSA")

        return fact.generatePrivate(spec)
    }

    fun javaPrivateKeyFromHex(hex: String): PrivateKey {
        val keyBytes = ByteTransformer.bytesFromHexString(hex)
        return javaPrivateKeyFromBytes(keyBytes)
    }

    fun javaPrivateKeyFromBytes(bytes: ByteArray): PrivateKey {
        val keyFactory = KeyFactory.getInstance("RSA", "SC")
        val keySpec = PKCS8EncodedKeySpec(bytes)
        return keyFactory.generatePrivate(keySpec)
    }

    fun hexFromJavaPrivateKey(pk: PrivateKey): String {
        return ByteTransformer.hexStringFromBytes(pk.encoded)
    }

    fun publicKeyFromString(public: String): PublicKey {
        val publicKeyContent =
            public.replace("\n", "").replace("-----BEGIN RSA PUBLIC KEY-----", "")
                .replace("-----END RSA PUBLIC KEY-----", "")

        val rsaPublicKeyBytes = Base64.decode(publicKeyContent, Base64.DEFAULT)
        val rsaPublicKey = RSAPublicKey.getInstance(ASN1InputStream(rsaPublicKeyBytes).readObject())
        return KeyFactory.getInstance("RSA")
            .generatePublic(RSAPublicKeySpec(rsaPublicKey.modulus, rsaPublicKey.publicExponent))
    }
}