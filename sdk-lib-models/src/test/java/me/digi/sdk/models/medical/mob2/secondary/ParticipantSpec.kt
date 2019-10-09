/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.medical.mob2.secondary.TParticipant

class ParticipantSpec : ModelTest<Participant>(Participant::class.java) {
    override val emptyTest: Participant? = Participant(
            null,
            null,
            null,
            null,
            null
    )

    override val jsonObjectTests: List<Pair<Participant?, String>> =
            listOf(
                    Pair(
                            TParticipant.obj,
                            TParticipant.json
                    )
            )
}