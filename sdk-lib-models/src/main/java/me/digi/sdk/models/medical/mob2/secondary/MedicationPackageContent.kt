/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import com.squareup.moshi.Json
import me.digi.sdk.models.medical.mob2.primary.Medication

data class MedicationPackageContent(
        @Json(name = "itemcodeableconcept")
        val itemCodeableConcept: CodeableConcept?,

        @Json(name = "itemReference")
        val itemReference: Medication?,

        @Json(name = "amount")
        val amount: Quantity?
)