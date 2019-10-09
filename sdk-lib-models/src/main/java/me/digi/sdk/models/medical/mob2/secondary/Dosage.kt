/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import com.squareup.moshi.Json

data class Dosage(
        @Json(name = "sequence")
        val sequence: Long?,

        @Json(name = "text")
        val text: String?,

        @Json(name = "timing")
        val timing: Timing?,

        @Json(name = "asneededboolean")
        val asNeededBoolean: Boolean?,

        @Json(name = "site")
        val site: CodeableConcept?,

        @Json(name = "route")
        val route: CodeableConcept?,

        @Json(name = "method")
        val method: CodeableConcept?,

        @Json(name = "dosequantity")
        val doseQuantity: Quantity?
)