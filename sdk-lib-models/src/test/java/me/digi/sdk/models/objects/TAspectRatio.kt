/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects

import me.digi.sdk.models.AspectRatio

object TAspectRatio : ModelTestObject<AspectRatio>(
        AspectRatio(
                1.5,
                "dummyActual",
                "dummyClosest"
        ),
        """
        {
            "accuracy":1.5,
            "actual":"dummyActual",
            "closest":"dummyClosest"
        }
        """.trimIndent()
)