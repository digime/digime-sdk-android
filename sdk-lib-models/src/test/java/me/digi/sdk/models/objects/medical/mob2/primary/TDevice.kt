/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.primary

import me.digi.sdk.models.medical.mob2.primary.Device
import me.digi.sdk.models.objects.ModelTestObject
import me.digi.sdk.models.objects.medical.mob2.secondary.TCodeableConcept
import me.digi.sdk.models.objects.medical.mob2.secondary.TIdentifier
import me.digi.sdk.models.objects.medical.mob2.secondary.TUdi

object TDevice : ModelTestObject<Device>(
        Device(
                "dummyEntityId",
                "dummyAccountEntityId",
                1,
                2,
                "dummyId",
                listOf(
                        TIdentifier.obj
                ),
                TUdi.obj,
                "dummyModel",
                3,
                "dummyStatus",
                TCodeableConcept.obj
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
            "udi":${TUdi.json},
            "model":"dummyModel",
            "expirationdate":3,
            "status":"dummyStatus",
            "type":${TCodeableConcept.json}
        }
        """.trimIndent()
)