package me.digi.sdk.utilities.crypto

import com.google.gson.annotations.SerializedName

object DMEDataEncryptor {

    private const val symKeyLength = 32
    private const val ivLength = 16

    data class DMEEncryptedData(

        @SerializedName("fileContent")
        val fileContent: ByteArray,

        @SerializedName("metadata")
        val metadata: String,

        @SerializedName("symmetricalKey")
        val symmetricalKey: String,

        @SerializedName("iv")
        val iv: String
    )

    fun encryptedDataFromBytes(publicKey: String, fileContent: ByteArray, metadata: ByteArray): DMEEncryptedData {

        val key = CryptoUtilities.generateSecureRandom(symKeyLength)
        val rsa = DMEKeyTransformer.publicKeyFromString(publicKey)

        val encryptedKey = CryptoUtilities.encryptRSA(key, rsa)
        val base64EncryptedKey = org.spongycastle.util.encoders.Base64.toBase64String(encryptedKey)

        val iv = CryptoUtilities.generateSecureRandom(ivLength)

        val encryptedData = CryptoUtilities.encryptAES(fileContent, key, iv)

        val encryptedMetaData = CryptoUtilities.encryptAES(metadata, key, iv)
        val base64encodedMetadata =
            org.spongycastle.util.encoders.Base64.toBase64String(encryptedMetaData)

        return DMEEncryptedData(
            encryptedData,
            base64encodedMetadata,
            base64EncryptedKey,
            ByteTransformer.hexStringFromBytes(iv)
        )
    }
}