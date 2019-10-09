/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.secondary

import com.squareup.moshi.Json

data class FamilyMemberHistoryCondition(
        @Json(name = "code")
        val code: CodeableConcept?,

        @Json(name = "outcome")
        val outcome: CodeableConcept?,

        @Json(name = "note")
        val note: List<Annotation>?
)