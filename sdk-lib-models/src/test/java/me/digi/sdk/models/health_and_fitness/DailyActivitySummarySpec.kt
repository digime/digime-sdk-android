/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.health_and_fitness

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.health_and_fitness.TDistance
import me.digi.sdk.models.objects.health_and_fitness.TGoals
import me.digi.sdk.models.objects.health_and_fitness.THeartRateZone

class DailyActivitySummarySpec : ModelTest<DailyActivitySummary>(DailyActivitySummary::class.java) {
    override val emptyTest: DailyActivitySummary? = null

    override val jsonObjectTests: List<Pair<DailyActivitySummary?, String>> =
            listOf(
                    Pair(
                            DailyActivitySummary(
                                    "dummyId",
                                    "dummyEntityId",
                                    "dummyAccountEntityId",
                                    1,
                                    2.5f,
                                    3.5f,
                                    4.5f,
                                    5.5f,
                                    6.5f,
                                    7.5f,
                                    8.5f,
                                    9.5f,
                                    10.5f,
                                    11.5f,
                                    12,
                                    13,
                                    14.5f,
                                    listOf(
                                            TDistance.obj
                                    ),
                                    TGoals.obj,
                                    listOf(
                                            THeartRateZone.obj
                                    ),
                                    15,
                                    16.5f,
                                    17.5f,
                                    18.5f
                            ),
                            """
                                {
                                    "id":"dummyId",
                                    "entityid":"dummyEntityId",
                                    "accountentityid":"dummyAccountEntityId",
                                    "createddate":1,
                                    "activescore":2.5,
                                    "sedentaryminutes":3.5,
                                    "lightlyactiveminutes":4.5,
                                    "fairlyactiveminutes":5.5,
                                    "veryactiveminutes":6.5,
                                    "activitycalories":7.5,
                                    "caloriesbmr":8.5,
                                    "marginalcalories":9.5,
                                    "caloriesout":10.5,
                                    "elevation":11.5,
                                    "floors":12,
                                    "steps":13,
                                    "restingheartrate":14.5,
                                    "distances":[
                                        ${TDistance.json}
                                    ],
                                    "goals":${TGoals.json},
                                    "heartratezones":[
                                        ${THeartRateZone.json}
                                    ],
                                    "duration":15,
                                    "heartrate":16.5,
                                    "maxheartrate":17.5,
                                    "minheartrate":18.5
                                }
                            """.trimIndent()
                    )
            )
}