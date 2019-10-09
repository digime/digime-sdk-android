/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.social

import com.squareup.moshi.Json

data class Comment(
        @Json(name = "baseid")
        val baseID: String?,

        @Json(name = "entityid")
        val entityId: String,

        @Json(name = "commentid")
        val commentID: String?,

        @Json(name = "referenceentityid")
        val referenceEntityID: String?,

        @Json(name = "referenceentitytype")
        val referenceEntityType: Int = 0,

        @Json(name = "commentreplyid")
        val commentReplyID: String?,

        @Json(name = "createddate")
        val createdDate: Long = 0,

        @Json(name = "text")
        val text: String?,

        @Json(name = "commentcount")
        val commentCount: Long = 0,

        @Json(name = "likecount")
        val likeCount: Long = 0,

        @Json(name = "updateddate")
        val updatedDate: Long = 0,

        @Json(name = "link")
        val link: String?,

        @Json(name = "privacy")
        val privacy: Int = 0,

        @Json(name = "metaid")
        val metaID: String?,

        @Json(name = "appid")
        val appID: String?,

        @Json(name = "socialnetworkuserentityid")
        val socialNetworkUserEntityID: String?,

        @Json(name = "personentityid")
        val personEntityID: String?,

        @Json(name = "personfullname")
        val personFullName: String?,

        @Json(name = "personusername")
        val personUsername: String?,

        @Json(name = "personfileurl")
        val personFileUrl: String?
)