/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.secondary.TQuantity

class QuantitySpec : ModelTest<Quantity>(Quantity::class.java) {
    override val emptyTest: Quantity? = Quantity(
            null,
            null,
            null,
            null
    )

    override val jsonObjectTests: List<Pair<Quantity?, String>> =
            listOf(
                    Pair(
                            TQuantity.obj,
                            TQuantity.json
                    )
            )
}