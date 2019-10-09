/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.secondary

import me.digi.sdk.models.medical.mob2.secondary.MedicationPackageContent
import me.digi.sdk.models.objects.ModelTestObject
import me.digi.sdk.models.objects.medical.mob2.primary.TMedication

object TMedicationPackageContent : ModelTestObject<MedicationPackageContent>(
        MedicationPackageContent(
                TCodeableConcept.obj,
                TMedication.obj,
                TQuantity.obj
        ),
        """
        {
            "itemcodeableconcept":${TCodeableConcept.json},
            "itemReference":${TMedication.json},
            "amount":${TQuantity.json}
        }
        """.trimIndent()
)