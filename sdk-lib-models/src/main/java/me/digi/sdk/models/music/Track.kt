/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.music

import com.squareup.moshi.Json
import me.digi.sdk.models.ItemDetails

data class Track(
        @Json(name = "accountentityid")
        val accountEntityId: String?,

        @Json(name = "entityid")
        val entityId: String,

        @Json(name = "id")
        val apiId: String?,

        @Json(name = "name")
        val name: String?,

        @Json(name = "duration")
        val duration: Int?,

        @Json(name = "discnumber")
        val discNumber: Int?,

        @Json(name = "number")
        val trackNumber: Int?,

        @Json(name = "link")
        val link: String?,

        @Json(name = "artists")
        val artists: List<Artist>?,

        @Json(name = "explicit")
        val explicit: Boolean?,

        // *** Fields not provided when simplified Track object (eg. PlayHistory context)
        @Json(name = "album")
        val album: Album?,

        @Json(name = "popularity")
        val popularity: Int?
        // ***
) : ItemDetails.ContentItemDetails {
    /**  Spotify has a concept of "simplified" and "full" form objects
    https://developer.spotify.com/web-api/object-model/#track-object-simplified
    Check comment for [album] and [popularity] fields*/
    val isSimplifiedTrack: Boolean = album == null
}