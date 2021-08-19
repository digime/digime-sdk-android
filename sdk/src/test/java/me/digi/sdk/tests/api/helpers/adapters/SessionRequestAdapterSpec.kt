package me.digi.sdk.tests.api.helpers.adapters

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import io.mockk.every
import io.mockk.mockk
import me.digi.sdk.api.adapters.SessionRequestAdapter
import me.digi.sdk.entities.*
import me.digi.sdk.entities.request.SessionRequest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SessionRequestAdapterSpec {

    private var dummyScope: CaScope = CaScope()
    private val mockContext = mockk<JsonSerializationContext>(relaxed = true)
    private val test = JsonObject()
    private val gson = Gson()


    @Before
    fun setup() {
        dummyScope = CaScope()
        test.addProperty("dummy", "dummy")
        every { mockContext.serialize(any()) } returns test
    }

    @Test
    fun `given a valid scope format, service groups and time ranges are serialized`() {
        val serviceObjectTypes: List<ServiceObjectType> =
            listOf(ServiceObjectType(1), ServiceObjectType(2), ServiceObjectType(7))
        val serviceTypes: List<ServiceType> = listOf(ServiceType(4, serviceObjectTypes))
        val serviceGroups: List<ServiceGroup> = listOf(ServiceGroup(2, serviceTypes))

        dummyScope.serviceGroups = serviceGroups
        dummyScope.timeRanges = listOf(TimeRange(null, null, null, "all"))

        val actualResult = SessionRequestAdapter.serialize(
            SessionRequest(
                "dummyAppId",
                "dummyContractId",
                SdkAgent(),
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
        val serviceTypes: List<ServiceType> = listOf()
        val serviceGroups: List<ServiceGroup> = listOf(ServiceGroup(2, serviceTypes))

        dummyScope.serviceGroups = serviceGroups
        dummyScope.timeRanges = listOf(TimeRange(null, null, null, "all"))

        val actualResult = SessionRequestAdapter.serialize(
            SessionRequest(
                "dummyAppId",
                "dummyContractId",
                SdkAgent(),
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
        val serviceObjectTypes: List<ServiceObjectType> = mutableListOf()
        val serviceTypes: List<ServiceType> = listOf(ServiceType(4, serviceObjectTypes))
        val serviceGroups: List<ServiceGroup> = listOf(ServiceGroup(2, serviceTypes))

        dummyScope.serviceGroups = serviceGroups
        dummyScope.timeRanges = listOf(TimeRange(null, null, null, "all"))

        val actualResult = SessionRequestAdapter.serialize(
            SessionRequest(
                "dummyAppId",
                "dummyContractId",
                SdkAgent(),
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
        val serviceObjectTypes: List<ServiceObjectType> = listOf(ServiceObjectType(1), ServiceObjectType(2), ServiceObjectType(7))
        val serviceTypes: List<ServiceType> = listOf(ServiceType(4, serviceObjectTypes))
        val serviceGroups: List<ServiceGroup> = listOf(ServiceGroup(2, serviceTypes))

        dummyScope.serviceGroups = serviceGroups

        val actualResult = SessionRequestAdapter.serialize(
            SessionRequest(
                "dummyAppId",
                "dummyContractId",
                SdkAgent(),
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
        dummyScope.timeRanges = listOf(TimeRange(null, null, null, "all"))

        val actualResult = SessionRequestAdapter.serialize(
            SessionRequest(
                "dummyAppId",
                "dummyContractId",
                SdkAgent(),
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