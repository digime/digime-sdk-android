/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.health_and_fitness

import me.digi.sdk.models.ModelTest
import me.digi.sdk.models.objects.TMediaResource

class BadgeSpec : ModelTest<Badge>(Badge::class.java) {
    override val emptyTest: Badge? = null

    override val jsonObjectTests: List<Pair<Badge?, String>> =
            listOf(
                    Pair(
                            Badge(
                                    "dummyId",
                                    "dummyEntityId",
                                    "dummyAccountEntityId",
                                    1,
                                    "dummyCategory",
                                    2,
                                    3,
                                    "dummyName",
                                    "dummyShortName",
                                    "dummyDescription",
                                    "dummyShortDescription",
                                    "dummyEarnedMessage",
                                    "dummyMarketingDescription",
                                    "dummymobileDescription",
                                    "dummyShareText",
                                    setOf(
                                            TMediaResource.obj
                                    ),
                                    "dummyGradientEndColor",
                                    "dummyGradientStartColor"
                            ),
                            """
                                {
                                    "id":"dummyId",
                                    "entityid":"dummyEntityId",
                                    "accountentityid":"dummyAccountEntityId",
                                    "updateddate":1,
                                    "category":"dummyCategory",
                                    "timesachieved":2,
                                    "value":3,
                                    "name":"dummyName",
                                    "shortname":"dummyShortName",
                                    "description":"dummyDescription",
                                    "shortdescription":"dummyShortDescription",
                                    "earnedmessage":"dummyEarnedMessage",
                                    "marketingdescription":"dummyMarketingDescription",
                                    "mobiledescription":"dummymobileDescription",
                                    "sharetext":"dummyShareText",
                                    "resources":[
                                        ${TMediaResource.json}
                                    ],
                                    "gradientendcolor":"dummyGradientEndColor",
                                    "gradientstartcolor":"dummyGradientStartColor"
                                }
                            """.trimIndent()
                    )
            )
}