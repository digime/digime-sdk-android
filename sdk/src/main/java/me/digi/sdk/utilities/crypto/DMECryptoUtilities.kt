package me.digi.sdk.utilities.crypto

import org.spongycastle.jce.provider.BouncyCastleProvider
import java.io.FileInputStream
import java.security.KeyStore
import java.security.Security

object DMECryptoUtilities {

    private const val keyCAPrivateKey = "Consent Access Contract"
    private val keyStore by lazy {
        Security.addProvider(BouncyCastleProvider())
        KeyStore.getInstance("pkcs12", "SC")
    }



    fun privateKeyHexFrom(p12File: String, password: String): String {

        val dataStream = FileInputStream(p12File)
        keyStore.load(dataStream, password.toCharArray())

        val privateKey = keyStore.getKey(keyCAPrivateKey, password.toCharArray())

        return privateKey.toString()
    }

}