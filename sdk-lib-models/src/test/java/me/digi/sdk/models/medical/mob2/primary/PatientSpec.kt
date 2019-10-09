/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.primary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.primary.TPatient

class PatientSpec : ModelTest<Patient>(Patient::class.java) {
    override val emptyTest: Patient? = null

    override val jsonObjectTests: List<Pair<Patient?, String>> =
            listOf(
                    Pair(
                            TPatient.obj,
                            TPatient.json
                    )
            )
}