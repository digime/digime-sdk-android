/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.secondary

import me.digi.sdk.models.medical.mob2.secondary.HumanName
import me.digi.sdk.models.objects.ModelTestObject

object THumanName : ModelTestObject<HumanName>(
        HumanName(
                "dummyUse",
                "dummyText",
                "dummyFamily",
                listOf("dummyGiven1", "dummyGiven2"),
                listOf("dummyPrefix1", "dummyPrefix2"),
                listOf("dummySuffix1", "dummySuffix2"),
                TPeriod.obj
        ),
        """
        {
            "use":"dummyUse",
            "text":"dummyText",
            "family":"dummyFamily",
            "given":["dummyGiven1","dummyGiven2"],
            "prefix":["dummyPrefix1","dummyPrefix2"],
            "suffix":["dummySuffix1","dummySuffix2"],
            "period":${TPeriod.json}
        }
        """.trimIndent()
)