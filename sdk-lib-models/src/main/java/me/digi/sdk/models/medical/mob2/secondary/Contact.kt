/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import com.squareup.moshi.Json

data class Contact(
        @Json(name = "relationship")
        val relationship: List<CodeableConcept>?,

        @Json(name = "name")
        val name: HumanName?,

        @Json(name = "telecom")
        val telecom: List<ContactPoint>?,

        @Json(name = "address")
        val address: Address?,

        @Json(name = "gender")
        val gender: String?,

        @Json(name = "organization")
        val organization: Organization?,

        @Json(name = "period")
        val period: Period?
)