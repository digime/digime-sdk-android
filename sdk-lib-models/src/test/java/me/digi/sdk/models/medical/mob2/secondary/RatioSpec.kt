/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.secondary.TRatio

class RatioSpec : ModelTest<Ratio>(Ratio::class.java) {
    override val emptyTest: Ratio? = Ratio(
            null,
            null
    )

    override val jsonObjectTests: List<Pair<Ratio?, String>> =
            listOf(
                    Pair(
                            TRatio.obj,
                            TRatio.json
                    )
            )
}