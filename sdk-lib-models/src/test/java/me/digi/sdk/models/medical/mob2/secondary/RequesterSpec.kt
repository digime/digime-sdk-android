/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.secondary.TRequester

class RequesterSpec : ModelTest<Requester>(Requester::class.java) {
    override val emptyTest: Requester? = Requester(
            null,
            null
    )

    override val jsonObjectTests: List<Pair<Requester?, String>> =
            listOf(
                    Pair(
                            TRequester.obj,
                            TRequester.json
                    )
            )
}