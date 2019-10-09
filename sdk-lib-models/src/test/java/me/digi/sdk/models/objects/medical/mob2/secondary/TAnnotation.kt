/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.secondary

import me.digi.sdk.models.medical.mob2.secondary.Annotation
import me.digi.sdk.models.objects.ModelTestObject

object TAnnotation : ModelTestObject<Annotation>(
        Annotation(
                TReference.obj,
                "dummyAuthorString",
                1,
                "dummyText"
        ),
        """
        {
            "authorreference":${TReference.json},
            "authorString":"dummyAuthorString",
            "time":1,
            "text":"dummyText"
        }
        """.trimIndent()
)