package me.digi.sdk.entities.service

import com.google.gson.annotations.SerializedName

data class AspectRatio(
    val accuracy: Int = 0,
    val `actual`: String = "",
    val closest: String = ""
)

data class Resource(
    @SerializedName("aspectratio")
    val aspectRatio: AspectRatio = AspectRatio(),
    val height: Int = 0,
    val mimetype: String = "",
    val resize: String = "",
    val type: Int = 0,
    val url: String = "",
    val width: Int = 0
)

data class Service(
    val homepageURL: String = "",
    val id: Int = 0,
    val name: String = "",
    val providerId: Int = 0,
    val publishedDate: Long = 0,
    val publishedStatus: String = "",
    val reference: String = "",
    val resources: List<Resource> = listOf(),
    val serviceId: Int = 0,
    val subTitle: String = "",
    val title: String = ""
)

data class Data(val services: List<Service> = listOf())

data class ServicesResponse(val `data`: Data = Data())