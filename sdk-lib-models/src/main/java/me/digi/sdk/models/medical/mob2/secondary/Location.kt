/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import com.squareup.moshi.Json

data class Location(
        @Json(name = "id")
        val id: String?,

        @Json(name = "identifier")
        val identifier: List<Identifier>?,

        @Json(name = "status")
        val status: String?,

        @Json(name = "name")
        val name: String?,

        @Json(name = "alias")
        val alias: List<String>?,

        @Json(name = "description")
        val description: String?,

        @Json(name = "address")
        val address: Address?,

        @Json(name = "physicaltype")
        val physicalType: CodeableConcept?
)