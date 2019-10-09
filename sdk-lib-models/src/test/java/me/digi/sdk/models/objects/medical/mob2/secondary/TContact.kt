/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.secondary

import me.digi.sdk.models.medical.mob2.secondary.Contact
import me.digi.sdk.models.objects.ModelTestObject

object TContact : ModelTestObject<Contact>(
        Contact(
                listOf(
                        TCodeableConcept.obj
                ),
                THumanName.obj,
                listOf(
                        TContactPoint.obj
                ),
                TAddress.obj,
                "dummyGender",
                TOrganization.obj,
                TPeriod.obj
        ),
        """
        {
            "relationship":[
                ${TCodeableConcept.json}
            ],
            "name":${THumanName.json},
            "telecom":[
                ${TContactPoint.json}
            ],
            "address":${TAddress.json},
            "gender":"dummyGender",
            "organization":${TOrganization.json},
            "period":${TPeriod.json}
        }
        """.trimIndent()
)