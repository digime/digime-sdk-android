/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.secondary.TReferenceRange

class ReferenceRangeSpec : ModelTest<ReferenceRange>(ReferenceRange::class.java) {
    override val emptyTest: ReferenceRange? = ReferenceRange(
            null,
            null,
            null,
            null,
            null,
            null
    )

    override val jsonObjectTests: List<Pair<ReferenceRange?, String>> =
            listOf(
                    Pair(
                            TReferenceRange.obj,
                            TReferenceRange.json
                    )
            )
}