package me.digi.sdk.utilities.crypto

import me.digi.sdk.SDKError

object DataDecryptor {

    private const val dskLength = 256
    private const val ivLength = 16

    fun dataFromEncryptedBytes(encryptedBytes: ByteArray, privateKeyHex: String): ByteArray {

        val encryptedDSK = encryptedBytes.copyOfRange(0, dskLength)
        val dataIV = encryptedBytes.copyOfRange(dskLength, dskLength + ivLength)

        val privateKey = KeyTransformer.privateKeyFromString(privateKeyHex)
        val dsk = CryptoUtilities.decryptRSA(encryptedDSK, privateKey)

        val encryptedContent =
            encryptedBytes.copyOfRange(dskLength + ivLength, encryptedBytes.count())

        if (encryptedBytes.count() < 352 || encryptedBytes.count() % 16 != 0) {
            throw SDKError.DecryptionFailed()
        }

        return CryptoUtilities.decryptAES(encryptedContent, dsk, dataIV)
    }
}