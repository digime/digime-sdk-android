/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.secondary.TPerformer

class PerformerSpec : ModelTest<Performer>(Performer::class.java) {
    override val emptyTest: Performer? = Performer(
            null,
            null,
            null
    )

    override val jsonObjectTests: List<Pair<Performer?, String>> =
            listOf(
                    Pair(
                            TPerformer.obj,
                            TPerformer.json
                    )
            )
}