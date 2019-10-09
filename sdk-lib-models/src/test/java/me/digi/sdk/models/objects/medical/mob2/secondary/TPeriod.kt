/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.secondary

import me.digi.sdk.models.medical.mob2.secondary.Period
import me.digi.sdk.models.objects.ModelTestObject

object TPeriod : ModelTestObject<Period>(
        Period(
                1,
                2
        ),
        """
        {
            "start":1,
            "end":2
        }
        """.trimIndent()
)