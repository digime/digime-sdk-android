/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import com.squareup.moshi.Json

data class Annotation(
        @Json(name = "authorreference")
        val authorReference: Reference?,

        @Json(name = "authorString")
        val authorString: String?,

        @Json(name = "time")
        val time: Long?,

        @Json(name = "text")
        val text: String?
)