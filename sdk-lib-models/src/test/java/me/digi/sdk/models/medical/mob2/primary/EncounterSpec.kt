/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.primary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.primary.TEncounter

class EncounterSpec : ModelTest<Encounter>(Encounter::class.java) {
    override val emptyTest: Encounter? = null

    override val jsonObjectTests: List<Pair<Encounter?, String>> =
            listOf(
                    Pair(
                            TEncounter.obj,
                            TEncounter.json
                    )
            )
}