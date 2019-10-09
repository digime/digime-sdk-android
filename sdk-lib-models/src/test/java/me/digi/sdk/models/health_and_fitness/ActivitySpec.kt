/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.health_and_fitness

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.health_and_fitness.*

class ActivitySpec : ModelTest<Activity>(Activity::class.java) {
    override val emptyTest: Activity? = null

    override val jsonObjectTests: List<Pair<Activity?, String>> =
            listOf(
                    Pair(
                            Activity(
                                    "dummyId",
                                    "dummyEntityId",
                                    "dummyAccountEntityId",
                                    "dummyActivityName",
                                    "dummyActivityTypeId",
                                    1,
                                    2,
                                    3,
                                    listOf(
                                            TLevel.obj
                                    ),
                                    4.5f,
                                    5.5f,
                                    6.5f,
                                    TDuration.obj,
                                    7.5f,
                                    8.5f,
                                    9,
                                    listOf(
                                            THeartRateZone.obj
                                    ),
                                    "dummyLogType",
                                    TManualValues.obj,
                                    TSource.obj,
                                    10.5f,
                                    11.5f,
                                    12.5f,
                                    13.5f,
                                    14.5f,
                                    15.5f,
                                    16.5f,
                                    17,
                                    18.5f,
                                    19.5f,
                                    20.5f,
                                    21.5f,
                                    22,
                                    23.5f,
                                    24.5f
                            ),
                            """
                                {
                                    "id":"dummyId",
                                    "entityid":"dummyEntityId",
                                    "accountentityid":"dummyAccountEntityId",
                                    "activityname":"dummyActivityName",
                                    "activitytypeid":"dummyActivityTypeId",
                                    "createddate":1,
                                    "originalstartdate":2,
                                    "updateddate":3,
                                    "activitylevel":[
                                        ${TLevel.json}
                                    ],
                                    "averageheartrate":4.5,
                                    "calories":5.5,
                                    "distance":6.5,
                                    "durations":${TDuration.json},
                                    "elevationgain":7.5,
                                    "speed":8.5,
                                    "steps":9,
                                    "heartratezones":[
                                        ${THeartRateZone.json}
                                    ],
                                    "logtype":"dummyLogType",
                                    "manualvaluesspecified":${TManualValues.json},
                                    "source":${TSource.json},
                                    "bikecadence":10.5,
                                    "duration":11.5,
                                    "maxbikecadence":12.5,
                                    "maxheartrate":13.5,
                                    "maxpace":14.5,
                                    "maxruncadence":15.5,
                                    "maxspeed":16.5,
                                    "numberofactivelengths":17,
                                    "pace":18.5,
                                    "runcadence":19.5,
                                    "startinglatitude":20.5,
                                    "startinglongitude":21.5,
                                    "starttimeoffset":22,
                                    "swimcadence":23.5,
                                    "totalelevationloss":24.5
                                }
                            """.trimIndent()
                    )
            )
}