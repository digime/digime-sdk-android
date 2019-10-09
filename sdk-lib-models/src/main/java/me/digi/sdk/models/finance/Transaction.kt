/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models.finance

import com.squareup.moshi.Json
import me.digi.sdk.models.ItemDetails.ContentItemDetails

data class Transaction(
        @Json(name = "container")
        val container: String?,

        @Json(name = "id")
        val id: String?,

        @Json(name = "entityid")
        val entityId: String,

        @Json(name = "accountentityid")
        val accountEntityId: String?,

        @Json(name = "amount")
        val amount: Float?,

        @Json(name = "currency")
        val currency: String?,

        @Json(name = "runningbalance")
        val runningBalance: Double?,

        @Json(name = "runningbalancecurrency")
        val runningBalanceCurrency: String?,

        @Json(name = "originalref")
        val originalRef: String?,

        @Json(name = "simpleref")
        val simpleRef: String?,

        @Json(name = "basetype")
        val baseType: String?,

        @Json(name = "type")
        val type: String?,

        @Json(name = "subtype")
        val subType: String?,

        @Json(name = "category")
        val category: String?,

        @Json(name = "categoryid")
        val categoryId: Int?,

        @Json(name = "highlevelcategoryid")
        val highLevelCategoryId: Int?,

        @Json(name = "categorysource")
        val categorySource: String?,

        @Json(name = "categorytype")
        val categoryType: String?,

        @Json(name = "checknumber")
        val checkNumber: String?,

        @Json(name = "consumerref")
        val consumerRef: String?,

        @Json(name = "merchantid")
        val merchantId: String?,

        @Json(name = "merchantname")
        val merchantName: String?,

        @Json(name = "merchantaddress1")
        val merchantAddress1: String?,

        @Json(name = "merchantaddress2")
        val merchantAddress2: String?,

        @Json(name = "merchantcity")
        val merchantCity: String?,

        @Json(name = "merchantcountry")
        val merchantCountry: String?,

        @Json(name = "createddate")
        val createdDate: Long?,

        @Json(name = "transactiondate")
        val transactionDate: Long?,

        @Json(name = "postdate")
        val postDate: Long?,

        @Json(name = "ismanual")
        val isManual: Boolean?,

        @Json(name = "status")
        val status: String?
) : ContentItemDetails {
    val isCredit: Boolean
        get() = "credit".equals(baseType, ignoreCase = true)
}