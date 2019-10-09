/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.music

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.music.TTrack
import org.junit.Assert
import org.junit.Test

class TrackSpec : ModelTest<Track>(Track::class.java) {
    override val emptyTest: Track? = null

    override val jsonObjectTests: List<Pair<Track?, String>> =
            listOf(
                    Pair(
                            TTrack.obj,
                            TTrack.json
                    )
            )

    @Test
    fun `when album is null, isSimplifiedTrack should be true`() {
        Assert.assertTrue(jsonObjectTests[0].first!!.copy(album = null).isSimplifiedTrack)
    }

    @Test
    fun `when album is not null, isSimplifiedTrack should be false`() {
        val album = Album(
                null,
                "dummyEntityId",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        )
        Assert.assertFalse(jsonObjectTests[0].first!!.copy(album = album).isSimplifiedTrack)
    }
}