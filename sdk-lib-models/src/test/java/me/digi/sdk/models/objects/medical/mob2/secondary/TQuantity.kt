/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.secondary

import me.digi.sdk.models.medical.mob2.secondary.Quantity
import me.digi.sdk.models.objects.ModelTestObject

object TQuantity : ModelTestObject<Quantity>(
        Quantity(
                1.5,
                "dummyUnit",
                "dummySystem",
                "dummyCode"
        ),
        """
        {
            "value":1.5,
            "unit":"dummyUnit",
            "system":"dummySystem",
            "code":"dummyCode"
        }
        """.trimIndent()
)