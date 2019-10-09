/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.health_and_fitness

import me.digi.sdk.models.health_and_fitness.HeartRateZone
import me.digi.sdk.models.objects.ModelTestObject

object THeartRateZone : ModelTestObject<HeartRateZone>(
        HeartRateZone(
                1.5f,
                2.5f,
                3.5f,
                "dummyName",
                4.5f
        ),
        """
        {
            "max":1.5,
            "min":2.5,
            "minutes":3.5,
            "name":"dummyName",
            "caloriesout":4.5
        }
        """.trimIndent()
)