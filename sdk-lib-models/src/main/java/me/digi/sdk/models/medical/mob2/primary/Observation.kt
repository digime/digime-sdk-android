/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.medical.mob2.primary

import com.squareup.moshi.Json
import me.digi.sdk.models.medical.mob2.secondary.*

data class Observation(
        @Json(name = "entityid")
        val entityId: String,

        @Json(name = "accountentityid")
        val accountEntityId: String?,

        @Json(name = "createddate")
        val createdDate: Long?,

        @Json(name = "updateddate")
        val updatedDate: Long?,

        @Json(name = "id")
        val id: String?,

        @Json(name = "identifier")
        val identifier: List<Identifier>?,

        @Json(name = "status")
        val status: String?,

        @Json(name = "category")
        val category: List<CodeableConcept>?,

        @Json(name = "code")
        val code: CodeableConcept?,

        @Json(name = "effectivedatetime")
        val effectiveDateTime: Long?,

        @Json(name = "issued")
        val issued: Long?,

        @Json(name = "performer")
        val performer: Reference?,

        @Json(name = "valuequantity")
        val valueQuantity: Quantity?,

        @Json(name = "valuecodeableconcept")
        val valueCodeableConcept: CodeableConcept?,

        @Json(name = "valuestring")
        val valueString: String?,

        @Json(name = "valueboolean")
        val valueBoolean: Boolean?,

        @Json(name = "valueratio")
        val valueRatio: Ratio?,

        @Json(name = "interpretation")
        val interpretation: CodeableConcept?,

        @Json(name = "referencerange")
        val referenceRange: List<ReferenceRange>?,

        @Json(name = "comment")
        val comment: String?,

        @Json(name = "component")
        val component: List<Component>?
)