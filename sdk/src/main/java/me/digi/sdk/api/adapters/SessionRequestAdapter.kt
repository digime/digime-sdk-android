package me.digi.sdk.api.adapters

import com.google.gson.*
import me.digi.sdk.entities.*
import me.digi.sdk.entities.request.SessionRequest
import me.digi.sdk.utilities.DMELog
import java.lang.reflect.Type

object SessionRequestAdapter : JsonSerializer<SessionRequest> {

    override fun serialize(
        src: SessionRequest?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {

        val json = JsonObject()

        if (src != null && context != null) {

            json.addProperty("appId", src.appId)
            json.addProperty("contractId", src.contractId)
            json.add("accept", context.serialize(DataAcceptCondition(src.compression)))
            json.add("sdkAgent", context.serialize(src.sdkAgent))

            if (src.scope != null) {
                val dataReq = serializeDataRequest(src.scope)
                if (dataReq.size() > 0) {
                    json.add(src.scope.context, dataReq)
                }
            }
        }

        return json
    }

    private fun serializeTimeRanges(src: DataRequest): JsonArray {
        val timeRangesJSON = JsonArray()

        if (src.timeRangesInitialized()) {
            src.timeRanges.forEach { timeRange ->
                val timeRangeJSON = JsonObject()

                val timeFrom = timeRange.from
                val timeTo = timeRange.to
                val timeLast = timeRange.last

                if (timeLast != null) {
                    timeRangeJSON.addProperty("last", timeLast)
                } else if (timeFrom != null && timeTo != null) {
                    timeRangeJSON.addProperty("from", timeFrom.time)
                    timeRangeJSON.addProperty("to", timeTo.time)
                }

                if (timeRangeJSON.size() > 0) {
                    timeRangesJSON.add(timeRangeJSON)
                }
            }

            if (timeRangesJSON.count() == 0)
                DMELog.e("Invalid scope time ranges format.")

            return timeRangesJSON
        } else
            return JsonArray()
    }

    private fun serializeServiceGroups(src: DataRequest): JsonArray {
        val serviceGroupsJSON = JsonArray()

        if (src.serviceGroupsInitialized()) {

            src.serviceGroups.forEach { serviceGroup ->
                val serviceGroupJSON = JsonObject()
                val serviceTypesJSON = JsonArray()

                serviceGroupJSON.addProperty("id", serviceGroup.id)

                serviceGroup.serviceTypes.forEach { serviceType ->
                    val serviceTypeJSON = JsonObject()
                    val serviceObjectTypesJSON = JsonArray()

                    serviceTypeJSON.addProperty("id", serviceType.id)

                    serviceType.serviceObjectTypes.forEach { serviceObjectType ->
                        val serviceObjectTypeJSON = JsonObject()
                        serviceObjectTypeJSON.addProperty("id", serviceObjectType.id)

                        if (serviceObjectTypeJSON.size() > 0)
                            serviceObjectTypesJSON.add(serviceObjectTypeJSON)
                    }

                    if (serviceObjectTypesJSON.count() > 0) {
                        serviceTypeJSON.add("serviceObjectTypes", serviceObjectTypesJSON)
                        serviceTypesJSON.add(serviceTypeJSON)
                    }
                }
                if (serviceTypesJSON.count() > 0) {
                    serviceGroupJSON.add("serviceTypes", serviceTypesJSON)
                    serviceGroupsJSON.add(serviceGroupJSON)
                }
            }

            if (serviceGroupsJSON.count() == 0)
                DMELog.e("Invalid scope service groups format.")

            return serviceGroupsJSON
        } else
            return JsonArray()
    }

    private fun serializeCriteria(src: DataRequest): JsonArray {
        val criteriasJSON = JsonArray()

        if (src.criteriaInitialized()) {
            src.criteria.forEach { criteria ->
                val criteriaJson = JsonObject()

                when (criteria) {
                    is Criteria -> {
                        val from = criteria.from
                        val to = criteria.to
                        val last = criteria.last

                        val metadataJson = JsonObject()
                        val metadataCriteria = criteria.metadata

                        if (last != null) {
                            criteriaJson.addProperty("last", last)
                        } else if (from != null && to != null) {
                            criteriaJson.addProperty("from", from)
                            criteriaJson.addProperty("to", to)
                        }

                        if (metadataCriteria != null) {
                            val accountsIds = metadataCriteria.accountIds
                            val mimeTypes = metadataCriteria.mimeTypes
                            val references = metadataCriteria.references
                            val tags = metadataCriteria.tags

                            if (!accountsIds.isNullOrEmpty()) {
                                val accountIdsJson = JsonArray()
                                accountsIds.forEach { id ->
                                    accountIdsJson.add(id)
                                }

                                metadataJson.add("accounts.accountId", accountIdsJson)
                            }

                            if (!mimeTypes.isNullOrEmpty()) {
                                val mimeTypesJson = JsonArray()
                                mimeTypes.forEach { mimeType ->
                                    mimeTypesJson.add(mimeType)
                                }

                                metadataJson.add("mimeType", mimeTypesJson)
                            }

                            if (!references.isNullOrEmpty()) {
                                val referencesJson = JsonArray()
                                references.forEach { reference ->
                                    referencesJson.add(reference)
                                }

                                metadataJson.add("reference", referencesJson)
                            }

                            if (!tags.isNullOrEmpty()) {
                                val tagsJson = JsonArray()
                                tags.forEach { tag ->
                                    tagsJson.add(tag)
                                }

                                metadataJson.add("tags", tagsJson)

                            }

                            criteriaJson.add("metadata", metadataJson)
                        }
                    }
                    is PartnersCriteria -> {
                        val partners = criteria.partners

                        if (!partners.isNullOrEmpty()) {
                            val partnersJson = JsonArray()
                            partners.forEach { partner ->
                                partnersJson.add(partner)
                            }

                            criteriaJson.add("partners", partnersJson)
                        }
                    }
                }
            }

            if (criteriasJSON.count() == 0)
                DMELog.e("Invalid unmapped scope format.")

            return criteriasJSON
        } else
            return JsonArray()
    }

    private fun serializeDataRequest(src: DataRequest): JsonObject {

        val json = JsonObject()

        val timeRangesJSON = serializeTimeRanges(src)
        val serviceGroupsJSON = serializeServiceGroups(src)
        val criteriaJSON = serializeCriteria(src)

        if (serviceGroupsJSON.count() > 0) {
            json.add("serviceGroups", serviceGroupsJSON)
        }

        if (timeRangesJSON.count() > 0) {
            json.add("timeRanges", timeRangesJSON)
        }

        if (criteriaJSON.count() > 0) {
            json.add("criteria", timeRangesJSON)
        }

        return json
    }
}