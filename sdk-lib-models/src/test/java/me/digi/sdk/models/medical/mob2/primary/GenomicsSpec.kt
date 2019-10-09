/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.primary

import me.digi.sdk.models.ModelTest

class GenomicsSpec : ModelTest<Genomics>(Genomics::class.java) {
    override val emptyTest: Genomics? = null

    override val jsonObjectTests: List<Pair<Genomics?, String>> =
            listOf(
                    Pair(
                            Genomics(
                                    "dummyEntityId",
                                    "dummyAccountEntityId",
                                    1,
                                    2
                            ),
                            """
                                {
                                    "entityid":"dummyEntityId",
                                    "accountentityid":"dummyAccountEntityId",
                                    "createddate":1,
                                    "updateddate":2
                                }
                            """.trimIndent()
                    )
            )
}