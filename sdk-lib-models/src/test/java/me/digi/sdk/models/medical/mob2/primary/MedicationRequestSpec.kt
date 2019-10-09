/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.primary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.primary.TMedication
import me.digi.sdk.models.objects.medical.mob2.secondary.*

class MedicationRequestSpec : ModelTest<MedicationRequest>(MedicationRequest::class.java) {
    override val emptyTest: MedicationRequest? = null

    override val jsonObjectTests: List<Pair<MedicationRequest?, String>> =
            listOf(
                    Pair(
                            MedicationRequest(
                                    "dummyEntityId",
                                    "dummyAccountEntityId",
                                    1,
                                    2,
                                    "dummyId",
                                    listOf(
                                            TIdentifier.obj
                                    ),
                                    "dummyStatus",
                                    TMedication.obj,
                                    3,
                                    TRequester.obj,
                                    listOf(
                                            TAnnotation.obj
                                    ),
                                    listOf(
                                            TDosage.obj
                                    ),
                                    TDispenseRequest.obj
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
                                    "medicationreference":${TMedication.json},
                                    "authoredon":3,
                                    "requester":${TRequester.json},
                                    "note":[
                                        ${TAnnotation.json}
                                    ],
                                    "dosageinstruction":[
                                        ${TDosage.json}
                                    ],
                                    "dispenserequest":${TDispenseRequest.json}
                                }
                            """.trimIndent()
                    )
            )
}