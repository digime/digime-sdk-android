/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.primary

import com.squareup.moshi.Json
import me.digi.sdk.models.ItemDetails
import me.digi.sdk.models.medical.mob2.secondary.*

data class Encounter(
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

        @Json(name = "type")
        val type: List<CodeableConcept>?,

        @Json(name = "priority")
        val priority: CodeableConcept?,

        @Json(name = "period")
        val period: Period?,

        @Json(name = "location")
        val location: List<EncounterLocation>?,

        @Json(name = "participant")
        val participant: List<Participant>?
) : ItemDetails.ContentItemDetails