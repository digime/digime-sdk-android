/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.io.EOFException

abstract class ModelTest<T>(private val clazz: Class<T>) {
    abstract val emptyTest: T?
    abstract val jsonObjectTests: List<Pair<T?, String>>
    open val jsonTests: List<Pair<T?, String>> = emptyList()

    private val moshi: Moshi =
            Moshi.Builder()
                    .add(KotlinJsonAdapterFactory())
                    .addDigimeModelsAdapters()
                    .build()

    //ToJson
    private val parseToJson: (String) -> T? = { moshi.adapter(clazz).fromJson(it) }
    private val testToJson: (T, String) -> Unit = { expectedResult, json -> Assert.assertEquals(expectedResult, parseToJson(json)) }
    private val testExceptionToJson: (T?, String) -> Unit = { _, json -> parseToJson(json) }
    private val testOrExceptionToJson: (T?, String) -> Unit = { expectedResult, json ->
        when (expectedResult) {
            null ->
                try {
                    testExceptionToJson(expectedResult, json)
                } catch (_: JsonDataException) {
                } catch (e: Exception) {
                    Assert.fail("Expected JsonDataException but got $e")
                }
            else -> testToJson(expectedResult, json)
        }
    }

    //ToObject
    private val parseToObject: (T) -> String? = { moshi.adapter(clazz).toJson(it) }
    private val testToObject: (String, T) -> Unit = { expectedResult, obj -> JSONAssert.assertEquals(expectedResult, parseToObject(obj), false) }

    @Test(expected = EOFException::class)
    fun `when json is empty string should throw EOFException`() =
            testExceptionToJson(emptyTest, "")

    @Test
    fun `when json is empty should return object with default values`() =
            testOrExceptionToJson(emptyTest, "{}")

    @Test(expected = JsonEncodingException::class)
    fun `when json is malformed should throw JsonEncodingException`() =
            testExceptionToJson(emptyTest, "{'dummyKey: 'Value}")

    @Test
    fun `when json is wrong should throw JsonDataException or return object with default values`() =
            testOrExceptionToJson(emptyTest, """{"dummyKey":1}""")

    @Test
    fun `when json is properly should return object with json values`() =
            (jsonObjectTests + jsonTests)
                    .forEach { testOrExceptionToJson(it.first, it.second) }

    @Test
    fun `when object is parsed should return json with object values`() =
            jsonObjectTests
                    .forEach { testCase -> testCase.first?.let { testToObject(testCase.second, it) } }
}