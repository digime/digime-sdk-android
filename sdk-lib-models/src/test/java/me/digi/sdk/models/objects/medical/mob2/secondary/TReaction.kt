/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.secondary

import me.digi.sdk.models.medical.mob2.secondary.Reaction
import me.digi.sdk.models.objects.ModelTestObject

object TReaction : ModelTestObject<Reaction>(
        Reaction(
                TCodeableConcept.obj,
                1,
                listOf(
                        TCodeableConcept.obj
                ),
                listOf(
                        TAnnotation.obj
                )
        ),
        """
        {
            "substance":${TCodeableConcept.json},
            "onset":1,
            "manifestation":[
                ${TCodeableConcept.json}
            ],
            "note":[
                ${TAnnotation.json}
            ]
        }
        """.trimIndent()
)