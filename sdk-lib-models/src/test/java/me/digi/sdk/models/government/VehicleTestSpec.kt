/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.government

import me.digi.sdk.models.ModelTest

class VehicleTestSpec : ModelTest<VehicleTest>(VehicleTest::class.java) {
    override val emptyTest: VehicleTest? = null

    override val jsonObjectTests: List<Pair<VehicleTest?, String>> =
            listOf(
                    Pair(
                            VehicleTest(
                                    "dummyAccountEntityId",
                                    listOf("dummyNote1", "dummyNote2"),
                                    1,
                                    "dummyEntityId",
                                    2,
                                    listOf("dummyReason1", "dummyReason2"),
                                    "dummyId",
                                    3,
                                    "dummyUnit",
                                    VehicleTest.Companion.TestResult.FAIL
                            ),
                            """
                                {
                                   "accountentityid":"dummyAccountEntityId",
                                   "advisorynotes":["dummyNote1","dummyNote2"],
                                   "createddate":1,
                                   "entityid":"dummyEntityId",
                                   "expirydate":2,
                                   "failurereasons":["dummyReason1","dummyReason2"],
                                   "id":"dummyId",
                                   "odometerreading":3,
                                   "odometerunit":"dummyUnit",
                                   "testresult":2
                                }
                            """.trimIndent()
                    ),
                    Pair(
                            VehicleTest(
                                    "dummyAccountEntityId",
                                    listOf("dummyNote1"),
                                    1,
                                    "dummyEntityId",
                                    2,
                                    emptyList(),
                                    "dummyId",
                                    3,
                                    "dummyUnit",
                                    VehicleTest.Companion.TestResult.FAIL
                            ),
                            """
                                {
                                   "accountentityid":"dummyAccountEntityId",
                                   "advisorynotes":["dummyNote1"],
                                   "createddate":1,
                                   "entityid":"dummyEntityId",
                                   "expirydate":2,
                                   "failurereasons":[],
                                   "id":"dummyId",
                                   "odometerreading":3,
                                   "odometerunit":"dummyUnit",
                                   "testresult":2
                                }
                            """.trimIndent()
                    )
            )
}

data class TestResultDummy(val testResult: VehicleTest.Companion.TestResult)

class TestResultSpec : ModelTest<TestResultDummy>(TestResultDummy::class.java) {
    override val emptyTest: TestResultDummy? = null

    override val jsonObjectTests: List<Pair<TestResultDummy?, String>> =
            listOf(
                    Pair(
                            TestResultDummy(VehicleTest.Companion.TestResult.UNKNOWN),
                            """{"testResult":0}""".trimIndent()
                    ),
                    Pair(
                            TestResultDummy(VehicleTest.Companion.TestResult.PASS),
                            """{"testResult":1}""".trimIndent()
                    ),
                    Pair(
                            TestResultDummy(VehicleTest.Companion.TestResult.FAIL),
                            """{"testResult":2}""".trimIndent()
                    ),
                    Pair(
                            null,
                            """{"testResult":"wrong"}""".trimIndent()
                    )
            )

    override val jsonTests: List<Pair<TestResultDummy?, String>> =
            listOf(
                    Pair(
                            TestResultDummy(VehicleTest.Companion.TestResult.UNKNOWN),
                            """{"testResult":99}""".trimIndent()
                    )
            )
}