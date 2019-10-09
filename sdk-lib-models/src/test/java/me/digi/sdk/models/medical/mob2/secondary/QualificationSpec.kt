/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.secondary.TQualification

class QualificationSpec : ModelTest<Qualification>(Qualification::class.java) {
    override val emptyTest: Qualification? = Qualification(
            null,
            null,
            null,
            null
    )

    override val jsonObjectTests: List<Pair<Qualification?, String>> =
            listOf(
                    Pair(
                            TQualification.obj,
                            TQualification.json
                    )
            )
}