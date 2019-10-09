/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models

import com.squareup.moshi.Json
import kotlin.math.sign

data class AspectRatio(
        @Json(name = "accuracy")
        val accuracy: Double = 0.0,
        @Json(name = "actual")
        val actual: String?,
        @Json(name = "closest")
        val closest: String?
)

data class MediaResource(
        @Json(name = "aspectratio")
        val aspectRatio: AspectRatio?,

        @Json(name = "height")
        val height: Int = 0,

        @Json(name = "width")
        val width: Int = 0,

        @Json(name = "mimetype")
        val mimetype: String?,

        @Json(name = "resize")
        val resize: String?,

        @Json(name = "type")
        val type: Int = 0,

        @Json(name = "url")
        val url: String?
) : Comparable<MediaResource> {
    // calculate diagonal of image to compare it between each other
    private fun resolutionSize(): Double = Math.sqrt((width * width + height * height).toDouble())

    override fun compareTo(other: MediaResource): Int =
            (resolutionSize() - other.resolutionSize()).sign.toInt()
}