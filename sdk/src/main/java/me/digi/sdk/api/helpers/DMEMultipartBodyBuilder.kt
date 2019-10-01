package me.digi.sdk.api.helpers

import me.digi.sdk.entities.DMEPostboxFile
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

internal class DMEMultipartBodyBuilder {
    companion object {
        private const val REQUEST_BODY_NAME = "file"
    }

    data class MultipartBody(
        @JvmField
        val description: RequestBody,

        @JvmField
        val requestBody: okhttp3.MultipartBody.Part
    )

    fun buildMultipartRequestBody(postboxFile: DMEPostboxFile, fileContent: ByteArray): MultipartBody {
        val file = File.createTempFile(REQUEST_BODY_NAME, "json")
        val bos = BufferedOutputStream(FileOutputStream(file, false))
        bos.write(fileContent)
        bos.flush()
        bos.close()

        val fileReqBody = file.asRequestBody(postboxFile.mimeType.stringValue.toMediaTypeOrNull())
        val requestBody = okhttp3.MultipartBody.Part.createFormData(REQUEST_BODY_NAME, REQUEST_BODY_NAME, fileReqBody)
        val description = REQUEST_BODY_NAME.toRequestBody(postboxFile.mimeType.stringValue.toMediaTypeOrNull())

        return MultipartBody(description, requestBody)
    }
}