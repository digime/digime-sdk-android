/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.primary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.primary.TMedication

class MedicationSpec : ModelTest<Medication>(Medication::class.java) {
    override val emptyTest: Medication? = null

    override val jsonObjectTests: List<Pair<Medication?, String>> =
            listOf(
                    Pair(
                            TMedication.obj,
                            TMedication.json
                    )
            )
}