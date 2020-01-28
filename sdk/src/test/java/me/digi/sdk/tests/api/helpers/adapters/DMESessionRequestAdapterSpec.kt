package me.digi.sdk.tests.api.helpers.adapters

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import io.mockk.every
import io.mockk.mockk
import me.digi.sdk.api.adapters.DMESessionRequestAdapter
import me.digi.sdk.entities.*
import me.digi.sdk.entities.api.DMESessionRequest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DMESessionRequestAdapterSpec {

    private var dummyScope: DMEScope = DMEScope()
    private val mockContext = mockk<JsonSerializationContext>(relaxed = true)
    private val test = JsonObject()
    private val gson = Gson()


    @Before
    fun setup() {
        dummyScope = DMEScope()
        test.addProperty("dummy", "dummy")
        every { mockContext.serialize(any()) } returns test
    }

    @Test
    fun `given a valid scope format, service groups and time ranges are serialized`() {
        val serviceObjectTypes: List<DMEServiceObjectType> =
            listOf(DMEServiceObjectType(1), DMEServiceObjectType(2), DMEServiceObjectType(7))
        val serviceTypes: List<DMEServiceType> = listOf(DMEServiceType(4, serviceObjectTypes))
        val serviceGroups: List<DMEServiceGroup> = listOf(DMEServiceGroup(2, serviceTypes))

        dummyScope.serviceGroups = serviceGroups
        dummyScope.timeRanges = listOf(DMETimeRange(null, null, null, "all"))

        val actualResult = DMESessionRequestAdapter.serialize(
            DMESessionRequest(
                "dummyAppId",
                "dummyContractId",
                DMESDKAgent(),
                "gzip",
                dummyScope
            ), null, mockContext
        )

        val expectedResult = gson.fromJson(
            "{\"appId\":\"dummyAppId\",\"contractId\":\"dummyContractId\",\"accept\":{\"dummy\":\"dummy\"},\"sdkAgent\":{\"dummy\":\"dummy\"},\"scope\":{\"serviceGroups\":[{\"id\":2,\"serviceTypes\":[{\"id\":4,\"serviceObjectTypes\":[{\"id\":1},{\"id\":2},{\"id\":7}]}]}],\"timeRanges\":[{\"type\":\"all\"}]}}",
            JsonElement::class.java
        )

        assertEquals(actualResult, expectedResult)
    }

    @Test
    fun `given an invalid scope format for service types, time ranges are serialized`() {
        val serviceTypes: List<DMEServiceType> = listOf()
        val serviceGroups: List<DMEServiceGroup> = listOf(DMEServiceGroup(2, serviceTypes))

        dummyScope.serviceGroups = serviceGroups
        dummyScope.timeRanges = listOf(DMETimeRange(null, null, null, "all"))

        val actualResult = DMESessionRequestAdapter.serialize(
            DMESessionRequest(
                "dummyAppId",
                "dummyContractId",
                DMESDKAgent(),
                "gzip",
                dummyScope
            ), null, mockContext
        )

        val expectedResult = gson.fromJson(
            "{\"appId\":\"dummyAppId\",\"contractId\":\"dummyContractId\",\"accept\":{\"dummy\":\"dummy\"},\"sdkAgent\":{\"dummy\":\"dummy\"},\"scope\":{\"timeRanges\":[{\"type\":\"all\"}]}}",
            JsonElement::class.java
        )

        assertEquals(actualResult, expectedResult)
    }

    @Test
    fun `given an invalid scope format for service object types, time ranges are serialized`() {
        val serviceObjectTypes: List<DMEServiceObjectType> = mutableListOf()
        val serviceTypes: List<DMEServiceType> = listOf(DMEServiceType(4, serviceObjectTypes))
        val serviceGroups: List<DMEServiceGroup> = listOf(DMEServiceGroup(2, serviceTypes))

        dummyScope.serviceGroups = serviceGroups
        dummyScope.timeRanges = listOf(DMETimeRange(null, null, null, "all"))

        val actualResult = DMESessionRequestAdapter.serialize(
            DMESessionRequest(
                "dummyAppId",
                "dummyContractId",
                DMESDKAgent(),
                "gzip",
                dummyScope
            ), null, mockContext
        )

        val expectedResult = gson.fromJson(
            "{\"appId\":\"dummyAppId\",\"contractId\":\"dummyContractId\",\"accept\":{\"dummy\":\"dummy\"},\"sdkAgent\":{\"dummy\":\"dummy\"},\"scope\":{\"timeRanges\":[{\"type\":\"all\"}]}}",
            JsonElement::class.java
        )

        assertEquals(actualResult, expectedResult)
    }

    @Test
    fun `given an empty scope format for time ranges, service groups are serialized`() {
        val serviceObjectTypes: List<DMEServiceObjectType> = listOf(DMEServiceObjectType(1), DMEServiceObjectType(2), DMEServiceObjectType(7))
        val serviceTypes: List<DMEServiceType> = listOf(DMEServiceType(4, serviceObjectTypes))
        val serviceGroups: List<DMEServiceGroup> = listOf(DMEServiceGroup(2, serviceTypes))

        dummyScope.serviceGroups = serviceGroups

        val actualResult = DMESessionRequestAdapter.serialize(
            DMESessionRequest(
                "dummyAppId",
                "dummyContractId",
                DMESDKAgent(),
                "gzip",
                dummyScope
            ), null, mockContext
        )

        val expectedResult = gson.fromJson(
            "{\"appId\":\"dummyAppId\",\"contractId\":\"dummyContractId\",\"accept\":{\"dummy\":\"dummy\"},\"sdkAgent\":{\"dummy\":\"dummy\"},\"scope\":{\"serviceGroups\":[{\"id\":2,\"serviceTypes\":[{\"id\":4,\"serviceObjectTypes\":[{\"id\":1},{\"id\":2},{\"id\":7}]}]}]}}",
            JsonElement::class.java
        )

        assertEquals(actualResult, expectedResult)
    }

    @Test
    fun `given an empty scope format for service groups, time ranges are serialized`() {
        dummyScope.timeRanges = listOf(DMETimeRange(null, null, null, "all"))

        val actualResult = DMESessionRequestAdapter.serialize(
            DMESessionRequest(
                "dummyAppId",
                "dummyContractId",
                DMESDKAgent(),
                "gzip",
                dummyScope
            ), null, mockContext
        )

        val expectedResult = gson.fromJson(
            "{\"appId\":\"dummyAppId\",\"contractId\":\"dummyContractId\",\"accept\":{\"dummy\":\"dummy\"},\"sdkAgent\":{\"dummy\":\"dummy\"},\"scope\":{\"timeRanges\":[{\"type\":\"all\"}]}}",
            JsonElement::class.java
        )

        assertEquals(actualResult, expectedResult)
    }
}