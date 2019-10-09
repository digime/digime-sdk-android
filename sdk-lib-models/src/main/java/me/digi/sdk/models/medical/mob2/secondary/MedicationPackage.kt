/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import com.squareup.moshi.Json

data class MedicationPackage(
        @Json(name = "container")
        val container: CodeableConcept?,

        @Json(name = "content")
        val content: List<MedicationPackageContent>?
)