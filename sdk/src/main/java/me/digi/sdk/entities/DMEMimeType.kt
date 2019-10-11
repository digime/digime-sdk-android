package me.digi.sdk.entities

import com.google.gson.annotations.SerializedName

enum class DMEMimeType(val stringValue: String) {

    @SerializedName("application/json")
    APPLICATION_JSON("application/json"),

    @SerializedName("application/octet-stream")
    APPLICATION_OCTECTSTREAM("application/octet-stream"),

    @SerializedName("application/pdf")
    APPLICATION_PDF("application/pdf"),

    @SerializedName("image/jpeg")
    IMAGE_JPEG("image/jpeg"),

    @SerializedName("image/tiff")
    IMAGE_TIFF("image/tiff"),

    @SerializedName("image/png")
    IMAGE_PNG("image/png"),

    @SerializedName("image/gif")
    IMAGE_GIF("image/gif"),

    @SerializedName("image/bmp")
    IMAGE_BMP("image/bmp"),


    @SerializedName("text/plain")
    TEXT_PLAIN("text/plain"),

    @SerializedName("text/json")
    TEXT_JSON("text/json"),

}