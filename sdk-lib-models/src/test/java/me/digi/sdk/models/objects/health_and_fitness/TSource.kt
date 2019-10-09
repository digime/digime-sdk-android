/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.health_and_fitness

import me.digi.sdk.models.health_and_fitness.Source
import me.digi.sdk.models.objects.ModelTestObject

object TSource : ModelTestObject<Source>(
        Source(
                "dummyId",
                "dummyName",
                "dummyType",
                "dummyUrl"
        ),
        """
        {
            "id":"dummyId",
            "name":"dummyName",
            "type":"dummyType",
            "url":"dummyUrl"
        }
        """.trimIndent()
)