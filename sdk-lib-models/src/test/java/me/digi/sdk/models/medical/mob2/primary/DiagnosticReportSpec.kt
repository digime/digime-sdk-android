/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.primary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.primary.TObservation
import me.digi.sdk.models.objects.medical.mob2.secondary.TCodeableConcept
import me.digi.sdk.models.objects.medical.mob2.secondary.TIdentifier
import me.digi.sdk.models.objects.medical.mob2.secondary.TPerformer
import me.digi.sdk.models.objects.medical.mob2.secondary.TReference

class DiagnosticReportSpec : ModelTest<DiagnosticReport>(DiagnosticReport::class.java) {
    override val emptyTest: DiagnosticReport? = null

    override val jsonObjectTests: List<Pair<DiagnosticReport?, String>> =
            listOf(
                    Pair(
                            DiagnosticReport(
                                    "dummyEntityId",
                                    "dummyAccountEntityId",
                                    1,
                                    2,
                                    "dummyId",
                                    listOf(
                                            TIdentifier.obj
                                    ),
                                    "dummyStatus",
                                    3,
                                    4,
                                    TCodeableConcept.obj,
                                    TReference.obj,
                                    TPerformer.obj,
                                    listOf(
                                            TObservation.obj
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
                                    "effectivedatetime":3,
                                    "issued":4,
                                    "code":${TCodeableConcept.json},
                                    "subject":${TReference.json},
                                    "performer":${TPerformer.json},
                                    "result":[
                                        ${TObservation.json}
                                    ]
                                }
                            """.trimIndent()
                    )
            )
}