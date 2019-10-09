/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.finance

import me.digi.sdk.models.ModelTest

class AmountSpec : ModelTest<Amount>(Amount::class.java) {
    override val emptyTest: Amount? = Amount(null, null)

    override val jsonObjectTests: List<Pair<Amount?, String>> =
            listOf(
                    Pair(
                            Amount(1.5F, "GBP"),
                            """{"amount": 1.5, "currency": "GBP"}"""
                    ),
                    Pair(
                            Amount(1F, "GBP"),
                            """{"amount": 1, "currency": "GBP"}"""
                    ),
                    Pair(
                            Amount(null, "GBP"),
                            """{"currency": "GBP"}"""
                    ),
                    Pair(
                            Amount(1.5F, null),
                            """{"amount": 1.5}"""
                    )
            )
}