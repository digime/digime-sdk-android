/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.primary

import com.squareup.moshi.Json
import me.digi.sdk.models.ItemDetails
import me.digi.sdk.models.medical.mob2.secondary.*
import me.digi.sdk.models.medical.mob2.secondary.Annotation

data class MedicationRequest(
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

        @Json(name = "medicationreference")
        val medicationReference: Medication?,

        @Json(name = "authoredon")
        val authoredOn: Long?,

        @Json(name = "requester")
        val requester: Requester?,

        @Json(name = "note")
        val note: List<Annotation>?,

        @Json(name = "dosageinstruction")
        val dosageInstruction: List<Dosage>?,

        @Json(name = "dispenserequest")
        val dispenseRequest: DispenseRequest?
) : ItemDetails.ContentItemDetails