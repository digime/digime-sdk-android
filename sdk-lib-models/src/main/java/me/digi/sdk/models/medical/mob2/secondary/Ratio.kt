/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import com.squareup.moshi.Json

data class Ratio(
        @Json(name = "numerator")
        val numerator: Quantity?,

        @Json(name = "denominator")
        val denominator: Quantity?
)