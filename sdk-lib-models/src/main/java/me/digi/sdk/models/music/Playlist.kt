/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.music

import com.squareup.moshi.Json
import me.digi.sdk.models.MediaResource

data class Playlist(
        @Json(name = "entityid")
        val entityId: String,

        @Json(name = "accountentityid")
        val accountEntityId: String?,

        @Json(name = "id")
        val apiId: String?,

        @Json(name = "name")
        val name: String?,

        @Json(name = "description")
        val description: String?,

        @Json(name = "followerscount")
        val followersCount: Int?,

        @Json(name = "public")
        val public: Boolean = false,

        @Json(name = "collaborative")
        val collaborative: Boolean = false,

        @Json(name = "owner")
        val owner: Actor?,

        @Json(name = "link")
        val link: String?,

        @Json(name = "tracks")
        val tracks: List<PlaylistTrack>?,

        @Json(name = "resources")
        val resources: Set<MediaResource>?
)