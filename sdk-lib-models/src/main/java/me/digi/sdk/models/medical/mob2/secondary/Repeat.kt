/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import com.squareup.moshi.Json

data class Repeat(
        @Json(name = "frequency")
        val frequency: Long?,

        @Json(name = "period")
        val period: Long?,

        @Json(name = "periodunit")
        val periodUnit: String?,

        @Json(name = "boundsperiod")
        val boundsPeriod: Period?
)