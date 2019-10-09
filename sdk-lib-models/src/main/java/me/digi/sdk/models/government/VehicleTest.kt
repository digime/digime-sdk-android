/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.government

import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.ToJson

data class VehicleTest(
        @Json(name = "accountentityid")
        val accountEntityId: String?,

        @Json(name = "advisorynotes")
        val advisoryNotes: List<String> = listOf(),

        @Json(name = "createddate")
        val createdDate: Long?,

        @Json(name = "entityid")
        val entityId: String,

        @Json(name = "expirydate")
        val expiryDate: Long?,

        @Json(name = "failurereasons")
        val failureReasons: List<String> = listOf(),

        @Json(name = "id")
        val id: String?,

        @Json(name = "odometerreading")
        val odometerReading: Long?,

        @Json(name = "odometerunit")
        val odometerUnit: String?,

        @Json(name = "testresult")
        val testResult: TestResult = TestResult.UNKNOWN

) {
    companion object {
        enum class TestResult {
            @Json(name = "0")
            UNKNOWN,
            @Json(name = "1")
            PASS,
            @Json(name = "2")
            FAIL;

            companion object {
                @ToJson
                fun toJson(value: TestResult): Int =
                        when (value) {
                            UNKNOWN -> 0
                            PASS -> 1
                            FAIL -> 2
                        }

                @FromJson
                fun fromJson(value: Int): TestResult =
                        when (value) {
                            0 -> UNKNOWN
                            1 -> PASS
                            2 -> FAIL
                            else -> UNKNOWN
                        }
            }
        }
    }
}