/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import com.squareup.moshi.Json

data class HumanName(
        @Json(name = "use")
        val use: String?,

        @Json(name = "text")
        val text: String?,

        @Json(name = "family")
        val family: String?,

        @Json(name = "given")
        val given: List<String>?,

        @Json(name = "prefix")
        val prefix: List<String>?,

        @Json(name = "suffix")
        val suffix: List<String>?,

        @Json(name = "period")
        val period: Period?
)