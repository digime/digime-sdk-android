/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.secondary

import me.digi.sdk.models.medical.mob2.secondary.CodeableConcept
import me.digi.sdk.models.objects.ModelTestObject

object TCodeableConcept : ModelTestObject<CodeableConcept>(
        CodeableConcept(
                listOf(
                        TCoding.obj
                ),
                "dummyText"
        ),
        """
        {
            "coding":[
                ${TCoding.json}
            ],
            "text":"dummyText"
        }
        """.trimIndent()
)