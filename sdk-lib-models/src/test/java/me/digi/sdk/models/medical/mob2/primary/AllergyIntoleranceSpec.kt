/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.primary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.secondary.*

class AllergyIntoleranceSpec : ModelTest<AllergyIntolerance>(AllergyIntolerance::class.java) {
    override val emptyTest: AllergyIntolerance? = null

    override val jsonObjectTests: List<Pair<AllergyIntolerance?, String>> =
            listOf(
                    Pair(
                            AllergyIntolerance(
                                    "dummyEntityId",
                                    "dummyAccountEntityId",
                                    1,
                                    2,
                                    listOf(
                                            TIdentifier.obj
                                    ),
                                    "dummyClinicalStatus",
                                    "dummyVerificationStatus",
                                    "dummyCriticality",
                                    TCodeableConcept.obj,
                                    3,
                                    4,
                                    TReference.obj,
                                    5,
                                    listOf(
                                            TAnnotation.obj
                                    ),
                                    listOf(
                                            TReaction.obj
                                    )
                            ),
                            """
                                {
                                    "entityid":"dummyEntityId",
                                    "accountentityid":"dummyAccountEntityId",
                                    "createddate":1,
                                    "updateddate":2,
                                    "identifier":[
                                        ${TIdentifier.json}
                                    ],
                                    "clinicalstatus":"dummyClinicalStatus",
                                    "verificationstatus":"dummyVerificationStatus",
                                    "criticality":"dummyCriticality",
                                    "code":${TCodeableConcept.json},
                                    "onsetdatetime":3,
                                    "asserteddate":4,
                                    "recorder":${TReference.json},
                                    "lastoccurence":5,
                                    "note":[
                                        ${TAnnotation.json}
                                    ],
                                    "reaction":[
                                        ${TReaction.json}
                                    ]
                                }
                            """.trimIndent()
                    )
            )
}