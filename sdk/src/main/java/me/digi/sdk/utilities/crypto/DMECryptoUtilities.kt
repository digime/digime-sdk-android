package me.digi.sdk.utilities.crypto

import android.content.Context
import android.util.Base64
import me.digi.sdk.DMESDKError
import org.spongycastle.crypto.InvalidCipherTextException
import org.spongycastle.crypto.engines.AESEngine
import org.spongycastle.crypto.modes.CBCBlockCipher
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher
import org.spongycastle.crypto.params.KeyParameter
import org.spongycastle.crypto.params.ParametersWithIV
import org.spongycastle.jce.provider.BouncyCastleProvider
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher

class DMECryptoUtilities(val context: Context) {

    private val keyStore by lazy {
        Security.addProvider(BouncyCastleProvider())
        KeyStore.getInstance("pkcs1", "SC")
    }

    fun privateKeyHexFrom(public: String): String {


            val publicKeyContent =
                public.replace("\n", "").replace("-----BEGIN RSA PRIVATE KEY-----", "")
                    .replace("-----END RSA PRIVATE KEY-----", "")

        val pkcs8EncodedBytes: ByteArray = Base64.decode(publicKeyContent, Base64.DEFAULT)

        // extract the private key


        // extract the private key
        val keySpec = PKCS8EncodedKeySpec(pkcs8EncodedBytes)
        val kf = KeyFactory.getInstance("RSA")
        return DMEKeyTransformer.hexFromJavaPrivateKey(kf.generatePrivate(keySpec))

    }

    companion object {
        internal fun decryptRSA(encryptedBytes: ByteArray, key: PrivateKey): ByteArray {
            try {
                val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding", "SC")
                cipher.init(Cipher.DECRYPT_MODE, key)
                return cipher.doFinal(encryptedBytes)

            }
            catch (error: Throwable) {
                throw DMESDKError.DecryptionFailed() // Throw generic crypto error.
            }
        }

        internal fun encryptRSA(data: ByteArray, publicKey: PublicKey): ByteArray {
            Security.addProvider(BouncyCastleProvider())

            try {
                val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding", "SC")
                cipher.init(Cipher.ENCRYPT_MODE, publicKey)
                return cipher.doFinal(data)
            } catch (e: Exception) {
                throw DMESDKError.EncryptionFailed()
            }
        }

        internal fun decryptAES(encryptedBytes: ByteArray, keyBytes: ByteArray, ivBytes: ByteArray): ByteArray {
            try {

                val cipher = PaddedBufferedBlockCipher(CBCBlockCipher(AESEngine()))
                val cipherParams = ParametersWithIV(KeyParameter(keyBytes), ivBytes)
                cipher.init(false, cipherParams)

                val minSize = cipher.getOutputSize(encryptedBytes.count())
                val outputBuffer = ByteArray(minSize)
                val len1 = cipher.processBytes(
                    encryptedBytes,
                    0,
                    encryptedBytes.count(),
                    outputBuffer,
                    0
                )
                val len2 = cipher.doFinal(outputBuffer, len1)
                val actualLen = len1 + len2

                val result = ByteArray(actualLen)
                System.arraycopy(outputBuffer, 0, result, 0, result.count())

                return result
            }
            catch (error: Throwable) {
                throw DMESDKError.DecryptionFailed() // Throw generic crypto error.
            }
        }

        internal fun encryptAES(data: ByteArray, keyBytes: ByteArray, ivBytes: ByteArray): ByteArray {
            try {
                val cipher = PaddedBufferedBlockCipher(CBCBlockCipher(AESEngine()))

                cipher.init(true, ParametersWithIV(KeyParameter(keyBytes), ivBytes))
                val outBuf = ByteArray(cipher.getOutputSize(data.size))

                val processed = cipher.processBytes(data, 0, data.size, outBuf, 0)
                cipher.doFinal(outBuf, processed)
                return outBuf
            } catch (e: InvalidCipherTextException) {
                throw DMESDKError.EncryptionFailed()
            }

        }

        internal fun hashData(data: ByteArray) =
            MessageDigest
                .getInstance("SHA-512")
                .digest(data)
                .map { String.format("%02X", it) }
                .joinToString("")

        fun generateSecureRandom(length: Int): ByteArray {
            val bytes = ByteArray(length)
            for (i in 0 until length) {
                val x = SecureRandom().nextInt(256)
                bytes[i] = x.toByte()
            }
            return bytes
        }
    }
}