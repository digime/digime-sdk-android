/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.health_and_fitness

import me.digi.sdk.models.health_and_fitness.Goals
import me.digi.sdk.models.objects.ModelTestObject

object TGoals : ModelTestObject<Goals>(
        Goals(
                1.5f,
                2.5f,
                3.5f,
                4,
                5
        ),
        """
        {
            "activeminutes":1.5,
            "caloriesout":2.5,
            "distance":3.5,
            "floors":4,
            "steps":5
        }
        """.trimIndent()
)