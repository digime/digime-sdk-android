/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.primary

import com.squareup.moshi.Json
import me.digi.sdk.models.medical.mob2.secondary.CodeableConcept
import me.digi.sdk.models.medical.mob2.secondary.FamilyMemberHistoryCondition
import me.digi.sdk.models.medical.mob2.secondary.Identifier

data class FamilyMemberHistory(
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

        @Json(name = "date")
        val date: Long?,

        @Json(name = "name")
        val name: String?,

        @Json(name = "deceasedboolean")
        val deceasedBoolean: Boolean?,

        @Json(name = "status")
        val status: String?,

        @Json(name = "relationship")
        val relationship: CodeableConcept?,

        @Json(name = "condition")
        val condition: List<FamilyMemberHistoryCondition>?
)