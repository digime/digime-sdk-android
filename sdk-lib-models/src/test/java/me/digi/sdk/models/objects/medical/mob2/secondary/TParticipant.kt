/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.secondary

import me.digi.sdk.models.medical.mob2.secondary.Participant
import me.digi.sdk.models.objects.ModelTestObject

object TParticipant : ModelTestObject<Participant>(
        Participant(
                TCodeableConcept.obj,
                TPeriod.obj,
                TReference.obj,
                "dummyRequired",
                "dummyStatus"
        ),
        """
        {
            "type":${TCodeableConcept.json},
            "period":${TPeriod.json},
            "individual":${TReference.json},
            "required":"dummyRequired",
            "status":"dummyStatus"
        }
        """.trimIndent()
)