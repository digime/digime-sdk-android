/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.primary

import me.digi.sdk.models.medical.mob2.primary.Observation
import me.digi.sdk.models.objects.ModelTestObject
import me.digi.sdk.models.objects.medical.mob2.secondary.*

object TObservation : ModelTestObject<Observation>(
        Observation(
                "dummyEntityId",
                "dummyAccountEntityId",
                1,
                2,
                "dummyId",
                listOf(
                        TIdentifier.obj
                ),
                "dummyStatus",
                listOf(
                        TCodeableConcept.obj
                ),
                TCodeableConcept.obj,
                3,
                4,
                TReference.obj,
                TQuantity.obj,
                TCodeableConcept.obj,
                "dummyValueString",
                true,
                TRatio.obj,
                TCodeableConcept.obj,
                listOf(
                        TReferenceRange.obj
                ),
                "dummyComment",
                listOf(
                        TComponent.obj
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
            "category":[
                ${TCodeableConcept.json}
            ],
            "code":${TCodeableConcept.json},
            "effectivedatetime":3,
            "issued":4,
            "performer":${TReference.json},
            "valuequantity":${TQuantity.json},
            "valuecodeableconcept":${TCodeableConcept.json},
            "valuestring":"dummyValueString",
            "valueboolean":true,
            "valueratio":${TRatio.json},
            "interpretation":${TCodeableConcept.json},
            "referencerange":[
                ${TReferenceRange.json}
            ],
            "comment":"dummyComment",
            "component":[
                ${TComponent.json}
            ]
        }
        """.trimIndent()
)