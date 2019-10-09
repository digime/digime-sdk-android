/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.music

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.music.TAlbum

class AlbumSpec : ModelTest<Album>(Album::class.java) {
    override val emptyTest: Album? = null

    override val jsonObjectTests: List<Pair<Album?, String>> =
            listOf(
                    Pair(
                            TAlbum.obj,
                            TAlbum.json
                    )
            )
}