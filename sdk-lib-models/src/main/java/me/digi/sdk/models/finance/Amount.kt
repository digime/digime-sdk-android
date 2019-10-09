/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.finance

import com.squareup.moshi.Json

data class Amount(
        @Json(name = "amount")
        val amount: Float?,

        @Json(name = "currency")
        val currency: String?
)