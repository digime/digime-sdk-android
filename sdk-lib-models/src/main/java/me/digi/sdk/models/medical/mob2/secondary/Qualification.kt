/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import com.squareup.moshi.Json

data class Qualification(
        @Json(name = "identifier")
        val identifier: List<Identifier>?,

        @Json(name = "code")
        val code: CodeableConcept?,

        @Json(name = "period")
        val period: Period?,

        @Json(name = "issuer")
        val issuer: Organization?
)