/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import com.squareup.moshi.Json

data class DispenseRequest(
        @Json(name = "validityperiod")
        val validityPeriod: Period?,

        @Json(name = "numberofrepeatsallowed")
        val numberOfRepeatsAllowed: Long?,

        @Json(name = "quantity")
        val quantity: Quantity?,

        @Json(name = "expectedsupplyduration")
        val expectedSupplyDuration: Quantity?
)