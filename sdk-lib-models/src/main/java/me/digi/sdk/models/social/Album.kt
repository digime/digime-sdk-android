/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.social

import com.squareup.moshi.Json

data class Album(
        @Json(name = "baseid")
        val baseID: String?,

        @Json(name = "entityid")
        val entityId: String,

        @Json(name = "mediaalbumid")
        val mediaAlbumID: String?,

        @Json(name = "socialnetworkuserentityid")
        val socialNetworkUserEntityID: String?,

        @Json(name = "mediaalbumobjectid")
        val mediaAlbumObjectID: String?,

        @Json(name = "referenceentityid")
        val referenceEntityID: String?,

        @Json(name = "referenceentitytype")
        val referenceEntityType: Int = 0,

        @Json(name = "mediaentityid")
        val mediaEntityID: String?,

        @Json(name = "name")
        val name: String?,

        @Json(name = "description")
        val description: String?,

        @Json(name = "createddate")
        val createdDate: Long = 0,

        @Json(name = "itemcount")
        val itemCount: Long = 0,

        @Json(name = "link")
        val link: String?,

        @Json(name = "location")
        val location: String?,

        @Json(name = "locationentityid")
        val locationEntityID: String?,

        @Json(name = "updateddate")
        val updatedDate: Long = 0,

        @Json(name = "updateddatemajor")
        val updatedDateMajor: Long = 0,

        @Json(name = "type")
        val type: Int = 0
)