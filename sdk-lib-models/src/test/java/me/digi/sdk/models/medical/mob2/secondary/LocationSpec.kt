/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.secondary.TLocation

class LocationSpec : ModelTest<Location>(Location::class.java) {
    override val emptyTest: Location? = Location(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
    )

    override val jsonObjectTests: List<Pair<Location?, String>> =
            listOf(
                    Pair(
                            TLocation.obj,
                            TLocation.json
                    )
            )
}