/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.music

import com.squareup.moshi.Json

data class PlaylistTrack(
        @Json(name = "accountentityid")
        val accountEntityId: String?,

        @Json(name = "entityid")
        val entityId: String,

        @Json(name = "id")
        val id: String?,

        @Json(name = "createddate")
        val addedAt: Long?,

        // null when Playlist created by Spotify
        @Json(name = "addedby")
        val addedBy: Actor?,

        @Json(name = "track")
        val track: Track?
)