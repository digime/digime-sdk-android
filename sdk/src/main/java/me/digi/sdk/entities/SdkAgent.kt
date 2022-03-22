package me.digi.sdk.entities

import me.digi.sdk.BuildConfig

data class SdkAgent(
    val name: String = "android",
    val version: String = BuildConfig.SDK_VERSION
)