/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.music

import com.squareup.moshi.Json
import me.digi.sdk.models.ItemDetails

abstract class SavedItem : ItemDetails.ContentItemDetails {
    abstract val accountEntityId: String?
    abstract val entityId: String
    abstract val addedAt: Long?
}

data class SavedAlbum(
        @Json(name = "accountentityid")
        override val accountEntityId: String?,

        @Json(name = "entityid")
        override val entityId: String,

        @Json(name = "createddate")
        override val addedAt: Long?,

        @Json(name = "album")
        val album: Album?
) : SavedItem()

data class SavedTrack(
        @Json(name = "accountentityid")
        override val accountEntityId: String?,

        @Json(name = "entityid")
        override val entityId: String,

        @Json(name = "createddate")
        override val addedAt: Long?,

        @Json(name = "track")
        val track: Track?
) : SavedItem()