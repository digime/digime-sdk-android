/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import com.squareup.moshi.Json

data class Address(
        @Json(name = "use")
        val use: String?,

        @Json(name = "type")
        val type: String?,

        @Json(name = "text")
        val text: String?,

        @Json(name = "line")
        val line: List<String>?,

        @Json(name = "city")
        val city: String?,

        @Json(name = "district")
        val district: String?,

        @Json(name = "state")
        val state: String?,

        @Json(name = "postalcode")
        val postalCode: String?,

        @Json(name = "country")
        val country: String?,

        @Json(name = "period")
        val period: Period?
)