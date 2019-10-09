/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.primary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.secondary.TCodeableConcept
import me.digi.sdk.models.objects.medical.mob2.secondary.TIdentifier
import me.digi.sdk.models.objects.medical.mob2.secondary.TReference

class ConditionSpec : ModelTest<Condition>(Condition::class.java) {
    override val emptyTest: Condition? = null

    override val jsonObjectTests: List<Pair<Condition?, String>> =
            listOf(
                    Pair(
                            Condition(
                                    "dummyEntityId",
                                    "dummyAccountEntityId",
                                    1,
                                    2,
                                    "dummyId",
                                    listOf(
                                            TIdentifier.obj
                                    ),
                                    "dummyClinicalStatus",
                                    "dummyVerificationStatus",
                                    listOf(
                                            TCodeableConcept.obj
                                    ),
                                    TCodeableConcept.obj,
                                    TCodeableConcept.obj,
                                    3,
                                    4,
                                    TReference.obj
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
                                    "clinicalstatus":"dummyClinicalStatus",
                                    "verificationstatus":"dummyVerificationStatus",
                                    "category":[
                                        ${TCodeableConcept.json}
                                    ],
                                    "severity":${TCodeableConcept.json},
                                    "code":${TCodeableConcept.json},
                                    "onsetdatetime":3,
                                    "asserteddate":4,
                                    "asserter":${TReference.json}
                                }
                            """.trimIndent()
                    )
            )
}