/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.government

import com.squareup.moshi.Json

data class VehicleRegistration(
        @Json(name = "accountentityid")
        val accountentityid: String?,

        @Json(name = "colour")
        val colour: String?,

        @Json(name = "createddate")
        val createdDate: Long?,

        @Json(name = "enginecapacity")
        val engineCapacity: Long?,

        @Json(name = "entityid")
        val entityId: String,

        @Json(name = "fueltype")
        val fuelType: String?,

        @Json(name = "gearcount")
        val gearCount: Int?,

        @Json(name = "id")
        val id: String?,

        @Json(name = "manufacturedate")
        val manufactureDate: Long?,

        @Json(name = "make")
        val make: String?,

        @Json(name = "model")
        val model: String?,

        @Json(name = "registrationdate")
        val registrationDate: Long?,

        @Json(name = "transmission")
        val transmission: String?
)