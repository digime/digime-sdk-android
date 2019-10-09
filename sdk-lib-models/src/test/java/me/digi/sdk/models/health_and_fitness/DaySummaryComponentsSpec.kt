/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.health_and_fitness

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.health_and_fitness.*

class GoalsSpec : ModelTest<Goals>(Goals::class.java) {
    override val emptyTest: Goals? = Goals(
            null,
            null,
            null,
            null,
            null
    )

    override val jsonObjectTests: List<Pair<Goals?, String>> =
            listOf(
                    Pair(
                            TGoals.obj,
                            TGoals.json
                    )
            )
}

class DistanceSpec : ModelTest<Distance>(Distance::class.java) {
    override val emptyTest: Distance? = Distance(
            null,
            null
    )

    override val jsonObjectTests: List<Pair<Distance?, String>> =
            listOf(
                    Pair(
                            TDistance.obj,
                            TDistance.json
                    )
            )
}
