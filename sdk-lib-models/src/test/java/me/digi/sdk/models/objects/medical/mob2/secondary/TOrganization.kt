/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.secondary

import me.digi.sdk.models.medical.mob2.secondary.Organization
import me.digi.sdk.models.objects.ModelTestObject

object TOrganization : ModelTestObject<Organization>(
        Organization(
                "dummyId",
                listOf(
                        TIdentifier.obj
                ),
                true,
                listOf(
                        TCodeableConcept.obj
                )
        ),
        """
        {
            "id":"dummyId",
            "identifier":[
                ${TIdentifier.json}
            ],
            "active":true,
            "type":[
                ${TCodeableConcept.json}
            ]
        }
        """.trimIndent()
)