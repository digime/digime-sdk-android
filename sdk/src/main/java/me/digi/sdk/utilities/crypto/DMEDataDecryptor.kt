package me.digi.sdk.utilities.crypto

import me.digi.sdk.SDKError

object DMEDataDecryptor {

    private const val dskLength = 256
    private const val ivLength = 16
    private const val hashLength = 64

    fun dataFromEncryptedBytes(encryptedBytes: ByteArray, privateKeyHex: String): ByteArray {

        val encryptedDSK = encryptedBytes.copyOfRange(0, dskLength)
        val dataIV = encryptedBytes.copyOfRange(dskLength, dskLength + ivLength)

        val privateKey = DMEKeyTransformer.privateKeyFromString(privateKeyHex)
        val dsk = DMECryptoUtilities.decryptRSA(encryptedDSK, privateKey)

        val encryptedContent = encryptedBytes.copyOfRange(dskLength + ivLength, encryptedBytes.count())

        if (encryptedBytes.count() < 352 || encryptedBytes.count() % 16 != 0) {
            throw SDKError.DecryptionFailed()
        }

        val jfsDataAndHash = DMECryptoUtilities.decryptAES(encryptedContent, dsk, dataIV)
        val jfsHash = jfsDataAndHash.copyOfRange(0, hashLength)
        val jfsData = jfsDataAndHash.copyOfRange(hashLength, jfsDataAndHash.count())

        if (!verifyHash(jfsData, jfsHash)) {
            throw SDKError.InvalidData()
        }

        return jfsData
    }

    private fun verifyHash(data: ByteArray, hash: ByteArray): Boolean {
        val computedHash = DMECryptoUtilities.hashData(data)
        val bundledHash = DMEByteTransformer.hexStringFromBytes(hash)
        return computedHash.toUpperCase() == bundledHash.toUpperCase()
    }
}