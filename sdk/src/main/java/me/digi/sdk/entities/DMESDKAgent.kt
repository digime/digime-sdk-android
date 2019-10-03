package me.digi.sdk.entities

import me.digi.sdk.BuildConfig

data class DMESDKAgent (

    @JvmField
    val name: String = "android",

    @JvmField
    val version: String = BuildConfig.VERSION_NAME.split("-").first()

)