/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models

import org.junit.Assert
import org.junit.Test

class JFSAccountSyncConfigSpec : ModelTest<JFSAccountSyncConfig>(JFSAccountSyncConfig::class.java) {
    override val emptyTest: JFSAccountSyncConfig? = null

    override val jsonObjectTests: List<Pair<JFSAccountSyncConfig?, String>> =
            listOf(
                    Pair(
                            JFSAccountSyncConfig(SyncType.AUTO),
                            """{"type": "auto"}"""
                    ),
                    Pair(
                            JFSAccountSyncConfig(SyncType.MANUAL),
                            """{"type": "manual"}"""
                    ),
                    Pair(
                            JFSAccountSyncConfig(SyncType.DEFAULT),
                            """{"type": "default"}"""
                    ),
                    Pair(
                            null,
                            """{"type": "dummy"}"""
                    )
            )
}

data class SyncTypeDummy(val type: SyncType)

class SyncTypeSpec : ModelTest<SyncTypeDummy>(SyncTypeDummy::class.java) {
    override val emptyTest: SyncTypeDummy? = null

    override val jsonObjectTests: List<Pair<SyncTypeDummy?, String>> =
            listOf(
                    Pair(
                            SyncTypeDummy(SyncType.AUTO),
                            """{"type":"auto"}""".trimIndent()
                    ),
                    Pair(
                            SyncTypeDummy(SyncType.MANUAL),
                            """{"type":"manual"}""".trimIndent()
                    ),
                    Pair(
                            SyncTypeDummy(SyncType.DEFAULT),
                            """{"type":"default"}""".trimIndent()
                    ),
                    Pair(
                            null,
                            """{"type":"wrong"}""".trimIndent()
                    )
            )

    @Test
    fun `when getTypeById receives a known id should return the correct value`() {
        listOf(
                Pair(0, SyncType.AUTO),
                Pair(1, SyncType.MANUAL),
                Pair(2, SyncType.DEFAULT)
        ).forEach { (id, expectedResult) ->
            Assert.assertEquals(expectedResult, SyncType.getTypeById(id))
        }
    }

    @Test
    fun `when getTypeById receives an unknown id should return the default value`() {
        val expectedResult = SyncType.DEFAULT
        val result = SyncType.getTypeById(99)
        Assert.assertEquals(expectedResult, result)
    }
}