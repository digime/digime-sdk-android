/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.primary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.secondary.TCodeableConcept
import me.digi.sdk.models.objects.medical.mob2.secondary.TIdentifier
import me.digi.sdk.models.objects.medical.mob2.secondary.TLocation
import me.digi.sdk.models.objects.medical.mob2.secondary.TPerformer

class ProcedureSpec : ModelTest<Procedure>(Procedure::class.java) {
    override val emptyTest: Procedure? = null

    override val jsonObjectTests: List<Pair<Procedure?, String>> =
            listOf(
                    Pair(
                            Procedure(
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
                                    3,
                                    listOf(
                                            TPerformer.obj
                                    ),
                                    TLocation.obj
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
                                    "notdone":true,
                                    "code":${TCodeableConcept.json},
                                    "performeddatetime":3,
                                    "performer":[
                                        ${TPerformer.json}
                                    ],
                                    "location":${TLocation.json}
                                }
                            """.trimIndent()
                    )
            )
}