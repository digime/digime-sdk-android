package me.digi.sdk.api.adapters

import com.google.gson.*
import me.digi.sdk.entities.DataAcceptCondition
import me.digi.sdk.entities.DataRequest
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
                val timeType = timeRange.type

                if (timeType != null) {
                    timeRangeJSON.addProperty("type", timeType)
                }

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

    private fun serializeDataRequest(src: DataRequest): JsonObject {

        val json = JsonObject()

        val timeRangesJSON = serializeTimeRanges(src)
        val serviceGroupsJSON = serializeServiceGroups(src)

        if (serviceGroupsJSON.count() > 0) {
            json.add("serviceGroups", serviceGroupsJSON)
        }

        if (timeRangesJSON.count() > 0) {
            json.add("timeRanges", timeRangesJSON)
        }

        return json
    }
}