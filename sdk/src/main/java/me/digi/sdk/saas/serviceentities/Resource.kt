package me.digi.sdk.saas.serviceentities


data class Resource(
    val aspectratio: Aspectratio = Aspectratio(),
    val height: Int = 0,
    val mimetype: String = "",
    val resize: String = "",
    val type: Int = 0,
    val url: String = "",
    val width: Int = 0
)