/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.primary

import com.squareup.moshi.Json
import me.digi.sdk.models.ItemDetails
import me.digi.sdk.models.medical.mob2.secondary.Annotation
import me.digi.sdk.models.medical.mob2.secondary.CodeableConcept
import me.digi.sdk.models.medical.mob2.secondary.Identifier

data class Immunization(
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

        @Json(name = "notgiven")
        val notGiven: Boolean?,

        @Json(name = "vaccinecode")
        val vaccineCode: CodeableConcept?,

        @Json(name = "encounter")
        val encounter: Encounter?,

        @Json(name = "date")
        val date: Long?,

        @Json(name = "lotlong")
        val lotLong: String?,

        @Json(name = "site")
        val site: CodeableConcept?,

        @Json(name = "route")
        val route: CodeableConcept?,

        @Json(name = "note")
        val note: List<Annotation>?
) : ItemDetails.ContentItemDetails