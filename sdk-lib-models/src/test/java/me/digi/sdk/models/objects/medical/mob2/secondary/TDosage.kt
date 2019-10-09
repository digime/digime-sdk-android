/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.secondary

import me.digi.sdk.models.medical.mob2.secondary.Dosage
import me.digi.sdk.models.objects.ModelTestObject

object TDosage : ModelTestObject<Dosage>(
        Dosage(
                1,
                "dummyText",
                TTiming.obj,
                true,
                TCodeableConcept.obj,
                TCodeableConcept.obj,
                TCodeableConcept.obj,
                TQuantity.obj
        ),
        """
        {
            "sequence":1,
            "text":"dummyText",
            "timing":${TTiming.json},
            "asneededboolean":true,
            "site":${TCodeableConcept.json},
            "route":${TCodeableConcept.json},
            "method":${TCodeableConcept.json},
            "dosequantity":${TQuantity.json}
        }
        """.trimIndent()
)