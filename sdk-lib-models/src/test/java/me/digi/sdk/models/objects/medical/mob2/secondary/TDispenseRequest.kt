/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.secondary

import me.digi.sdk.models.medical.mob2.secondary.DispenseRequest
import me.digi.sdk.models.objects.ModelTestObject

object TDispenseRequest : ModelTestObject<DispenseRequest>(
        DispenseRequest(
                TPeriod.obj,
                1,
                TQuantity.obj,
                TQuantity.obj
        ),
        """
        {
            "validityperiod":${TPeriod.json},
            "numberofrepeatsallowed":1,
            "quantity":${TQuantity.json},
            "expectedsupplyduration":${TQuantity.json}
        }
        """.trimIndent()
)