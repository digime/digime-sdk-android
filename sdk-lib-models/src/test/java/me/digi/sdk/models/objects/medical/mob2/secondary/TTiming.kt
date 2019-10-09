/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.secondary

import me.digi.sdk.models.medical.mob2.secondary.Timing
import me.digi.sdk.models.objects.ModelTestObject

object TTiming : ModelTestObject<Timing>(
        Timing(
                TRepeat.obj,
                TCodeableConcept.obj
        ),
        """
        {
            "repeat":${TRepeat.json},
            "code":${TCodeableConcept.json}
        }
        """.trimIndent()
)