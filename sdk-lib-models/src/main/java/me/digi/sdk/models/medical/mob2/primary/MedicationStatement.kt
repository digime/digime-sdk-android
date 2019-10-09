/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.primary

import com.squareup.moshi.Json
import me.digi.sdk.models.medical.mob2.secondary.*

data class MedicationStatement(
        @Json(name = "entityid")
        val entityId: String,

        @Json(name = "accountentityid")
        val accountEntityId: String?,

        @Json(name = "createddate")
        val createdDate: Long?,

        @Json(name = "updateddate")
        val updatedDate: Long?,

        @Json(name = "id")
        val id: String?,

        @Json(name = "identifier")
        val identifier: List<Identifier>?,

        @Json(name = "status")
        val status: String?,

        @Json(name = "informationsource")
        val informationSource: Reference?,

        @Json(name = "effectiveperiod")
        val effectivePeriod: Period?,

        @Json(name = "medicationcodeableconcept")
        val medicationCodeableConcept: CodeableConcept?,

        @Json(name = "dosage")
        val dosage: List<Dosage>?
)