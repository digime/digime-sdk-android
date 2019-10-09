/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.secondary.TCommunication

class CommunicationSpec : ModelTest<Communication>(Communication::class.java) {
    override val emptyTest: Communication? = Communication(
            null,
            null
    )

    override val jsonObjectTests: List<Pair<Communication?, String>> =
            listOf(
                    Pair(
                            TCommunication.obj,
                            TCommunication.json
                    )
            )
}