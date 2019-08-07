package me.digi.sdk.utilities.crypto

import android.content.Context
import me.digi.sdk.DMESDKError
import org.spongycastle.jce.provider.BouncyCastleProvider
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Security

class DMECryptoUtilities(val context: Context) {

    private val keyStore by lazy {
        Security.addProvider(BouncyCastleProvider())
        KeyStore.getInstance("pkcs12", "SC")
    }

    fun privateKeyHexFrom(p12File: String, password: String): String {

        val inStream = context.assets.open(p12File)
        keyStore.load(inStream, password.toCharArray())

        val keyAlaises = keyStore.aliases().toList().filter { keyStore.isKeyEntry(it) }

        if (keyAlaises.count() != 1) {
            throw DMESDKError.P12ParsingError()
        }

        val key = keyStore.getKey(keyAlaises.first(), password.toCharArray()) as PrivateKey

        return DMEKeyTransformer.hexFromJavaPrivateKey(key)
    }

}