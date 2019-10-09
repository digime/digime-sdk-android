/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import com.squareup.moshi.Json

data class Timing(
        @Json(name = "repeat")
        val repeat: Repeat?,

        @Json(name = "code")
        val code: CodeableConcept?
)