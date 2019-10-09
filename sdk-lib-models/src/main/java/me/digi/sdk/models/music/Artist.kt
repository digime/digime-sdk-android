/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.music

import com.squareup.moshi.Json
import me.digi.sdk.models.ItemDetails
import me.digi.sdk.models.MediaResource

abstract class IArtist : ItemDetails.ContentItemDetails {
    abstract val accountEntityId: String?
    abstract val entityId: String
    abstract val apiId: String?
    abstract val name: String?
    abstract val resources: Set<MediaResource>?
}

data class Artist(
        @Json(name = "accountentityid")
        override val accountEntityId: String?,

        @Json(name = "entityid")
        override val entityId: String,

        @Json(name = "id")
        override val apiId: String?,

        @Json(name = "name")
        override val name: String?,

        @Json(name = "resources")
        override val resources: Set<MediaResource>?
) : IArtist()

data class FollowedArtist(
        @Json(name = "accountentityid")
        override val accountEntityId: String?,

        @Json(name = "entityid")
        override val entityId: String,

        @Json(name = "id")
        override val apiId: String?,

        @Json(name = "name")
        override val name: String?,

        @Json(name = "resources")
        override val resources: Set<MediaResource>?
) : IArtist()