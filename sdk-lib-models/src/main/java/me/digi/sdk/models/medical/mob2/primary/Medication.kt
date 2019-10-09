/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.primary

import com.squareup.moshi.Json
import me.digi.sdk.models.medical.mob2.secondary.CodeableConcept
import me.digi.sdk.models.medical.mob2.secondary.MedicationPackage

data class Medication(
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

        @Json(name = "code")
        val code: CodeableConcept?,

        @Json(name = "status")
        val status: String?,

        @Json(name = "form")
        val form: CodeableConcept?,

        @Json(name = "package")
        val medicationPackage: MedicationPackage?
)