/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.secondary.TCodeableConcept

class CodeableConceptSpec : ModelTest<CodeableConcept>(CodeableConcept::class.java) {
    override val emptyTest: CodeableConcept? = CodeableConcept(
            null,
            null
    )

    override val jsonObjectTests: List<Pair<CodeableConcept?, String>> =
            listOf(
                    Pair(
                            TCodeableConcept.obj,
                            TCodeableConcept.json
                    )
            )
}