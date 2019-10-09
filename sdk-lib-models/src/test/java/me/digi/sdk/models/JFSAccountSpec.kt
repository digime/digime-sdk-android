/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models

import org.junit.Assert
import org.junit.Test

//TODO add more cases.
class JFSAccountSpec : ModelTest<JFSAccount>(JFSAccount::class.java) {
    override val emptyTest: JFSAccount? = null

    override val jsonObjectTests: List<Pair<JFSAccount?, String>> =
            listOf(
                    Pair(
                            JFSAccount(
                                    "1_234567890",
                                    3,
                                    null,
                                    null,
                                    "1234567890",
                                    "1111111111111111111-222222222222222222222222222222",
                                    "abcdefghijklmnopqrstuvwxyz0123456789",
                                    "Dummy Account",
                                    "DummyUser",
                                    "https://dummy.com/image.png",
                                    null,
                                    2,
                                    946684800,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    JFSAccount.Companion.AccountType.USER,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                            ),
                            """
                                {
                                   "accesstoken":"1111111111111111111-222222222222222222222222222222",
                                   "accesstokensecret":"abcdefghijklmnopqrstuvwxyz0123456789",
                                   "accountid":"1234567890",
                                   "accounttype":0,
                                   "connectionstatus":2,
                                   "entityid":"1_234567890",
                                   "fileurl":"https://dummy.com/image.png",
                                   "fullname":"Dummy Account",
                                   "serviceid":3,
                                   "updateddate":946684800,
                                   "username":"DummyUser"
                                }
                            """.trimIndent()
                    )
            )

    @Test
    fun `when obscuredAccountId receives a string with greater than 10 characters should return the correct value`() {
        val accountId = "a".repeat(11)
        val expectedResult = "***...*aaaa"
        val result = JFSAccount.obscuredAccountId(accountId)
        Assert.assertEquals(expectedResult, result)
    }

    @Test
    fun `when obscuredAccountId receives a string with less than 10 characters and greater or equal than 4 should return the correct value`() {
        listOf(
                Pair(4, "aaaa"),
                Pair(5, "*aaaa"),
                Pair(6, "**aaaa"),
                Pair(7, "***aaaa"),
                Pair(8, "****aaaa"),
                Pair(9, "*****aaaa")
        ).forEach { (repeatTimes, expectedResult) ->
            val accountId = "a".repeat(repeatTimes)
            val result = JFSAccount.obscuredAccountId(accountId)
            Assert.assertEquals(expectedResult, result)
        }
    }

    @Test
    fun `when obscuredAccountId receives a string with 10 characters should return the correct value`() {
        val accountId = "a".repeat(10)
        val expectedResult = "******aaaa"
        val result = JFSAccount.obscuredAccountId(accountId)
        Assert.assertEquals(expectedResult, result)
    }

    @Test
    fun `when obscuredAccountId receives a string with less than 4 characters should throw exception`() {
        for (i in 0..3) {
            try {
                JFSAccount.obscuredAccountId("a".repeat(i))
                Assert.fail("Expected StringIndexOutOfBoundsException but didn't raise expection")
            } catch (e: StringIndexOutOfBoundsException) {
            } catch (e: Exception) {
                Assert.fail("Expected StringIndexOutOfBoundsException but got $e")
            }
        }
    }
}

data class AccountTypeDummy(val type: JFSAccount.Companion.AccountType)

class AccountTypeSpec : ModelTest<AccountTypeDummy>(AccountTypeDummy::class.java) {
    override val emptyTest: AccountTypeDummy? = null

    override val jsonObjectTests: List<Pair<AccountTypeDummy?, String>> =
            listOf(
                    Pair(
                            AccountTypeDummy(JFSAccount.Companion.AccountType.USER),
                            """{"type":0}""".trimIndent()
                    ),
                    Pair(
                            AccountTypeDummy(JFSAccount.Companion.AccountType.ADMIN),
                            """{"type":1}""".trimIndent()
                    ),
                    Pair(
                            AccountTypeDummy(JFSAccount.Companion.AccountType.EVENT),
                            """{"type":2}""".trimIndent()
                    ),
                    Pair(
                            AccountTypeDummy(JFSAccount.Companion.AccountType.GROUP),
                            """{"type":3}""".trimIndent()
                    ),
                    Pair(
                            AccountTypeDummy(JFSAccount.Companion.AccountType.BANK),
                            """{"type":4}""".trimIndent()
                    ),
                    Pair(
                            AccountTypeDummy(JFSAccount.Companion.AccountType.CREDIT_CARD),
                            """{"type":5}""".trimIndent()
                    ),
                    Pair(
                            AccountTypeDummy(JFSAccount.Companion.AccountType.IMPORTED),
                            """{"type":6}""".trimIndent()
                    ),
                    Pair(
                            null,
                            """{"type":"wrong"}""".trimIndent()
                    )
            )

    override val jsonTests: List<Pair<AccountTypeDummy?, String>> =
            listOf(
                    Pair(
                            AccountTypeDummy(JFSAccount.Companion.AccountType.USER),
                            """{"type":99}""".trimIndent()
                    )
            )
}