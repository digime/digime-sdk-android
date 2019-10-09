/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.health_and_fitness

import com.squareup.moshi.Json

data class Goals(
        @Json(name = "activeminutes")
        val activeMinutes: Float?,

        @Json(name = "caloriesout")
        val calories: Float?,

        @Json(name = "distance")
        val distance: Float?,

        @Json(name = "floors")
        val floors: Int?,

        @Json(name = "steps")
        val steps: Int?
)

data class Distance(
        @Json(name = "activity")
        val activity: String?,

        @Json(name = "distance")
        val distance: Float?
)
