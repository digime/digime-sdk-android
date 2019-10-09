/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.secondary

import me.digi.sdk.models.medical.mob2.secondary.Practitioner
import me.digi.sdk.models.objects.ModelTestObject

object TPractitioner : ModelTestObject<Practitioner>(
        Practitioner(
                "dummyId",
                listOf(
                        TIdentifier.obj
                ),
                true,
                listOf(
                        THumanName.obj
                ),
                "dummyGender",
                listOf(
                        TQualification.obj
                )
        ),
        """
        {
            "id":"dummyId",
            "identifier":[
                ${TIdentifier.json}
            ],
            "active":true,
            "name":[
                ${THumanName.json}
            ],
            "gender":"dummyGender",
            "qualification":[
                ${TQualification.json}
            ]
        }
        """.trimIndent()
)