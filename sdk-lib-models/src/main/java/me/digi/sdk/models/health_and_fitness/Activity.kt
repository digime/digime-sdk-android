/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.health_and_fitness

import com.squareup.moshi.Json
import me.digi.sdk.models.ItemDetails

data class Activity(
        @Json(name = "id")
        val id: String?,

        @Json(name = "entityid")
        val entityId: String,

        @Json(name = "accountentityid")
        val accountEntityId: String?,

        @Json(name = "activityname")
        val activityName: String?,

        @Json(name = "activitytypeid")
        val activityTypeId: String?,

        @Json(name = "createddate")
        val createdDate: Long?,

        @Json(name = "originalstartdate")
        val startDate: Long?,

        @Json(name = "updateddate")
        val updatedDate: Long?,

        @Json(name = "activitylevel")
        val levels: List<Level>?,

        @Json(name = "averageheartrate")
        val averageHeartRate: Float?,

        @Json(name = "calories")
        val calories: Float?,

        @Json(name = "distance")
        val distance: Float?,

        @Json(name = "durations")
        val durations: Duration?,

        @Json(name = "elevationgain")
        val elevationGain: Float?,

        @Json(name = "speed")
        val speed: Float?,

        @Json(name = "steps")
        val steps: Int?,

        @Json(name = "heartratezones")
        val heartRateZones: List<HeartRateZone>?,

        @Json(name = "logtype")
        val logType: String?,

        @Json(name = "manualvaluesspecified")
        val manualValues: ManualValues?,

        @Json(name = "source")
        val source: Source?,

        @Json(name = "bikecadence")
        val bikecadence: Float?,

        @Json(name = "duration")
        val duration: Float?,

        @Json(name = "maxbikecadence")
        val maxbikecadence: Float?,

        @Json(name = "maxheartrate")
        val maxheartrate: Float?,

        @Json(name = "maxpace")
        val maxpace: Float?,

        @Json(name = "maxruncadence")
        val maxruncadence: Float?,

        @Json(name = "maxspeed")
        val maxspeed: Float?,

        @Json(name = "numberofactivelengths")
        val numberofactivelengths: Long?,

        @Json(name = "pace")
        val pace: Float?,

        @Json(name = "runcadence")
        val runcadence: Float?,

        @Json(name = "startinglatitude")
        val startinglatitude: Float?,

        @Json(name = "startinglongitude")
        val startinglongitude: Float?,

        @Json(name = "starttimeoffset")
        val starttimeoffset: Long?,

        @Json(name = "swimcadence")
        val swimcadence: Float?,

        @Json(name = "totalelevationloss")
        val totalelevationloss: Float?
) : ItemDetails.ContentItemDetails