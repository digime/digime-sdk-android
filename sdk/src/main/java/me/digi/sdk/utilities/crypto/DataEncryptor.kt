package me.digi.sdk.utilities.crypto

import com.google.gson.annotations.SerializedName

object DataEncryptor {

    private const val symKeyLength = 32
    private const val ivLength = 16

    data class EncryptedData(

        @SerializedName("fileContent")
        val fileContent: ByteArray,

        @SerializedName("metadata")
        val metadata: String,

        @SerializedName("symmetricalKey")
        val symmetricalKey: String,

        @SerializedName("iv")
        val iv: String
    )

    fun encryptedDataFromBytes(publicKey: String, fileContent: ByteArray, metadata: ByteArray): EncryptedData {

        val key = CryptoUtilities.generateSecureRandom(symKeyLength)
        val rsa = KeyTransformer.publicKeyFromString(publicKey)

        val encryptedKey = CryptoUtilities.encryptRSA(key, rsa)
        val base64EncryptedKey = org.spongycastle.util.encoders.Base64.toBase64String(encryptedKey)

        val iv = CryptoUtilities.generateSecureRandom(ivLength)

        val encryptedData = CryptoUtilities.encryptAES(fileContent, key, iv)

        val encryptedMetaData = CryptoUtilities.encryptAES(metadata, key, iv)
        val base64encodedMetadata =
            org.spongycastle.util.encoders.Base64.toBase64String(encryptedMetaData)

        return EncryptedData(
            encryptedData,
            base64encodedMetadata,
            base64EncryptedKey,
            ByteTransformer.hexStringFromBytes(iv)
        )
    }
}