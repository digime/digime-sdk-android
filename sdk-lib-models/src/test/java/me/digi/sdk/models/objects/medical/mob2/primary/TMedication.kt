/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.primary

import me.digi.sdk.models.medical.mob2.primary.Medication
import me.digi.sdk.models.medical.mob2.secondary.MedicationPackage
import me.digi.sdk.models.medical.mob2.secondary.MedicationPackageContent
import me.digi.sdk.models.objects.ModelTestObject
import me.digi.sdk.models.objects.medical.mob2.secondary.TCodeableConcept
import me.digi.sdk.models.objects.medical.mob2.secondary.TQuantity

object TMedication : ModelTestObject<Medication>(
        Medication(
                "dummyEntityId",
                "dummyAccountEntityId",
                1,
                2,
                "dummyId",
                TCodeableConcept.obj,
                "dummyStatus",
                TCodeableConcept.obj,
                MedicationPackage(
                        TCodeableConcept.obj,
                        listOf(
                                MedicationPackageContent(
                                        TCodeableConcept.obj,
                                        Medication(
                                                "dummyEntityId",
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null
                                        ),
                                        TQuantity.obj
                                )
                        )
                )
        ),
        """
        {
            "entityid":"dummyEntityId",
            "accountentityid":"dummyAccountEntityId",
            "createddate":1,
            "updateddate":2,
            "id":"dummyId",
            "code":${TCodeableConcept.json},
            "status":"dummyStatus",
            "form":${TCodeableConcept.json},
            "package":{
                "container":${TCodeableConcept.json},
                "content":[
                    {
                        "itemcodeableconcept":${TCodeableConcept.json},
                        "itemReference":{
                            "entityid":"dummyEntityId"
                        },
                        "amount":${TQuantity.json}
                    }
                ]
            }
        }
        """.trimIndent()
)