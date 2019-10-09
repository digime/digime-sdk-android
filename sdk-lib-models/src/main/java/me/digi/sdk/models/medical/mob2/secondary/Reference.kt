/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import com.squareup.moshi.Json
import me.digi.sdk.models.medical.mob2.primary.Device
import me.digi.sdk.models.medical.mob2.primary.Patient

data class Reference(
        @Json(name = "reference")
        val reference: String?,

        @Json(name = "identifier")
        val identifier: Identifier?,

        @Json(name = "display")
        val display: String?,

        @Json(name = "practitioner")
        val practitioner: Practitioner?,

        @Json(name = "organization")
        val organization: Organization?,

        @Json(name = "patient")
        val patient: Patient?,

        @Json(name = "device")
        val device: Device?
)