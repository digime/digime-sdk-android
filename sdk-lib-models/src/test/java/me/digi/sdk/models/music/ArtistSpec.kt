/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.music

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.music.TArtist
import me.digi.sdk.models.objects.music.TFollowedArtist

class ArtistSpec : ModelTest<Artist>(Artist::class.java) {
    override val emptyTest: Artist? = null

    override val jsonObjectTests: List<Pair<Artist?, String>> =
            listOf(
                    Pair(
                            TArtist.obj,
                            TArtist.json
                    )
            )
}

class FollowedArtistSpec : ModelTest<FollowedArtist>(FollowedArtist::class.java) {
    override val emptyTest: FollowedArtist? = null

    override val jsonObjectTests: List<Pair<FollowedArtist?, String>> =
            listOf(
                    Pair(
                            TFollowedArtist.obj,
                            TFollowedArtist.json
                    )
            )
}