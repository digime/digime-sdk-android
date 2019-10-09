/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.secondary.TReaction

class ReactionSpec : ModelTest<Reaction>(Reaction::class.java) {
    override val emptyTest: Reaction? = Reaction(
            null,
            null,
            null,
            null
    )

    override val jsonObjectTests: List<Pair<Reaction?, String>> =
            listOf(
                    Pair(
                            TReaction.obj,
                            TReaction.json
                    )
            )
}