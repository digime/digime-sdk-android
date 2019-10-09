/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.music

import me.digi.sdk.models.music.Album
import me.digi.sdk.models.music.Track
import me.digi.sdk.models.objects.ModelTestObject
import me.digi.sdk.models.objects.TMediaResource

object TTrack : ModelTestObject<Track>(
        Track(
                "dummyaccountentityid",
                "dummyentityid",
                "dummyid",
                "dummyname",
                1,
                2,
                3,
                "dummylink",
                listOf(
                        TArtist.obj
                ),
                true,
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
                        emptyList()
                ),
                4
        ),
        """
        {
            "accountentityid":"dummyaccountentityid",
            "entityid":"dummyentityid",
            "id":"dummyid",
            "name":"dummyname",
            "duration":1,
            "discnumber":2,
            "number":3,
            "link":"dummylink",
            "artists":[
                ${TArtist.json}
            ],
            "explicit":true,
            "album":{
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
                "tracks":[]
            },
            "popularity":4
        }
        """.trimIndent()
)