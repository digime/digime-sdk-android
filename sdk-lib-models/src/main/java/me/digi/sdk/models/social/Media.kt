/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.social

import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.ToJson
import me.digi.sdk.models.MediaResource

data class Media(
        @Json(name = "baseid")
        val baseID: String?,

        @Json(name = "entityid")
        val entityId: String,

        @Json(name = "mediaid")
        val mediaID: String?,

        @Json(name = "mediaobjectid")
        val mediaObjectID: String?,

        @Json(name = "mediaalbumname")
        val mediaAlbumName: String?,

        @Json(name = "mediaobjectlikeid")
        val mediaObjectLikeID: String?,

        @Json(name = "locationentityid")
        val locationEntityID: String?,

        @Deprecated("Resources should be fully supported")
        @Json(name = "imagefileurl")
        //We still use this field as a fallback in some situations where the resources do not have images.
        //  e.g.: Twitter Videos
        val imageFileUrl: String?,

        @Json(name = "createddate")
        val createdDate: Long = 0,

        @Json(name = "videofileentityid")
        val videoFileEntityID: String?,

        @Json(name = "latitude")
        val latitude: Float = 0.toFloat(),

        @Json(name = "longitude")
        val longitude: Float = 0.toFloat(),

        @Json(name = "updateddate")
        val updatedDate: Long = 0,

        @Json(name = "name")
        val name: String?,

        @Json(name = "type")
        val type: MediaType?,

        @Json(name = "link")
        val link: String?,

        @Json(name = "filter")
        val filter: String?,

        @Json(name = "commentcount")
        val commentCount: Int = 0,

        @Json(name = "likecount")
        val likeCount: Int = 0,

        @Json(name = "displayshorturl")
        val displayShortUrl: String?,

        @Json(name = "tagcount")
        val tagCount: Int = 0,

        @Json(name = "taggedpeoplecount")
        val taggedPeopleCount: Int = 0,

        @Json(name = "cameramodelentityid")
        val cameraModelEntityID: String?,

        @Json(name = "itemlicenceentityid")
        val itemLicenceEntityID: String?,

        @Json(name = "description")
        val description: String?,

        @Json(name = "postentityid")
        val postEntityID: String?,

        @Json(name = "resources")
        val resources: Set<MediaResource>?,

        @Json(name = "personentityid")
        val personEntityID: String?,

        @Json(name = "personfullname")
        val personFullName: String?,

        @Json(name = "personfileurl")
        val personFileUrl: String?,

        @Json(name = "personusername")
        val personUsername: String?
) {
    companion object {
        enum class MediaType(val id: Int) {
            @Json(name = "0")
            IMAGE(0),
            @Json(name = "1")
            VIDEO(1);

            companion object {
                fun forId(id: Int): MediaType =
                        when (id) {
                            0 -> IMAGE
                            1 -> VIDEO
                            else -> IMAGE
                        }

                @ToJson
                fun toJson(t: MediaType): Int = t.id

                @FromJson
                fun fromJson(value: Int): MediaType = MediaType.values().find { it.id == value }
                        ?: MediaType.IMAGE
            }
        }
    }
}