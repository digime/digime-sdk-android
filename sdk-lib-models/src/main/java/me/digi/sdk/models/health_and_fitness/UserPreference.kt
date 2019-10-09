/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.health_and_fitness

import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.ToJson

/**
 * User preference settings
 */

enum class WaterUnit {
    @Json(name = "0")
    MILLILITERS,
    @Json(name = "1")
    FLUID_OUNCE;

    companion object {
        @ToJson
        fun toJson(value: WaterUnit): Int =
                when (value) {
                    MILLILITERS -> 0
                    FLUID_OUNCE -> 1
                }

        @FromJson
        fun fromJson(value: Int): WaterUnit? =
                when (value) {
                    0 -> MILLILITERS
                    1 -> FLUID_OUNCE
                    else -> null
                }
    }
}

enum class LengthUnit {
    @Json(name = "0")
    CM,
    @Json(name = "1")
    INCHES;

    companion object {
        @ToJson
        fun toJson(value: LengthUnit): Int =
                when (value) {
                    CM -> 0
                    INCHES -> 1
                }

        @FromJson
        fun fromJson(value: Int): LengthUnit? =
                when (value) {
                    0 -> CM
                    1 -> INCHES
                    else -> null
                }
    }
}

enum class DistanceUnit {
    @Json(name = "0")
    KM,
    @Json(name = "1")
    MILES;

    companion object {
        @ToJson
        fun toJson(value: DistanceUnit): Int =
                when (value) {
                    KM -> 0
                    MILES -> 1
                }

        @FromJson
        fun fromJson(value: Int): DistanceUnit? =
                when (value) {
                    0 -> KM
                    1 -> MILES
                    else -> null
                }
    }
}

enum class WeightUnit {
    @Json(name = "0")
    KG,
    @Json(name = "1")
    LBS,
    @Json(name = "2")
    STONE;

    companion object {
        @ToJson
        fun toJson(value: WeightUnit): Int =
                when (value) {
                    KG -> 0
                    LBS -> 1
                    STONE -> 2
                }

        @FromJson
        fun fromJson(value: Int): WeightUnit? =
                when (value) {
                    0 -> KG
                    1 -> LBS
                    2 -> STONE
                    else -> null
                }
    }
}