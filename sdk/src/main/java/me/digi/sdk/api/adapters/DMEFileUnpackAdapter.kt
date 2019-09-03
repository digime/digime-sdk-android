package me.digi.sdk.api.adapters

import android.util.Base64
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import me.digi.sdk.DMESDKError
import me.digi.sdk.entities.DMEFile
import me.digi.sdk.entities.DMEFileMetadata
import me.digi.sdk.entities.DMEMimeType
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
        val encryptedBytes = Base64.decode(encryptedContent, Base64.DEFAULT)

        val contentBytes = DMEDataDecryptor.dataFromEncryptedBytes(encryptedBytes, privateKeyHex)

        val compression = try { json["compression"].asString } catch(e: Throwable) { DMECompressor.COMPRESSION_NONE }
        val decompressedContentBytes = DMECompressor.decompressData(contentBytes, compression)

        return DMEFile(metadata, DMEMimeType.APPLICATION_JSON, decompressedContentBytes)
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