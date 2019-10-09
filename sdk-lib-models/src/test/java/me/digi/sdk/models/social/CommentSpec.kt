/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.social

import me.digi.sdk.models.ModelTest

class CommentSpec : ModelTest<Comment>(Comment::class.java) {
    override val emptyTest: Comment? = null

    override val jsonObjectTests: List<Pair<Comment?, String>> =
            listOf(
                    Pair(
                            Comment(
                                    "dummyBaseId",
                                    "dummyEntityId",
                                    "dummyCommentId",
                                    "dummyReferenceEntityId",
                                    1,
                                    "dummyCommentReplyId",
                                    2,
                                    "dummyText",
                                    3,
                                    4,
                                    5,
                                    "dummyLink",
                                    6,
                                    "dummyMetaId",
                                    "dummyAppId",
                                    "dummySocialNetworkUserEntityId",
                                    "dummyPersonEntityId",
                                    "dummyPersonFullName",
                                    "dummyPersonUserName",
                                    "dummyPersonFileUrl"
                            ),
                            """
                                {
                                    "baseid":"dummyBaseId",
                                    "entityid":"dummyEntityId",
                                    "commentid":"dummyCommentId",
                                    "referenceentityid":"dummyReferenceEntityId",
                                    "referenceentitytype":1,
                                    "commentreplyid":"dummyCommentReplyId",
                                    "createddate":2,
                                    "text":"dummyText",
                                    "commentcount":3,
                                    "likecount":4,
                                    "updateddate":5,
                                    "link":"dummyLink",
                                    "privacy":6,
                                    "metaid":"dummyMetaId",
                                    "appid":"dummyAppId",
                                    "socialnetworkuserentityid":"dummySocialNetworkUserEntityId",
                                    "personentityid":"dummyPersonEntityId",
                                    "personfullname":"dummyPersonFullName",
                                    "personusername":"dummyPersonUserName",
                                    "personfileurl":"dummyPersonFileUrl"
                                }
                            """.trimIndent()
                    )
            )
}