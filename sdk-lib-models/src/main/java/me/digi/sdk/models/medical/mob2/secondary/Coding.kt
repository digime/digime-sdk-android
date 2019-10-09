/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import com.squareup.moshi.Json

data class Coding(
        @Json(name = "code")
        val code: String?,

        @Json(name = "display")
        val display: String?,

        @Json(name = "system")
        val system: String?,

        @Json(name = "version")
        val version: String?
)