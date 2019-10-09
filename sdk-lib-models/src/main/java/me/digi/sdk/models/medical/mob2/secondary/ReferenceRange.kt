/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import com.squareup.moshi.Json

data class ReferenceRange(
        @Json(name = "low")
        val low: Quantity?,

        @Json(name = "high")
        val high: Quantity?,

        @Json(name = "type")
        val type: CodeableConcept?,

        @Json(name = "appliesto")
        val appliesTo: List<CodeableConcept>?,

        @Json(name = "age")
        val age: Range?,

        @Json(name = "text")
        val text: String?
)