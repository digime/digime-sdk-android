/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.secondary

import me.digi.sdk.models.medical.mob2.secondary.Ratio
import me.digi.sdk.models.objects.ModelTestObject

object TRatio : ModelTestObject<Ratio>(
        Ratio(
                TQuantity.obj,
                TQuantity.obj
        ),
        """
        {
            "numerator":${TQuantity.json},
            "denominator":${TQuantity.json}
        }
        """.trimIndent()
)