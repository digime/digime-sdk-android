/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.primary

import com.squareup.moshi.Json
import me.digi.sdk.models.medical.mob2.secondary.CodeableConcept
import me.digi.sdk.models.medical.mob2.secondary.Identifier
import me.digi.sdk.models.medical.mob2.secondary.Udi

data class Device(
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

        @Json(name = "udi")
        val udi: Udi?,

        @Json(name = "model")
        val model: String?,

        @Json(name = "expirationdate")
        val expirationDate: Long?,

        @Json(name = "status")
        val status: String?,

        @Json(name = "type")
        val type: CodeableConcept?
)