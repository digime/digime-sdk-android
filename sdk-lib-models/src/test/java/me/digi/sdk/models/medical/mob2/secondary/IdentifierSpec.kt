/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.secondary.TIdentifier

class IdentifierSpec : ModelTest<Identifier>(Identifier::class.java) {
    override val emptyTest: Identifier? = Identifier(
            null,
            null,
            null,
            null,
            null,
            null
    )

    override val jsonObjectTests: List<Pair<Identifier?, String>> =
            listOf(
                    Pair(
                            TIdentifier.obj,
                            TIdentifier.json
                    )
            )
}