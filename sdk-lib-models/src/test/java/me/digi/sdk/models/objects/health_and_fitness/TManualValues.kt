/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.health_and_fitness

import me.digi.sdk.models.health_and_fitness.ManualValues
import me.digi.sdk.models.objects.ModelTestObject

object TManualValues : ModelTestObject<ManualValues>(
        ManualValues(
                true,
                true,
                true
        ),
        """
        {
            "calories":true,
            "distance":true,
            "steps":true
        }
        """.trimIndent()
)