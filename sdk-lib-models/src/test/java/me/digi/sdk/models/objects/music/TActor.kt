/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.music

import me.digi.sdk.models.music.Actor
import me.digi.sdk.models.objects.ModelTestObject
import me.digi.sdk.models.objects.TMediaResource

object TActor : ModelTestObject<Actor>(
        Actor(
                "dummyAccountEntityId",
                "dummyEntityId",
                "dummyId",
                "dummyLink",
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
            "link":"dummyLink",
            "name":"dummyName",
            "resources":[
                ${TMediaResource.json}
            ]
        }
        """.trimIndent()
)