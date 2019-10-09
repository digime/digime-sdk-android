/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.primary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.primary.TEncounter
import me.digi.sdk.models.objects.medical.mob2.secondary.TAnnotation
import me.digi.sdk.models.objects.medical.mob2.secondary.TCodeableConcept
import me.digi.sdk.models.objects.medical.mob2.secondary.TIdentifier

class ImmunizationSpec : ModelTest<Immunization>(Immunization::class.java) {
    override val emptyTest: Immunization? = null

    override val jsonObjectTests: List<Pair<Immunization?, String>> =
            listOf(
                    Pair(
                            Immunization(
                                    "dummyEntityId",
                                    "dummyAccountEntityId",
                                    1,
                                    2,
                                    "dummyId",
                                    listOf(
                                            TIdentifier.obj
                                    ),
                                    "dummyStatus",
                                    true,
                                    TCodeableConcept.obj,
                                    TEncounter.obj,
                                    3,
                                    "dummyLotLong",
                                    TCodeableConcept.obj,
                                    TCodeableConcept.obj,
                                    listOf(
                                            TAnnotation.obj
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
                                    "notgiven":true,
                                    "vaccinecode":${TCodeableConcept.json},
                                    "encounter":${TEncounter.json},
                                    "date":3,
                                    "lotlong":"dummyLotLong",
                                    "site":${TCodeableConcept.json},
                                    "route":${TCodeableConcept.json},
                                    "note":[
                                        ${TAnnotation.json}
                                    ]
                                }
                            """.trimIndent()
                    )
            )
}