/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.secondary.TCoding

class CodingSpec : ModelTest<Coding>(Coding::class.java) {
    override val emptyTest: Coding? = Coding(
            null,
            null,
            null,
            null
    )

    override val jsonObjectTests: List<Pair<Coding?, String>> =
            listOf(
                    Pair(
                            TCoding.obj,
                            TCoding.json
                    )
            )
}