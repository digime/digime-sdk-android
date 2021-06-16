package me.digi.sdk.saas.serviceentities


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