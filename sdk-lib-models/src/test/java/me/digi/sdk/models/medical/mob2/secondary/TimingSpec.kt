/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.secondary.TTiming

class TimingSpec : ModelTest<Timing>(Timing::class.java) {
    override val emptyTest: Timing? = Timing(
            null,
            null
    )

    override val jsonObjectTests: List<Pair<Timing?, String>> =
            listOf(
                    Pair(
                            TTiming.obj,
                            TTiming.json
                    )
            )
}