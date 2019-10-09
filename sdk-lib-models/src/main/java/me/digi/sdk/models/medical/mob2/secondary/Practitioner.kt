/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import com.squareup.moshi.Json

data class Practitioner(
        @Json(name = "id")
        val id: String?,

        @Json(name = "identifier")
        val identifier: List<Identifier>?,

        @Json(name = "active")
        val active: Boolean?,

        @Json(name = "name")
        val name: List<HumanName>?,

        @Json(name = "gender")
        val gender: String?,

        @Json(name = "qualification")
        val qualification: List<Qualification>?
)