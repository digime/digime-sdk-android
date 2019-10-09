/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.secondary.TPeriod

class PeriodSpec : ModelTest<Period>(Period::class.java) {
    override val emptyTest: Period? = Period(
            null,
            null
    )

    override val jsonObjectTests: List<Pair<Period?, String>> =
            listOf(
                    Pair(
                            TPeriod.obj,
                            TPeriod.json
                    )
            )
}