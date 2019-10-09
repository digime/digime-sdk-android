/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.secondary

import me.digi.sdk.models.medical.mob2.secondary.Location
import me.digi.sdk.models.objects.ModelTestObject

object TLocation : ModelTestObject<Location>(
        Location(
                "dummyId",
                listOf(
                        TIdentifier.obj
                ),
                "dummyStatus",
                "dummyName",
                listOf("dummyAlias1", "dummyAlias2"),
                "dummyDescription",
                TAddress.obj,
                TCodeableConcept.obj
        ),
        """
        {
            "id":"dummyId",
            "identifier":[
                ${TIdentifier.json}
            ],
            "status":"dummyStatus",
            "name":"dummyName",
            "alias":["dummyAlias1","dummyAlias2"],
            "description":"dummyDescription",
            "address":${TAddress.json},
            "physicaltype":${TCodeableConcept.json}
        }
        """.trimIndent()
)