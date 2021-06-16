package me.digi.sdk.saas.serviceentities


data class ServiceGroup(
    val categoryTypeId: Int = 0,
    val expandedSubTitle: String = "",
    val expandedTitle: String = "",
    val id: Int = 0,
    val name: String = "",
    val reference: String = "",
    val resources: List<Resource> = listOf(),
    val subTitle: String = "",
    val title: String = ""
)