/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.health_and_fitness

import com.squareup.moshi.Json
import me.digi.sdk.models.ItemDetails

data class DailyActivitySummary(
        @Json(name = "id")
        val id: String?,

        @Json(name = "entityid")
        val entityId: String,

        @Json(name = "accountentityid")
        val accountEntityId: String?,

        @Json(name = "createddate")
        val createdDate: Long?,

        @Json(name = "activescore")
        val activeScore: Float?,

        @Json(name = "sedentaryminutes")
        val sedentaryMinutes: Float?,

        @Json(name = "lightlyactiveminutes")
        val lightlyActiveMinutes: Float?,

        @Json(name = "fairlyactiveminutes")
        val fairlyActiveMinutes: Float?,

        @Json(name = "veryactiveminutes")
        val veryActiveMinutes: Float?,

        @Json(name = "activitycalories")
        val activityCalories: Float?,

        @Json(name = "caloriesbmr")
        val caloriesBmr: Float?,

        @Json(name = "marginalcalories")
        val marginalCalories: Float?,

        @Json(name = "caloriesout")
        val caloriesOut: Float?,

        @Json(name = "elevation")
        val elevation: Float?,

        @Json(name = "floors")
        val floors: Int?,

        @Json(name = "steps")
        val steps: Int?,

        @Json(name = "restingheartrate")
        val restingHeartRate: Float?,

        @Json(name = "distances")
        val distances: List<Distance>?,

        @Json(name = "goals")
        val goals: Goals?,

        @Json(name = "heartratezones")
        val heartRateZones: List<HeartRateZone>?,

        @Json(name = "duration")
        val duration: Long?,

        @Json(name = "heartrate")
        val heartrate: Float?,

        @Json(name = "maxheartrate")
        val maxheartrate: Float?,

        @Json(name = "minheartrate")
        val minheartrate: Float?
) : ItemDetails.ContentItemDetails