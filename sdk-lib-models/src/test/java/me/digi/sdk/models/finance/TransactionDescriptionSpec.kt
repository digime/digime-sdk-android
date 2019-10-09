/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.finance

import me.digi.sdk.models.ModelTest

class TransactionDescriptionSpec : ModelTest<TransactionDescription>(TransactionDescription::class.java) {
    override val emptyTest: TransactionDescription? = TransactionDescription(null, null)

    override val jsonObjectTests: List<Pair<TransactionDescription?, String>> =
            listOf(
                    Pair(
                            TransactionDescription("dummyOriginal", "dummySimple"),
                            """{"original": "dummyOriginal", "simple": "dummySimple"}"""
                    ),
                    Pair(
                            TransactionDescription("dummyOriginal", null),
                            """{"original": "dummyOriginal"}"""
                    ),
                    Pair(
                            TransactionDescription(null, "dummySimple"),
                            """{"simple": "dummySimple"}"""
                    )
            )
}