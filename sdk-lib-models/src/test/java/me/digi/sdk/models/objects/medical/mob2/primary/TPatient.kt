/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.primary

import me.digi.sdk.models.medical.mob2.primary.Patient
import me.digi.sdk.models.objects.ModelTestObject
import me.digi.sdk.models.objects.medical.mob2.secondary.*

object TPatient : ModelTestObject<Patient>(
        Patient(
                "dummyentityid",
                "dummyaccountentityid",
                1,
                2,
                "dummyid",
                listOf(
                        TIdentifier.obj
                ),
                true,
                listOf(
                        THumanName.obj
                ),
                listOf(
                        TContactPoint.obj
                ),
                "dummygender",
                3,
                true,
                4,
                listOf(
                        TAddress.obj
                ),
                TCodeableConcept.obj,
                listOf(
                        TContact.obj
                ),
                listOf(
                        TCommunication.obj
                )
        ),
        """
        {
            "entityid":"dummyentityid",
            "accountentityid":"dummyaccountentityid",
            "createddate":1,
            "updateddate":2,
            "id":"dummyid",
            "identifier":[
                ${TIdentifier.json}
            ],
            "active":true,
            "name":[
                ${THumanName.json}
            ],
            "telecom":[
                ${TContactPoint.json}
            ],
            "gender":"dummygender",
            "birthdate":3,
            "deceasedboolean":true,
            "deceaseddatetime":4,
            "address":[
                ${TAddress.json}
            ],
            "maritalstatus":${TCodeableConcept.json},
            "contact":[
                ${TContact.json}
            ],
            "communication":[
                ${TCommunication.json}
            ]
        }
        """.trimIndent()
)