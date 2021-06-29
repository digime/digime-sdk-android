package me.digi.sdk.api.adapters

import android.util.Base64
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import me.digi.sdk.DMESDKError
import me.digi.sdk.entities.DMEFile
import me.digi.sdk.entities.DMEFileMetadata
import me.digi.sdk.entities.Status
import me.digi.sdk.utilities.DMECompressor
import me.digi.sdk.utilities.crypto.DMEDataDecryptor
import java.lang.reflect.Type

class DMEFileUnpackAdapter(private val privateKeyHex: String): JsonDeserializer<DMEFile> {

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): DMEFile {

        if (json !is JsonObject || context == null) {
            throw DMESDKError.InvalidData()
        }

        val metadata = extractMetadata(json, context)

        val encryptedContent = json["fileContent"].asString ?: throw DMESDKError.InvalidData()
        val encryptedBytes: ByteArray = Base64.decode(encryptedContent, Base64.DEFAULT)

        val contentBytes: ByteArray = DMEDataDecryptor.dataFromEncryptedBytes(encryptedBytes, privateKeyHex)

        val compression: String = try { json["compression"].asString } catch(e: Throwable) { DMECompressor.COMPRESSION_NONE }
        val decompressedContentBytes: ByteArray = DMECompressor.decompressData(contentBytes, compression)

        return DMEFile(String(decompressedContentBytes), status = Status())
    }

    private fun extractMetadata(rootJSON: JsonObject, context: JsonDeserializationContext): DMEFileMetadata? {

        return try {
            val metadataJSON = rootJSON["fileMetadata"].asJsonObject ?: throw DMESDKError.InvalidData()
            val metadataObjectType = object: TypeToken<DMEFileMetadata>() {}.type
            context.deserialize(metadataJSON, metadataObjectType)
        }
        catch(e: Throwable) {
            return null
        }
    }
}