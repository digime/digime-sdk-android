/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.secondary.TPractitioner

class PractitionerSpec : ModelTest<Practitioner>(Practitioner::class.java) {
    override val emptyTest: Practitioner? = Practitioner(
            null,
            null,
            null,
            null,
            null,
            null
    )

    override val jsonObjectTests: List<Pair<Practitioner?, String>> =
            listOf(
                    Pair(
                            TPractitioner.obj,
                            TPractitioner.json
                    )
            )
}