package me.digi.sdk.utilities.crypto

import android.content.Context
import me.digi.sdk.DMESDKError
import org.spongycastle.crypto.engines.AESEngine
import org.spongycastle.crypto.modes.CBCBlockCipher
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher
import org.spongycastle.crypto.params.KeyParameter
import org.spongycastle.crypto.params.ParametersWithIV
import org.spongycastle.jce.provider.BouncyCastleProvider
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.security.*
import javax.crypto.Cipher

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

    companion object {
        internal fun decryptRSA(encryptedBytes: ByteArray, key: PrivateKey): ByteArray {
            try {

                val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding", "SC")
                cipher.init(Cipher.DECRYPT_MODE, key)
                return cipher.doFinal(encryptedBytes)

            }
            catch(error: Throwable) {
                throw DMESDKError.DecryptionFailed() // Throw generic crypto error.
            }
        }

        internal fun decryptAES(encryptedBytes: ByteArray, keyBytes: ByteArray, ivBytes: ByteArray): ByteArray {
            try {

                val cipher = PaddedBufferedBlockCipher(CBCBlockCipher(AESEngine()))
                val cipherParams = ParametersWithIV(KeyParameter(keyBytes), ivBytes)
                cipher.init(false, cipherParams)

                val minSize = cipher.getOutputSize(encryptedBytes.count())
                val outputBuffer = ByteArray(minSize)
                val len1 = cipher.processBytes(encryptedBytes, 0, encryptedBytes.count(), outputBuffer,0)
                val len2 = cipher.doFinal(outputBuffer, len1)
                val actualLen = len1 + len2

                val result = ByteArray(actualLen)
                System.arraycopy(outputBuffer, 0, result, 0, result.count())

                return result
            }
            catch(error: Throwable) {
                throw DMESDKError.DecryptionFailed() // Throw generic crypto error.
            }
        }

        internal fun hashData(data: ByteArray) =
            MessageDigest
                .getInstance("SHA-512")
                .digest(data)
                .map { String.format("%02X", it) }
                .joinToString("")

        internal fun blockedBytesFromStream(stream: InputStream): ByteArray {
            var readCount: Int
            val buffer = ByteArrayOutputStream()
            val data = ByteArray(16)

            while(true) {
                readCount = stream.read(data)
                if (readCount == -1) {
                    break
                }
                buffer.write(data, 0, readCount)
            }

            buffer.flush()
            return buffer.toByteArray()
        }
    }
}