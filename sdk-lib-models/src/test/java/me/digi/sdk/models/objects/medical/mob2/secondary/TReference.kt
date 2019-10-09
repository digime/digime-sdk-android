/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.secondary

import me.digi.sdk.models.medical.mob2.secondary.Reference
import me.digi.sdk.models.objects.ModelTestObject
import me.digi.sdk.models.objects.medical.mob2.primary.TDevice
import me.digi.sdk.models.objects.medical.mob2.primary.TPatient

object TReference : ModelTestObject<Reference>(
        Reference(
                "dummyReference",
                TIdentifier.obj,
                "dummyDisplay",
                TPractitioner.obj,
                TOrganization.obj,
                TPatient.obj,
                TDevice.obj
        ),
        """
        {
            "reference":"dummyReference",
            "identifier":${TIdentifier.json},
            "display":"dummyDisplay",
            "practitioner":${TPractitioner.json},
            "organization":${TOrganization.json},
            "patient":${TPatient.json},
            "device":${TDevice.json}
        }
        """.trimIndent()
)