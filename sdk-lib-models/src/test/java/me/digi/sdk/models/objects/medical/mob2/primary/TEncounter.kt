/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.primary

import me.digi.sdk.models.medical.mob2.primary.Encounter
import me.digi.sdk.models.objects.ModelTestObject
import me.digi.sdk.models.objects.medical.mob2.secondary.*

object TEncounter : ModelTestObject<Encounter>(
        Encounter(
                "dummyEntityId",
                "dummyAccountEntityId",
                1,
                2,
                "dummyId",
                listOf(
                        TIdentifier.obj
                ),
                "dummyStatus",
                listOf(
                        TCodeableConcept.obj
                ),
                TCodeableConcept.obj,
                TPeriod.obj,
                listOf(
                        TEncounterLocation.obj
                ),
                listOf(
                        TParticipant.obj
                )
        ),
        """
        {
            "entityid":"dummyEntityId",
            "accountentityid":"dummyAccountEntityId",
            "createddate":1,
            "updateddate":2,
            "id":"dummyId",
            "identifier":[
                ${TIdentifier.json}
            ],
            "status":"dummyStatus",
            "type":[
                ${TCodeableConcept.json}
            ],
            "priority":${TCodeableConcept.json},
            "period":${TPeriod.json},
            "location":[
                ${TEncounterLocation.json}
            ],
            "participant":[
                ${TParticipant.json}
            ]
        }
        """.trimIndent()
)