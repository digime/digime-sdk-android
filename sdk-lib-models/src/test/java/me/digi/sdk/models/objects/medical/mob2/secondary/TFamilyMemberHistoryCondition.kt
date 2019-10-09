/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.secondary

import me.digi.sdk.models.medical.mob2.secondary.FamilyMemberHistoryCondition
import me.digi.sdk.models.objects.ModelTestObject

object TFamilyMemberHistoryCondition : ModelTestObject<FamilyMemberHistoryCondition>(
        FamilyMemberHistoryCondition(
                TCodeableConcept.obj,
                TCodeableConcept.obj,
                listOf(
                        TAnnotation.obj
                )
        ),
        """
        {
            "code":${TCodeableConcept.json},
            "outcome":${TCodeableConcept.json},
            "note":[
                ${TAnnotation.json}
            ]
        }
        """.trimIndent()
)