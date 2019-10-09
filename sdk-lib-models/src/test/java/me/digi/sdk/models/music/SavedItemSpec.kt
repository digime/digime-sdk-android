/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.music

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.music.TAlbum
import me.digi.sdk.models.objects.music.TTrack

class SavedAlbumSpec : ModelTest<SavedAlbum>(SavedAlbum::class.java) {
    override val emptyTest: SavedAlbum? = null

    override val jsonObjectTests: List<Pair<SavedAlbum?, String>> =
            listOf(
                    Pair(
                            SavedAlbum(
                                    "dummyaccountentityid",
                                    "dummyentityid",
                                    1,
                                    TAlbum.obj
                            ),
                            """
                                {
                                    "accountentityid":"dummyaccountentityid",
                                    "entityid":"dummyentityid",
                                    "createddate":1,
                                    "album":${TAlbum.json}
                                }
                            """.trimIndent()
                    )
            )
}

class SavedTrackSpec : ModelTest<SavedTrack>(SavedTrack::class.java) {
    override val emptyTest: SavedTrack? = null

    override val jsonObjectTests: List<Pair<SavedTrack?, String>> =
            listOf(
                    Pair(
                            SavedTrack(
                                    "dummyaccountentityid",
                                    "dummyentityid",
                                    1,
                                    TTrack.obj
                            ),
                            """
                                {
                                    "accountentityid":"dummyaccountentityid",
                                    "entityid":"dummyentityid",
                                    "createddate":1,
                                    "track":${TTrack.json}
                                }
                            """.trimIndent()
                    )
            )
}