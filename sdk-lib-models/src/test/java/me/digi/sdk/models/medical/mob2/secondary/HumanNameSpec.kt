/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.secondary.THumanName

class HumanNameSpec : ModelTest<HumanName>(HumanName::class.java) {
    override val emptyTest: HumanName? = HumanName(
            null,
            null,
            null,
            null,
            null,
            null,
            null
    )

    override val jsonObjectTests: List<Pair<HumanName?, String>> =
            listOf(
                    Pair(
                            THumanName.obj,
                            THumanName.json
                    )
            )
}