/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.secondary

import me.digi.sdk.models.medical.mob2.secondary.Communication
import me.digi.sdk.models.objects.ModelTestObject

object TCommunication : ModelTestObject<Communication>(
        Communication(
                TCodeableConcept.obj,
                true
        ),
        """
        {
            "language":${TCodeableConcept.json},
            "preferred":true
        }
        """.trimIndent()
)