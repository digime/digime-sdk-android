/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.primary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.secondary.*

class MedicationStatementSpec : ModelTest<MedicationStatement>(MedicationStatement::class.java) {
    override val emptyTest: MedicationStatement? = null

    override val jsonObjectTests: List<Pair<MedicationStatement?, String>> =
            listOf(
                    Pair(
                            MedicationStatement(
                                    "dummyEntityId",
                                    "dummyAccountEntityId",
                                    1,
                                    2,
                                    "dummyId",
                                    listOf(
                                            TIdentifier.obj
                                    ),
                                    "dummyStatus",
                                    TReference.obj,
                                    TPeriod.obj,
                                    TCodeableConcept.obj,
                                    listOf(
                                            TDosage.obj
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
                                    "informationsource":${TReference.json},
                                    "effectiveperiod":${TPeriod.json},
                                    "medicationcodeableconcept":${TCodeableConcept.json},
                                    "dosage":[
                                        ${TDosage.json}
                                    ]
                                }
                            """.trimIndent()
                    )
            )
}