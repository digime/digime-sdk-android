/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.secondary

import me.digi.sdk.models.medical.mob2.secondary.EncounterLocation
import me.digi.sdk.models.objects.ModelTestObject

object TEncounterLocation : ModelTestObject<EncounterLocation>(
        EncounterLocation(
                TLocation.obj,
                "dummyStatus",
                TPeriod.obj
        ),
        """
        {
            "location":${TLocation.json},
            "status":"dummyStatus",
            "period":${TPeriod.json}
        }
        """.trimIndent()
)