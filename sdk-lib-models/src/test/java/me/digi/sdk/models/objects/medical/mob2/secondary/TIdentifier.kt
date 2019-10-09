/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.secondary

import me.digi.sdk.models.medical.mob2.secondary.Identifier
import me.digi.sdk.models.medical.mob2.secondary.Organization
import me.digi.sdk.models.objects.ModelTestObject

object TIdentifier : ModelTestObject<Identifier>(
        Identifier(
                "dummyUse",
                TCodeableConcept.obj,
                "dummySystem",
                "dummyValue",
                TPeriod.obj,
                Organization(
                        "dummyId",
                        emptyList(),
                        true,
                        listOf(
                                TCodeableConcept.obj
                        )
                )
        ),
        """
        {
            "use":"dummyUse",
            "type":${TCodeableConcept.json},
            "system":"dummySystem",
            "value":"dummyValue",
            "period":${TPeriod.json},
            "assigner":{
                "id":"dummyId",
                "identifier":[],
                "active":true,
                "type":[
                    ${TCodeableConcept.json}
                ]
            }
        }
        """.trimIndent()
)