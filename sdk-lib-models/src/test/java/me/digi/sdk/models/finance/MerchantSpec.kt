/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.finance

import me.digi.sdk.models.ModelTest

class MerchantSpec : ModelTest<Merchant>(Merchant::class.java) {
    override val emptyTest: Merchant? = Merchant(null, null)

    override val jsonObjectTests: List<Pair<Merchant?, String>> =
            listOf(
                    Pair(
                            Merchant("dummyId", "dummyName"),
                            """{"id": "dummyId", "name": "dummyName"}"""
                    ),
                    Pair(
                            Merchant("dummyId", null),
                            """{"id": "dummyId"}"""
                    ),
                    Pair(
                            Merchant(null, "dummyName"),
                            """{"name": "dummyName"}"""
                    )
            )
}