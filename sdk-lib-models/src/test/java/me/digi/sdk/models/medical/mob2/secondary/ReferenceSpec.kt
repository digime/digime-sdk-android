/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.secondary.TReference

class ReferenceSpec : ModelTest<Reference>(Reference::class.java) {
    override val emptyTest: Reference? = Reference(
            null,
            null,
            null,
            null,
            null,
            null,
            null
    )

    override val jsonObjectTests: List<Pair<Reference?, String>> =
            listOf(
                    Pair(
                            TReference.obj,
                            TReference.json
                    )
            )
}