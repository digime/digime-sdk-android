/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.medical.mob2.secondary

import me.digi.sdk.models.medical.mob2.secondary.Coding
import me.digi.sdk.models.objects.ModelTestObject

object TCoding : ModelTestObject<Coding>(
        Coding(
                "dummyCode",
                "dummyDisplay",
                "dummySystem",
                "dummyVersion"
        ),
        """
        {
            "code": "dummyCode",
            "display": "dummyDisplay",
            "system": "dummySystem",
            "version": "dummyVersion"
        }
        """.trimIndent()
)