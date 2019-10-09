/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.primary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.secondary.*

class MedicationDispenseSpec : ModelTest<MedicationDispense>(MedicationDispense::class.java) {
    override val emptyTest: MedicationDispense? = null

    override val jsonObjectTests: List<Pair<MedicationDispense?, String>> =
            listOf(
                    Pair(
                            MedicationDispense(
                                    "dummyEntityId",
                                    "dummyAccountEntityId",
                                    1,
                                    2,
                                    "dummyId",
                                    listOf(
                                            TIdentifier.obj
                                    ),
                                    "dummyStatus",
                                    TCodeableConcept.obj,
                                    listOf(
                                            TPerformer.obj
                                    ),
                                    3,
                                    4,
                                    listOf(
                                            TAnnotation.obj
                                    ),
                                    listOf(
                                            TDosage.obj
                                    ),
                                    true
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
                                    "medicationcodeableconcept":${TCodeableConcept.json},
                                    "performer":[
                                        ${TPerformer.json}
                                    ],
                                    "whenprepared":3,
                                    "whenhandedover":4,
                                    "note":[
                                        ${TAnnotation.json}
                                    ],
                                    "dosageinstruction":[
                                        ${TDosage.json}
                                    ],
                                    "notdone":true
                                }
                            """.trimIndent()
                    )
            )
}