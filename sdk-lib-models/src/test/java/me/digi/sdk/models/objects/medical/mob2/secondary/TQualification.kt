/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.secondary

import me.digi.sdk.models.medical.mob2.secondary.Qualification
import me.digi.sdk.models.objects.ModelTestObject

object TQualification : ModelTestObject<Qualification>(
        Qualification(
                listOf(
                        TIdentifier.obj
                ),
                TCodeableConcept.obj,
                TPeriod.obj,
                TOrganization.obj
        ),
        """
        {
            "identifier":[
                ${TIdentifier.json}
            ],
            "code":${TCodeableConcept.json},
            "period":${TPeriod.json},
            "issuer":${TOrganization.json}
        }
        """.trimIndent()
)