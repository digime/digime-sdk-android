/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.secondary

import me.digi.sdk.models.medical.mob2.secondary.ReferenceRange
import me.digi.sdk.models.objects.ModelTestObject

object TReferenceRange : ModelTestObject<ReferenceRange>(
        ReferenceRange(
                TQuantity.obj,
                TQuantity.obj,
                TCodeableConcept.obj,
                listOf(
                        TCodeableConcept.obj
                ),
                TRange.obj,
                "dummyText"
        ),
        """
        {
            "low":${TQuantity.json},
            "high":${TQuantity.json},
            "type":${TCodeableConcept.json},
            "appliesto":[
                ${TCodeableConcept.json}
            ],
            "age":${TRange.json},
            "text":"dummyText"
        }
        """.trimIndent()
)