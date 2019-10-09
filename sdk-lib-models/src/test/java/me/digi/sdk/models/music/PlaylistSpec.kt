/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.music

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.TMediaResource
import me.digi.sdk.models.objects.music.TActor
import me.digi.sdk.models.objects.music.TPlaylistTrack

class PlaylistSpec : ModelTest<Playlist>(Playlist::class.java) {
    override val emptyTest: Playlist? = null

    override val jsonObjectTests: List<Pair<Playlist?, String>> =
            listOf(
                    Pair(
                            Playlist(
                                    "dummyEntityId",
                                    "dummyAccountEntityId",
                                    "dummyId",
                                    "dummyName",
                                    "dummyDescription",
                                    1,
                                    true,
                                    true,
                                    TActor.obj,
                                    "dummyLink",
                                    listOf(
                                            TPlaylistTrack.obj
                                    ),
                                    setOf(
                                            TMediaResource.obj
                                    )
                            ),
                            """
                                {
                                    "entityid":"dummyEntityId",
                                    "accountentityid":"dummyAccountEntityId",
                                    "id":"dummyId",
                                    "name":"dummyName",
                                    "description":"dummyDescription",
                                    "followerscount":1,
                                    "public":true,
                                    "collaborative":true,
                                    "owner":${TActor.json},
                                    "link":"dummyLink",
                                    "tracks":[
                                        ${TPlaylistTrack.json}
                                    ],
                                    "resources":[
                                        ${TMediaResource.json}
                                    ]
                                }
                            """.trimIndent()
                    )
            )
}