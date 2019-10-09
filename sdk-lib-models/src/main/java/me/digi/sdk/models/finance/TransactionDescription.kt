/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.finance

import com.squareup.moshi.Json

data class TransactionDescription(
        @Json(name = "original")
        val originalRef: String?,

        @Json(name = "simple")
        val simpleRef: String?
)