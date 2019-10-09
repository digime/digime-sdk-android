/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.music

import me.digi.sdk.models.music.Artist
import me.digi.sdk.models.objects.ModelTestObject
import me.digi.sdk.models.objects.TMediaResource

object TArtist : ModelTestObject<Artist>(
        Artist(
                "dummyAccountEntityId",
                "dummyEntityId",
                "dummyId",
                "dummyName",
                setOf(
                        TMediaResource.obj
                )
        ),
        """
        {
            "accountentityid":"dummyAccountEntityId",
            "entityid":"dummyEntityId",
            "id":"dummyId",
            "name":"dummyName",
            "resources":[
                ${TMediaResource.json}
            ]
        }
        """.trimIndent()
)