/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import com.squareup.moshi.Json

data class ContactPoint(
        @Json(name = "system")
        val system: String?,

        @Json(name = "value")
        val value: String?,

        @Json(name = "use")
        val use: String?,

        @Json(name = "rank")
        val rank: Long?,

        @Json(name = "period")
        val period: Period?
)