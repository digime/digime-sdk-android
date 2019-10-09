/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.music

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.music.TTrack

class PlayHistorySpec : ModelTest<PlayHistory>(PlayHistory::class.java) {
    override val emptyTest: PlayHistory? = null

    override val jsonObjectTests: List<Pair<PlayHistory?, String>> =
            listOf(
                    Pair(
                            PlayHistory(
                                    "dummyAccountEntityId",
                                    "dummyEntityId",
                                    1,
                                    TTrack.obj
                            ),
                            """
                                {
                                    "accountentityid":"dummyAccountEntityId",
                                    "entityid":"dummyEntityId",
                                    "createddate":1,
                                    "track":${TTrack.json}
                                }
                            """.trimIndent()
                    )
            )
}