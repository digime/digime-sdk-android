package me.digi.sdk.entities

import com.google.gson.annotations.SerializedName
import me.digi.sdk.entities.payload.CredentialsPayload

data class WriteDataPayload(
    val data: Data,
    val metadata: ByteArray,
    val content: ByteArray,
    @SerializedName("mimetype")
    val mimeType: MimeType
)

data class Data(
    val key: String? = null,
    val postboxId: String? = null,
    val publicKey: String? = null
)

data class WriteDataInfo(
    val postboxId: String? = null,
    val publicKey: String? = null
)

// TODO: Can be removed
data class OngoingData(
    val session: Session? = null,
    val data: WriteDataInfo? = null,
    val credentials: CredentialsPayload? = null
)