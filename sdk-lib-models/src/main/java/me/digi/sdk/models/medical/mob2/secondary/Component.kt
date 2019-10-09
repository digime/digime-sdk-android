/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import com.squareup.moshi.Json

data class Component(
        @Json(name = "code")
        val code: CodeableConcept?,

        @Json(name = "valuequantity")
        val valueQuantity: Quantity?
)