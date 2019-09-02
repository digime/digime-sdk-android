package me.digi.sdk.utilities.crypto

import me.digi.sdk.DMESDKError
import java.nio.charset.StandardCharsets

object DMEDataDecryptor {

    private const val dskLength = 256
    private const val ivLength = 16
    private const val hashLength = 64

    fun dataFromEncryptedBytes(encryptedBytes: ByteArray, privateKeyHex: String): ByteArray {

        val inStream = encryptedBytes.inputStream()

        val encryptedDSK = ByteArray(dskLength)
        val dataIV = ByteArray(ivLength)

        if (encryptedDSK.count() != inStream.read(encryptedDSK) || dataIV.count() != inStream.read(dataIV)) {
            throw DMESDKError.DecryptionFailed()
        }

        val privateKey = DMEKeyTransformer.javaPrivateKeyFromHex(privateKeyHex)
        val dsk = DMECryptoUtilities.decryptRSA(encryptedDSK, privateKey)

        val encryptedContent = inStream.readBytes()
        val totalLength = encryptedContent.count() + dskLength + ivLength

        if (totalLength < 352 || totalLength % 16 != 0) {
            throw DMESDKError.DecryptionFailed()
        }

        val jfsDataAndHash = DMECryptoUtilities.decryptAES(encryptedContent, dsk, dataIV)
        val jfsHash = jfsDataAndHash.copyOfRange(0, hashLength)
        val jfsData = jfsDataAndHash.copyOfRange(hashLength, jfsDataAndHash.count() - hashLength)

        if (!verifyHash(jfsData, jfsHash)) {
            throw DMESDKError.InvalidData()
        }

        return jfsData
    }

//    private fun blockedBytesFromStream(stream: InputStream): ByteArray {
//        var readCount: Int
//        val buffer = ByteArrayOutputStream()
//        val data = ByteArray(16)
//
//        while ((readCount = stream.read(data)) != -1) {
//            buffer.write(data, 0, readCount)
//        }
//    }

    private fun verifyHash(data: ByteArray, hash: ByteArray): Boolean {
        val computedHash = DMECryptoUtilities.hashData(data)
        val bundledHash = String(hash, StandardCharsets.UTF_8)
        return computedHash == bundledHash
    }
}