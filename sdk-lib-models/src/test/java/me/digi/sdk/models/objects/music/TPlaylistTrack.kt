/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.objects.music

import me.digi.sdk.models.music.PlaylistTrack
import me.digi.sdk.models.objects.ModelTestObject

object TPlaylistTrack : ModelTestObject<PlaylistTrack>(
        PlaylistTrack(
                "dummyAccountEntityId",
                "dummyEntityId",
                "dummyId",
                1,
                TActor.obj,
                TTrack.obj
        ),
        """
        {
            "accountentityid":"dummyAccountEntityId",
            "entityid":"dummyEntityId",
            "id":"dummyId",
            "createddate":1,
            "addedby":${TActor.json},
            "track":${TTrack.json}
        }
        """.trimIndent()
)