/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.secondary

import me.digi.sdk.models.medical.mob2.secondary.Component
import me.digi.sdk.models.objects.ModelTestObject

object TComponent : ModelTestObject<Component>(
        Component(
                TCodeableConcept.obj,
                TQuantity.obj
        ),
        """
        {
            "code":${TCodeableConcept.json},
            "valuequantity":${TQuantity.json}
        }
        """.trimIndent()
)