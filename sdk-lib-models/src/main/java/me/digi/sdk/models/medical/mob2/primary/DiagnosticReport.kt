/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.primary

import com.squareup.moshi.Json
import me.digi.sdk.models.medical.mob2.secondary.CodeableConcept
import me.digi.sdk.models.medical.mob2.secondary.Identifier
import me.digi.sdk.models.medical.mob2.secondary.Performer
import me.digi.sdk.models.medical.mob2.secondary.Reference

data class DiagnosticReport(
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

        @Json(name = "effectivedatetime")
        val effectiveDateTime: Long?,

        @Json(name = "issued")
        val issued: Long?,

        @Json(name = "code")
        val code: CodeableConcept?,

        @Json(name = "subject")
        val subject: Reference?,

        @Json(name = "performer")
        val performer: Performer?,

        @Json(name = "result")
        val result: List<Observation>?
)