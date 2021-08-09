package me.digi.sdk.api.helpers

import me.digi.sdk.entities.MimeType
import me.digi.sdk.entities.payload.DMEPushPayload
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

internal class DMEMultipartBody(description : RequestBody, requestBody: MultipartBody.Part) {
    var description : RequestBody
    var requestBody: MultipartBody.Part

    init {
        this.description = description
        this.requestBody = requestBody
    }

    companion object {
        private const val REQUEST_BODY_NAME = "file"
    }

    internal class Builder {
        private var description: RequestBody? = null
        private var requestBody: MultipartBody.Part? = null

        fun postboxPushPayload(postboxPushPayload: DMEPushPayload): Builder {
            description = REQUEST_BODY_NAME.toRequestBody(postboxPushPayload.mimeType.stringValue.toMediaTypeOrNull())
            return this
        }

        fun dataContent(dataContent: ByteArray, mimetype: MimeType): Builder {
            val file = File.createTempFile(REQUEST_BODY_NAME, "json")
            val bos = BufferedOutputStream(FileOutputStream(file, false))
            bos.write(dataContent)
            bos.flush()
            bos.close()

            val fileReqBody = file.asRequestBody(mimetype.stringValue.toMediaTypeOrNull())
            this.requestBody = MultipartBody.Part.createFormData(REQUEST_BODY_NAME, REQUEST_BODY_NAME, fileReqBody)
            return this
        }

        fun build(): DMEMultipartBody {
            return description?.let { requestBody?.let { it1 -> DMEMultipartBody(it, it1) } }!!
        }
    }
}