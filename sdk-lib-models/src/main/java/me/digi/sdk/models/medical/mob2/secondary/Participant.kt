/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import com.squareup.moshi.Json

data class Participant(
        @Json(name = "type")
        val type: CodeableConcept?,

        @Json(name = "period")
        val period: Period?,

        @Json(name = "individual")
        val individual: Reference?,

        @Json(name = "required")
        val required: String?,

        @Json(name = "status")
        val status: String?
)