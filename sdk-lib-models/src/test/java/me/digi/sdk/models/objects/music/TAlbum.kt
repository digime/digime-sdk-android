/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.music

import me.digi.sdk.models.music.Album
import me.digi.sdk.models.objects.ModelTestObject
import me.digi.sdk.models.objects.TMediaResource

object TAlbum : ModelTestObject<Album>(
        Album(
                "dummyAccountEntityId",
                "dummyEntityId",
                "dummyId",
                "dummyLink",
                "dummyName",
                "dummyType",
                listOf(
                        TArtist.obj
                ),
                setOf(
                        TMediaResource.obj
                ),
                1,
                2,
                listOf("dummyGenre1", "dummyGenre2"),
                listOf(
                        TTrack.obj
                )
        ),
        """
        {
            "accountentityid":"dummyAccountEntityId",
            "entityid":"dummyEntityId",
            "id":"dummyId",
            "link":"dummyLink",
            "name":"dummyName",
            "type":"dummyType",
            "artists":[
                ${TArtist.json}
            ],
            "resources":[
                ${TMediaResource.json}
            ],
            "releasedate":1,
            "popularity":2,
            "genres":["dummyGenre1","dummyGenre2"],
            "tracks":[
                ${TTrack.json}
            ]
        }
        """.trimIndent()
)