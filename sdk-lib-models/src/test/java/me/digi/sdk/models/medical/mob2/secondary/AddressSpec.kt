/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.secondary.TAddress

class AddressSpec : ModelTest<Address>(Address::class.java) {
    override val emptyTest: Address? = Address(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
    )

    override val jsonObjectTests: List<Pair<Address?, String>> =
            listOf(
                    Pair(
                            TAddress.obj,
                            TAddress.json
                    )
            )
}