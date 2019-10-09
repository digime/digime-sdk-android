/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.finance

import me.digi.sdk.models.ModelTest
import org.junit.Assert
import org.junit.Test

class TransactionSpec : ModelTest<Transaction>(Transaction::class.java) {
    override val emptyTest: Transaction? = null

    override val jsonObjectTests: List<Pair<Transaction?, String>> =
            listOf(
                    Pair(
                            Transaction(
                                    "dummyContainer",
                                    "dummyId",
                                    "dummyEntityId",
                                    "dummyAccountEntityId",
                                    1.5f,
                                    "dummyCurrency",
                                    2.5,
                                    "dummyRunningCurrency",
                                    "dummyOriginalRef",
                                    "dummySimpleRef",
                                    "dummyBaseType",
                                    "dummyType",
                                    "dummySubType",
                                    "dummyCategory",
                                    1,
                                    2,
                                    "dummyCategorySource",
                                    "dummyCategoryType",
                                    "dummyCheckNumber",
                                    "dummyConsumerRef",
                                    "dummyMerchantId",
                                    "dummyMerchantName",
                                    "dummyMerchantAddress1",
                                    "dummyMerchantAddress2",
                                    "dummyMerchantCity",
                                    "dummyMerchantCountry",
                                    3,
                                    4,
                                    5,
                                    true,
                                    "dummyStatus"
                            ),
                            """
                                {
                                   "container":"dummyContainer",
                                   "id":"dummyId",
                                   "entityid":"dummyEntityId",
                                   "accountentityid":"dummyAccountEntityId",
                                   "amount":1.5,
                                   "currency":"dummyCurrency",
                                   "runningbalance":2.5,
                                   "runningbalancecurrency":"dummyRunningCurrency",
                                   "originalref":"dummyOriginalRef",
                                   "simpleref":"dummySimpleRef",
                                   "basetype":"dummyBaseType",
                                   "type":"dummyType",
                                   "subtype":"dummySubType",
                                   "category":"dummyCategory",
                                   "categoryid":1,
                                   "highlevelcategoryid":2,
                                   "categorysource":"dummyCategorySource",
                                   "categorytype":"dummyCategoryType",
                                   "checknumber":"dummyCheckNumber",
                                   "consumerref":"dummyConsumerRef",
                                   "merchantid":"dummyMerchantId",
                                   "merchantname":"dummyMerchantName",
                                   "merchantaddress1":"dummyMerchantAddress1",
                                   "merchantaddress2":"dummyMerchantAddress2",
                                   "merchantcity":"dummyMerchantCity",
                                   "merchantcountry":"dummyMerchantCountry",
                                   "createddate":3,
                                   "transactiondate":4,
                                   "postdate":5,
                                   "ismanual":true,
                                   "status":"dummyStatus"
                                }
                            """.trimIndent()
                    )
            )

    @Test
    fun `when baseType is null, isCredit should be false`() {
        Assert.assertFalse(jsonObjectTests[0].first!!.copy(baseType = null).isCredit)
    }

    @Test
    fun `when baseType is not 'credit', isCredit should be false`() {
        Assert.assertFalse(jsonObjectTests[0].first!!.copy(baseType = "dummyType").isCredit)
    }

    @Test
    fun `when baseType is 'credit', isCredit should be true`() {
        Assert.assertTrue(jsonObjectTests[0].first!!.copy(baseType = "credit").isCredit)
        Assert.assertTrue(jsonObjectTests[0].first!!.copy(baseType = "Credit").isCredit)
        Assert.assertTrue(jsonObjectTests[0].first!!.copy(baseType = "CREDIT").isCredit)
    }
}