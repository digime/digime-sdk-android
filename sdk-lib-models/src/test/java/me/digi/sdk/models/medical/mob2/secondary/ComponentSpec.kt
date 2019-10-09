/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.secondary.TComponent

class ComponentSpec : ModelTest<Component>(Component::class.java) {
    override val emptyTest: Component? = Component(
            null,
            null
    )

    override val jsonObjectTests: List<Pair<Component?, String>> =
            listOf(
                    Pair(
                            TComponent.obj,
                            TComponent.json
                    )
            )
}