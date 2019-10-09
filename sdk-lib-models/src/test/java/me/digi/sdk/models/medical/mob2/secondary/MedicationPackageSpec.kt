/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.secondary.TCodeableConcept
import me.digi.sdk.models.objects.medical.mob2.secondary.TMedicationPackageContent

class MedicationPackageSpec : ModelTest<MedicationPackage>(MedicationPackage::class.java) {
    override val emptyTest: MedicationPackage? = MedicationPackage(
            null,
            null
    )

    override val jsonObjectTests: List<Pair<MedicationPackage?, String>> =
            listOf(
                    Pair(
                            MedicationPackage(
                                    TCodeableConcept.obj,
                                    listOf(
                                            TMedicationPackageContent.obj
                                    )
                            ),
                            """
                                {
                                    "container":${TCodeableConcept.json},
                                    "content":[
                                        ${TMedicationPackageContent.json}
                                    ]
                                }
                            """.trimIndent()
                    )
            )
}