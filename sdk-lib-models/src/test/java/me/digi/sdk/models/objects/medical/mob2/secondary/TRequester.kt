/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.secondary

import me.digi.sdk.models.medical.mob2.secondary.Requester
import me.digi.sdk.models.objects.ModelTestObject

object TRequester : ModelTestObject<Requester>(
        Requester(
                TReference.obj,
                TOrganization.obj
        ),
        """
        {
            "agent":${TReference.json},
            "onbehalfof":${TOrganization.json}
        }
        """.trimIndent()
)