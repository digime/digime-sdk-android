/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.music

import com.squareup.moshi.Json
import me.digi.sdk.models.MediaResource

/**
 * Actor responsible for creating Music content (eg. [Playlist], [PlaylistTrack])
 */
data class Actor(
        @Json(name = "accountentityid")
        val accountEntityId: String?,

        @Json(name = "entityid")
        val entityId: String,

        @Json(name = "id")
        val id: String?,

        @Json(name = "link")
        val link: String?,

        @Json(name = "name")
        val name: String?,

        @Json(name = "resources")
        val resources: Set<MediaResource>?
)