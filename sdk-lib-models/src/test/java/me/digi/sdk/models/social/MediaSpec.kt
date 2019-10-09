/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.social

import me.digi.sdk.models.AspectRatio
import me.digi.sdk.models.MediaResource

import me.digi.sdk.models.ModelTest
import org.junit.Assert
import org.junit.Test

data class MediaTypeDummy(val type: Media.Companion.MediaType)

class MediaTypeSpec : ModelTest<MediaTypeDummy>(MediaTypeDummy::class.java) {
    override val emptyTest: MediaTypeDummy? = null

    override val jsonObjectTests: List<Pair<MediaTypeDummy?, String>> =
            listOf(
                    Pair(
                            MediaTypeDummy(Media.Companion.MediaType.IMAGE),
                            """{"type":0}""".trimIndent()
                    ),
                    Pair(
                            MediaTypeDummy(Media.Companion.MediaType.VIDEO),
                            """{"type":1}""".trimIndent()
                    ),
                    Pair(
                            null,
                            """{"type":"wrong"}""".trimIndent()
                    )
            )

    override val jsonTests: List<Pair<MediaTypeDummy?, String>> =
            listOf(
                    Pair(
                            MediaTypeDummy(Media.Companion.MediaType.IMAGE),
                            """{"type":99}""".trimIndent()
                    )
            )

    @Test
    fun `when forId receives a known id should return the correct value`() {
        listOf(
                Pair(0, Media.Companion.MediaType.IMAGE),
                Pair(1, Media.Companion.MediaType.VIDEO)
        ).forEach { (id, expectedResult) ->
            Assert.assertEquals(expectedResult, Media.Companion.MediaType.forId(id))
        }
    }

    @Test
    fun `when forId receives an unknown id should return the default value`() {
        val expectedResult = Media.Companion.MediaType.IMAGE
        val result = Media.Companion.MediaType.forId(99)
        Assert.assertEquals(expectedResult, result)
    }
}

class MediaSpec : ModelTest<Media>(Media::class.java) {
    override val emptyTest: Media? = null

    override val jsonObjectTests: List<Pair<Media?, String>> =
            listOf(
                    Pair(
                            Media(
                                    "dummyBaseId",
                                    "dummyEntityId",
                                    "dummyMediaId",
                                    "dummyMediaObjectId",
                                    "dummyMediaAlbumName",
                                    "dummyMediaObjectLikeId",
                                    "dummyLocationEntityId",
                                    "dummyImageFileUrl",
                                    1,
                                    "dummyVideoFileEntityId",
                                    1.5f,
                                    2.5f,
                                    2,
                                    "dummyName",
                                    Media.Companion.MediaType.VIDEO,
                                    "dummyLink",
                                    "dummyFilter",
                                    3,
                                    4,
                                    "dummyDisplayShortUrl",
                                    5,
                                    6,
                                    "dummyCameraModelEntityId",
                                    "dummyItemLicenceEntityId",
                                    "dummyDescription",
                                    "dummyPostEntityId",
                                    setOf(
                                            MediaResource(
                                                    AspectRatio(1.5, "dummyActual", "dummyClosest"),
                                                    1,
                                                    2,
                                                    "application/dummyType",
                                                    "dummyResize",
                                                    3,
                                                    "dummyUrl"
                                            ),
                                            MediaResource(
                                                    AspectRatio(2.5, "dummyActual", "dummyClosest"),
                                                    4,
                                                    5,
                                                    "application/dummyType",
                                                    "dummyResize",
                                                    6,
                                                    "dummyUrl"
                                            )
                                    ),
                                    "dummyPersonEntityId",
                                    "dummyPersonFullName",
                                    "dummyPersonFileUrl",
                                    "dummyPersonUserName"
                            ),
                            """
                                {
                                    "baseid":"dummyBaseId",
                                    "entityid":"dummyEntityId",
                                    "mediaid":"dummyMediaId",
                                    "mediaobjectid":"dummyMediaObjectId",
                                    "mediaalbumname":"dummyMediaAlbumName",
                                    "mediaobjectlikeid":"dummyMediaObjectLikeId",
                                    "locationentityid":"dummyLocationEntityId",
                                    "imagefileurl":"dummyImageFileUrl",
                                    "createddate":1,
                                    "videofileentityid":"dummyVideoFileEntityId",
                                    "latitude":1.5,
                                    "longitude":2.5,
                                    "updateddate":2,
                                    "name":"dummyName",
                                    "type":1,
                                    "link":"dummyLink",
                                    "filter":"dummyFilter",
                                    "commentcount":3,
                                    "likecount":4,
                                    "displayshorturl":"dummyDisplayShortUrl",
                                    "tagcount":5,
                                    "taggedpeoplecount":6,
                                    "cameramodelentityid":"dummyCameraModelEntityId",
                                    "itemlicenceentityid":"dummyItemLicenceEntityId",
                                    "description":"dummyDescription",
                                    "postentityid":"dummyPostEntityId",
                                    "resources":[
                                        {
                                            "aspectratio":{
                                                "accuracy":1.5,
                                                "actual":"dummyActual",
                                                "closest":"dummyClosest"
                                            },
                                            "height":1,
                                            "width":2,
                                            "mimetype":"application/dummyType",
                                            "resize":"dummyResize",
                                            "type":3,
                                            "url":"dummyUrl"
                                        },
                                        {
                                            "aspectratio":{
                                                "accuracy":2.5,
                                                "actual":"dummyActual",
                                                "closest":"dummyClosest"
                                            },
                                            "height":4,
                                            "width":5,
                                            "mimetype":"application/dummyType",
                                            "resize":"dummyResize",
                                            "type":6,
                                            "url":"dummyUrl"
                                        }
                                    ],
                                    "personentityid":"dummyPersonEntityId",
                                    "personfullname":"dummyPersonFullName",
                                    "personfileurl":"dummyPersonFileUrl",
                                    "personusername":"dummyPersonUserName"
                                }
                            """.trimIndent()
                    ),
                    Pair(
                            Media(
                                    "dummyBaseId",
                                    "dummyEntityId",
                                    "dummyMediaId",
                                    "dummyMediaObjectId",
                                    "dummyMediaAlbumName",
                                    "dummyMediaObjectLikeId",
                                    "dummyLocationEntityId",
                                    "dummyImageFileUrl",
                                    1,
                                    "dummyVideoFileEntityId",
                                    1.5f,
                                    2.5f,
                                    2,
                                    "dummyName",
                                    Media.Companion.MediaType.VIDEO,
                                    "dummyLink",
                                    "dummyFilter",
                                    3,
                                    4,
                                    "dummyDisplayShortUrl",
                                    5,
                                    6,
                                    "dummyCameraModelEntityId",
                                    "dummyItemLicenceEntityId",
                                    "dummyDescription",
                                    "dummyPostEntityId",
                                    setOf(
                                            MediaResource(
                                                    AspectRatio(1.5, "dummyActual", "dummyClosest"),
                                                    1,
                                                    2,
                                                    "application/dummyType",
                                                    "dummyResize",
                                                    3,
                                                    "dummyUrl"
                                            )
                                    ),
                                    "dummyPersonEntityId",
                                    "dummyPersonFullName",
                                    "dummyPersonFileUrl",
                                    "dummyPersonUserName"
                            ),
                            """
                                {
                                    "baseid":"dummyBaseId",
                                    "entityid":"dummyEntityId",
                                    "mediaid":"dummyMediaId",
                                    "mediaobjectid":"dummyMediaObjectId",
                                    "mediaalbumname":"dummyMediaAlbumName",
                                    "mediaobjectlikeid":"dummyMediaObjectLikeId",
                                    "locationentityid":"dummyLocationEntityId",
                                    "imagefileurl":"dummyImageFileUrl",
                                    "createddate":1,
                                    "videofileentityid":"dummyVideoFileEntityId",
                                    "latitude":1.5,
                                    "longitude":2.5,
                                    "updateddate":2,
                                    "name":"dummyName",
                                    "type":1,
                                    "link":"dummyLink",
                                    "filter":"dummyFilter",
                                    "commentcount":3,
                                    "likecount":4,
                                    "displayshorturl":"dummyDisplayShortUrl",
                                    "tagcount":5,
                                    "taggedpeoplecount":6,
                                    "cameramodelentityid":"dummyCameraModelEntityId",
                                    "itemlicenceentityid":"dummyItemLicenceEntityId",
                                    "description":"dummyDescription",
                                    "postentityid":"dummyPostEntityId",
                                    "resources":[
                                        {
                                            "aspectratio":{
                                                "accuracy":1.5,
                                                "actual":"dummyActual",
                                                "closest":"dummyClosest"
                                            },
                                            "height":1,
                                            "width":2,
                                            "mimetype":"application/dummyType",
                                            "resize":"dummyResize",
                                            "type":3,
                                            "url":"dummyUrl"
                                        }
                                    ],
                                    "personentityid":"dummyPersonEntityId",
                                    "personfullname":"dummyPersonFullName",
                                    "personfileurl":"dummyPersonFileUrl",
                                    "personusername":"dummyPersonUserName"
                                }
                            """.trimIndent()
                    ),
                    Pair(
                            Media(
                                    "dummyBaseId",
                                    "dummyEntityId",
                                    "dummyMediaId",
                                    "dummyMediaObjectId",
                                    "dummyMediaAlbumName",
                                    "dummyMediaObjectLikeId",
                                    "dummyLocationEntityId",
                                    "dummyImageFileUrl",
                                    1,
                                    "dummyVideoFileEntityId",
                                    1.5f,
                                    2.5f,
                                    2,
                                    "dummyName",
                                    Media.Companion.MediaType.VIDEO,
                                    "dummyLink",
                                    "dummyFilter",
                                    3,
                                    4,
                                    "dummyDisplayShortUrl",
                                    5,
                                    6,
                                    "dummyCameraModelEntityId",
                                    "dummyItemLicenceEntityId",
                                    "dummyDescription",
                                    "dummyPostEntityId",
                                    emptySet(),
                                    "dummyPersonEntityId",
                                    "dummyPersonFullName",
                                    "dummyPersonFileUrl",
                                    "dummyPersonUserName"
                            ),
                            """
                                {
                                    "baseid":"dummyBaseId",
                                    "entityid":"dummyEntityId",
                                    "mediaid":"dummyMediaId",
                                    "mediaobjectid":"dummyMediaObjectId",
                                    "mediaalbumname":"dummyMediaAlbumName",
                                    "mediaobjectlikeid":"dummyMediaObjectLikeId",
                                    "locationentityid":"dummyLocationEntityId",
                                    "imagefileurl":"dummyImageFileUrl",
                                    "createddate":1,
                                    "videofileentityid":"dummyVideoFileEntityId",
                                    "latitude":1.5,
                                    "longitude":2.5,
                                    "updateddate":2,
                                    "name":"dummyName",
                                    "type":1,
                                    "link":"dummyLink",
                                    "filter":"dummyFilter",
                                    "commentcount":3,
                                    "likecount":4,
                                    "displayshorturl":"dummyDisplayShortUrl",
                                    "tagcount":5,
                                    "taggedpeoplecount":6,
                                    "cameramodelentityid":"dummyCameraModelEntityId",
                                    "itemlicenceentityid":"dummyItemLicenceEntityId",
                                    "description":"dummyDescription",
                                    "postentityid":"dummyPostEntityId",
                                    "resources":[],
                                    "personentityid":"dummyPersonEntityId",
                                    "personfullname":"dummyPersonFullName",
                                    "personfileurl":"dummyPersonFileUrl",
                                    "personusername":"dummyPersonUserName"
                                }
                            """.trimIndent()
                    )
            )
}