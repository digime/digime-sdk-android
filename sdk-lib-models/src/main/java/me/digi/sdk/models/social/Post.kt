/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.social

import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.ToJson

data class Post(
        @Json(name = "baseid")
        val baseID: String?,

        @Json(name = "entityid")
        val entityId: String,

        @Json(name = "postid")
        val postID: String?,

        @Json(name = "socialnetworkuserentityid")
        val socialNetworkUserEntityID: String?,

        @Json(name = "referenceentityid")
        val referenceEntityID: String?,

        @Json(name = "referenceentitytype")
        val referenceEntityType: Int = 0,

        @Json(name = "personentityid")
        val personEntityID: String?,

        @Json(name = "personfullname")
        val personFullName: String?,

        @Json(name = "personusername")
        val personUsername: String?,

        @Json(name = "personfileurl")
        val personFileUrl: String?,

        @Json(name = "createddate")
        val createdDate: Long = 0,

        @Json(name = "posturl")
        val postUrl: String?,

        @Json(name = "text")
        val text: String?,

        @Json(name = "title")
        val title: String?,

        @Json(name = "updateddate")
        val updatedDate: Long = 0,

        @Json(name = "type")
        val type: PostType?,

        @Json(name = "commentcount")
        val commentCount: Int = 0,

        @Json(name = "sharecount")
        val shareCount: Int = 0,

        @Json(name = "likecount")
        val likeCount: Int = 0,

        /**
         * Used by Twitter
         */
        @Json(name = "favouritecount")
        val favouriteCount: Int = 0,

        @Json(name = "islikes")
        val isLiked: Int = 0
) {
    companion object {
        enum class PostType(val value: Int) {
            @Json(name = "0")
            STANDARD(0),
            @Json(name = "1")
            FACEBOOK_STATUS_UPDATE(1),
            @Json(name = "2")
            FACEBOOK_WALL_POST(2),
            @Json(name = "3")
            FACEBOOK_NOTE(3),
            @Json(name = "4")
            TWITTER_TWEET(4),
            @Json(name = "5")
            TWITTER_MENTION(5),
            @Json(name = "6")
            TWITTER_FAVOURITE(6),
            @Json(name = "7")
            GOOGLE_PLUS_ACTIVITY_POST(7),
            @Json(name = "8")
            GOOGLE_PLUS_ACTIVITY_SHARE(8),
            @Json(name = "9")
            LINKEDIN_SHAR(9),
            @Json(name = "10")
            LINKEDIN_OTHER(10),
            @Json(name = "11")
            RSS_FEED_ITEM(11),
            @Json(name = "12")
            VIADEO_NEWS_ITEM(12),
            @Json(name = "13")
            VIADEO_GROUP_MESSAGE(13),
            @Json(name = "14")
            VIADEO_OTHER(14),
            @Json(name = "15")
            TWITTER_FAVOURITE_AND_TWEET(15),
            @Json(name = "16")
            TWITTER_FAVOURITE_AND_MENTION(16),
            @Json(name = "17")
            TWITTER_MENTION_AND_TWEET(17),
            @Json(name = "18")
            TWITTER_FAVOURITE_AND_MENTION_AND_TWEET(18),
            @Json(name = "19")
            FLICKR_POST(19),
            @Json(name = "20")
            INSTAGRAM_POST(20),
            @Json(name = "21")
            FLICKR_POST_FAVOURITE(21),
            @Json(name = "22")
            FACEBOOK_INTERNAL_POST(22),
            @Json(name = "23")
            FACEBOOK_GROUP_POST(23),
            @Json(name = "24")
            PINTEREST_PIN(24),
            @Json(name = "25")
            PINTEREST_LIKE(25),
            @Json(name = "26")
            SEARCH_WIDGET(26),
            @Json(name = "27")
            ACTIVITY_WIDGET(27),
            @Json(name = "28")
            HERE_LOCATION_WIDGET(28);

            companion object {
                @ToJson
                fun toJson(t: PostType): Int = t.value

                @FromJson
                fun fromJson(value: Int): PostType? = PostType.values().find { it.value == value }
            }
        }
    }
}