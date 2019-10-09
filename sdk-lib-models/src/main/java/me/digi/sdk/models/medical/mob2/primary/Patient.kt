/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.primary

import com.squareup.moshi.Json
import me.digi.sdk.models.medical.mob2.secondary.*

data class Patient(
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

        @Json(name = "active")
        val active: Boolean?,

        @Json(name = "name")
        val name: List<HumanName>?,

        @Json(name = "telecom")
        val telecom: List<ContactPoint>?,

        @Json(name = "gender")
        val gender: String?,

        @Json(name = "birthdate")
        val birthDate: Long?,

        @Json(name = "deceasedboolean")
        val deceasedBoolean: Boolean?,

        @Json(name = "deceaseddatetime")
        val deceasedDateTime: Long?,

        @Json(name = "address")
        val address: List<Address>?,

        @Json(name = "maritalstatus")
        val maritalStatus: CodeableConcept?,

        @Json(name = "contact")
        val contact: List<Contact>?,

        @Json(name = "communication")
        val communication: List<Communication>?
)