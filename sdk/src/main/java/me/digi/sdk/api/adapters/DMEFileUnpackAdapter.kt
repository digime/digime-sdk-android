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

        val encryptedContent = json["fileContent"] as? String ?: throw DMESDKError.InvalidData()
        val encryptedBytes = Base64.decode(encryptedContent, 0)

        val contentBytes = DMEDataDecryptor.dataFromEncryptedBytes(encryptedBytes, privateKeyHex)

        val compression = json["compression"] as? String ?: throw DMESDKError.InvalidData()
        val decompressedContentBytes = DMECompressor.decompressData(contentBytes, compression)

        return DMEFile(metadata, DMEMimeType.APPLICATION_JSON, decompressedContentBytes)
    }

    private fun extractMetadata(rootJSON: JsonObject, context: JsonDeserializationContext): DMEFileMetadata {

        val metadataJSON = rootJSON["fileMetadata"] as? JsonObject ?: throw DMESDKError.InvalidData()
        val metadataObjectType = object: TypeToken<DMEFileMetadata>() {}.type
        return context.deserialize(metadataJSON, metadataObjectType)
    }
}