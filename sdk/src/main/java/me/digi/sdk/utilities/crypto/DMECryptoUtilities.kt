package me.digi.sdk.utilities.crypto

import android.content.Context
import me.digi.sdk.legacy.crypto.PKCS12Utils
import org.spongycastle.jce.provider.BouncyCastleProvider
import java.io.FileInputStream
import java.security.KeyStore
import java.security.Security

class DMECryptoUtilities(val context: Context) {

    companion object {
        private const val keyCAPrivateKey = "Consent Access Contract"
    }

    private val keyStore by lazy {
        Security.addProvider(BouncyCastleProvider())
        KeyStore.getInstance("pkcs12", "SC")
    }

    fun privateKeyHexFrom(p12File: String, password: String): String {

        val list = context.assets.list("")

        val inStream = context.assets.open("CA_RSA_PRIVATE_KEY.p12")
        PKCS12Utils.getKeysFromP12Stream(inStream, "monkey periscope")
//        keyStore.load(dataStream, password.toCharArray())

        val privateKey = keyStore.getKey(keyCAPrivateKey, password.toCharArray())

        return privateKey.toString()
    }

}