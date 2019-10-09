/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.health_and_fitness

import me.digi.sdk.models.health_and_fitness.Level
import me.digi.sdk.models.objects.ModelTestObject

object TLevel : ModelTestObject<Level>(
        Level(
                "dummyName",
                1.5f
        ),
        """
        {
            "name":"dummyName",
            "minutes":1.5
        }
        """.trimIndent()
)