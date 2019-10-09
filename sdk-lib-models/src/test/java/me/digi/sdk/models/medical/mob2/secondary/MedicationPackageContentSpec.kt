/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.secondary.TMedicationPackageContent

class MedicationPackageContentSpec : ModelTest<MedicationPackageContent>(MedicationPackageContent::class.java) {
    override val emptyTest: MedicationPackageContent? = MedicationPackageContent(
            null,
            null,
            null
    )

    override val jsonObjectTests: List<Pair<MedicationPackageContent?, String>> =
            listOf(
                    Pair(
                            TMedicationPackageContent.obj,
                            TMedicationPackageContent.json
                    )
            )
}