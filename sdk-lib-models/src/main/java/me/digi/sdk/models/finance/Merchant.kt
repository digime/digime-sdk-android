/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.finance

import com.squareup.moshi.Json

data class Merchant(
        @Json(name = "id")
        val id: String?,

        @Json(name = "name")
        val name: String?
)