/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.health_and_fitness

import com.squareup.moshi.Json

/**
 * Activity data class components
 */

/**
 * Specifies the "level" associated with a particular time segment
 */
data class Level(
        @Json(name = "name")
        val name: String?,

        @Json(name = "minutes")
        val minutes: Float?
)

/**
 * Specifies which Activity parameters were entered manually
 */
data class ManualValues(
        @Json(name = "calories")
        val calories: Boolean?,

        @Json(name = "distance")
        val distance: Boolean?,

        @Json(name = "steps")
        val steps: Boolean?
)

/**
 * Specifies the source of the activity eg. a particular workout on fitstar.com
 */
data class Source(
        @Json(name = "id")
        val id: String?,

        @Json(name = "name")
        val name: String?,

        @Json(name = "type")
        val type: String?,

        @Json(name = "url")
        val url: String?
)

/**
 * Milliseconds duration in each category
 */
data class Duration(
        @Json(name = "active")
        val active: Int?,

        @Json(name = "original")
        val original: Int?,

        @Json(name = "total")
        val total: Int?
)

/**
 * Heart rate zone for a particular time segment
 */
data class HeartRateZone(
        @Json(name = "max")
        val max: Float?,

        @Json(name = "min")
        val min: Float?,

        @Json(name = "minutes")
        val minutes: Float?,

        @Json(name = "name")
        val name: String?,

        @Json(name = "caloriesout")
        val caloriesOut: Float?
)