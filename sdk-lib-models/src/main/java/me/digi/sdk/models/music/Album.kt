/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.music

import com.squareup.moshi.Json
import me.digi.sdk.models.ItemDetails
import me.digi.sdk.models.MediaResource

data class Album(
        @Json(name = "accountentityid")
        val accountEntityId: String?,

        @Json(name = "entityid")
        val entityId: String,

        @Json(name = "id")
        val apiId: String?,

        @Json(name = "link")
        val link: String?,

        @Json(name = "name")
        val name: String?,

        @Json(name = "type")
        val type: String?,

        @Json(name = "artists")
        val artists: List<Artist>?,

        @Json(name = "resources")
        val resources: Set<MediaResource>?,

        // not provided in Simplified context
        @Json(name = "releasedate")
        val releaseDate: Long?,

        // not provided in Simplified context
        @Json(name = "popularity")
        val popularity: Int?,

        // not provided in Simplified context
        @Json(name = "genres")
        val genres: List<String>?,

        // not provided in Simplified context
        @Json(name = "tracks")
        val tracks: List<Track>?
) : ItemDetails.ContentItemDetails