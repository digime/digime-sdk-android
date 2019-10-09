/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.primary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.secondary.TCodeableConcept
import me.digi.sdk.models.objects.medical.mob2.secondary.TFamilyMemberHistoryCondition
import me.digi.sdk.models.objects.medical.mob2.secondary.TIdentifier

class FamilyMemberHistorySpec : ModelTest<FamilyMemberHistory>(FamilyMemberHistory::class.java) {
    override val emptyTest: FamilyMemberHistory? = null

    override val jsonObjectTests: List<Pair<FamilyMemberHistory?, String>> =
            listOf(
                    Pair(
                            FamilyMemberHistory(
                                    "dummyEntityId",
                                    "dummyAccountEntityId",
                                    1,
                                    2,
                                    "dummyId",
                                    listOf(
                                            TIdentifier.obj
                                    ),
                                    3,
                                    "dummyName",
                                    true,
                                    "dummyStatus",
                                    TCodeableConcept.obj,
                                    listOf(
                                            TFamilyMemberHistoryCondition.obj
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
                                    "date":3,
                                    "name":"dummyName",
                                    "deceasedboolean":true,
                                    "status":"dummyStatus",
                                    "relationship":${TCodeableConcept.json},
                                    "condition":[
                                        ${TFamilyMemberHistoryCondition.json}
                                    ]
                                }
                            """.trimIndent()
                    )
            )
}