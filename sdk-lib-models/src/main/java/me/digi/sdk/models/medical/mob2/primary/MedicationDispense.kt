/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.primary

import com.squareup.moshi.Json
import me.digi.sdk.models.medical.mob2.secondary.*
import me.digi.sdk.models.medical.mob2.secondary.Annotation

data class MedicationDispense(
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

        @Json(name = "medicationcodeableconcept")
        val medicationCodeableConcept: CodeableConcept?,

        @Json(name = "performer")
        val performer: List<Performer>?,

        @Json(name = "whenprepared")
        val whenPrepared: Long?,

        @Json(name = "whenhandedover")
        val whenHandedOver: Long?,

        @Json(name = "note")
        val note: List<Annotation>?,

        @Json(name = "dosageinstruction")
        val dosageInstruction: List<Dosage>?,

        @Json(name = "notdone")
        val notDone: Boolean?
)