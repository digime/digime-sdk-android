/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.secondary

import me.digi.sdk.models.medical.mob2.secondary.Performer
import me.digi.sdk.models.objects.ModelTestObject

object TPerformer : ModelTestObject<Performer>(
        Performer(
                TReference.obj,
                TOrganization.obj,
                TCodeableConcept.obj
        ),
        """
        {
            "actor":${TReference.json},
            "onbehalfof":${TOrganization.json},
            "role":${TCodeableConcept.json}
        }
        """.trimIndent()
)