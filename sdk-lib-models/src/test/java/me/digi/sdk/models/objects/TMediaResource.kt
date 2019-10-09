/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects

import me.digi.sdk.models.MediaResource

object TMediaResource : ModelTestObject<MediaResource>(
        MediaResource(
                TAspectRatio.obj,
                1,
                2,
                "application/dummyType",
                "dummyResize",
                3,
                "dummyUrl"
        ),
        """
        {
            "aspectratio":${TAspectRatio.json},
            "height":1,
            "width":2,
            "mimetype":"application/dummyType",
            "resize":"dummyResize",
            "type":3,
            "url":"dummyUrl"
        }
        """.trimIndent()
)