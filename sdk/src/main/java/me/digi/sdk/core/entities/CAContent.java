/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core.entities;

import com.google.gson.annotations.SerializedName;

public class CAContent {
    @SerializedName("annotation")
    public String annotation;

    @SerializedName("baseid")
    public String baseId;

    @SerializedName("commentcount")
    public int commentCount;

    @SerializedName("createddate")
    public long createdDate;

    @SerializedName("entityid")
    public String entityId;

    @SerializedName("favouritecount")
    public int favouriteCount;

    @SerializedName("iscommentable")
    public int isCommentable;

    @SerializedName("isfavourited")
    public int isFavourited;

    @SerializedName("islikeable")
    public int isLikeable;

    @SerializedName("islikes")
    public int isLikes;

    @SerializedName("isshared")
    public int isShared;

    @SerializedName("istruncated")
    public int isTruncated;

    @SerializedName("latitude")
    public float latitude;

    @SerializedName("likecount")
    public int likeCount;

    @SerializedName("longitude")
    public float longitude;

    @SerializedName("originalcrosspostid")
    public String originalCrossPostId;

    @SerializedName("originalpostid")
    public String originalPostId;

    @SerializedName("originalposturl")
    public String originalPostUrl;

    @SerializedName("personentityid")
    public String personEntityId;

    @SerializedName("personfilerelativepath")
    public String personFileRelativePath;

    @SerializedName("personfileurl")
    public String personFileUrl;

    @SerializedName("personfullname")
    public String personFullname;

    @SerializedName("personusername")
    public String personUsename;

    @SerializedName("postentityid")
    public String posEntityId;

    @SerializedName("postid")
    public String postId;

    @SerializedName("postreplycount")
    public String postReplyCount;

    @SerializedName("posturl")
    public String postUrl;

    @SerializedName("rawtext")
    public String rawText;

    @SerializedName("referenceentityid")
    public String referenceEntityId;

    @SerializedName("referenceentitytype")
    public int referenceEntityType;

    @SerializedName("sharecount")
    public int shareCount;

    @SerializedName("socialnetworkuserentityid")
    public String socialNetworkUserEntityId;

    @SerializedName("source")
    public String source;

    @SerializedName("text")
    public String text;

    @SerializedName("title")
    public String title;

    @SerializedName("type")
    public int type;

    @SerializedName("updateddate")
    public long updatedDate;

    @SerializedName("visibility")
    public String visibility;

    @Override
    public String toString() {
        return "File: " + this.entityId +
                ", Title: " + this.title +
                ", Text: " + this.text +
                ", Created: " + this.createdDate;
    }
}
