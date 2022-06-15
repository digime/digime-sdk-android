package me.digi.sdk.api.adapters

import android.util.Base64
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import me.digi.sdk.SDKError
import me.digi.sdk.entities.FileMetadata
import me.digi.sdk.entities.response.FileItem
import me.digi.sdk.utilities.Compressor
import me.digi.sdk.utilities.crypto.DataDecryptor
import java.lang.reflect.Type

class FileUnpackAdapter(private val privateKeyHex: String) : JsonDeserializer<FileItem> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): FileItem {

        if (json !is JsonObject || context == null) {
            throw SDKError.InvalidData()
        }

        val metadata = extractMetadata(json, context)

        val encryptedContent = json["fileContent"].asString ?: throw SDKError.InvalidData()
        val encryptedBytes: ByteArray = Base64.decode(encryptedContent, Base64.DEFAULT)

        val contentBytes: ByteArray =
            DataDecryptor.dataFromEncryptedBytes(encryptedBytes, privateKeyHex)

        val compression: String = try {
            json["compression"].asString
        } catch (e: Throwable) {
            Compressor.COMPRESSION_NONE
        }
        val decompressedContentBytes: ByteArray =
            Compressor.decompressData(contentBytes, compression)

        return FileItem(String(decompressedContentBytes))
    }

    private fun extractMetadata(
        rootJSON: JsonObject,
        context: JsonDeserializationContext
    ): FileMetadata? {

        return try {
            val metadataJSON = rootJSON["fileMetadata"].asJsonObject ?: throw SDKError.InvalidData()
            val metadataObjectType = object : TypeToken<FileMetadata>() {}.type
            context.deserialize(metadataJSON, metadataObjectType)
        } catch (e: Throwable) {
            return null
        }
    }
}