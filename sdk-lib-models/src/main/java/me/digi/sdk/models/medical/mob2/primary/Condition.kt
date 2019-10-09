/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.primary

import com.squareup.moshi.Json
import me.digi.sdk.models.ItemDetails
import me.digi.sdk.models.medical.mob2.secondary.CodeableConcept
import me.digi.sdk.models.medical.mob2.secondary.Identifier
import me.digi.sdk.models.medical.mob2.secondary.Reference

data class Condition(
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

        @Json(name = "clinicalstatus")
        val clinicalStatus: String?,

        @Json(name = "verificationstatus")
        val verificationStatus: String?,

        @Json(name = "category")
        val category: List<CodeableConcept>?,

        @Json(name = "severity")
        val severity: CodeableConcept?,

        @Json(name = "code")
        val code: CodeableConcept?,

        @Json(name = "onsetdatetime")
        val onsetDateTime: Long?,

        @Json(name = "asserteddate")
        val assertedDate: Long?,

        @Json(name = "asserter")
        val asserter: Reference?
) : ItemDetails.ContentItemDetails