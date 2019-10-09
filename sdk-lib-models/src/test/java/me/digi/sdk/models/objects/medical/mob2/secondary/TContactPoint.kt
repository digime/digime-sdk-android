/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.secondary

import me.digi.sdk.models.medical.mob2.secondary.ContactPoint
import me.digi.sdk.models.objects.ModelTestObject

object TContactPoint : ModelTestObject<ContactPoint>(
        ContactPoint(
                "dummySystem",
                "dummyValue",
                "dummyUse",
                1,
                TPeriod.obj
        ),
        """
        {
            "system":"dummySystem",
            "value":"dummyValue",
            "use":"dummyUse",
            "rank":1,
            "period":${TPeriod.json}
        }
        """.trimIndent()
)