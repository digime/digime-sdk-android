package me.digi.sdk.entities

import java.util.*

data class TimeRange(
    val from: Date? = null,
    val to: Date? = null,
    val last: String? = null,
    val type: String? = null
)