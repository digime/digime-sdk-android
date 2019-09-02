package me.digi.sdk.api.adapters

import com.google.gson.*
import me.digi.sdk.entities.DMEDataAcceptCondition
import me.digi.sdk.entities.DMEDataRequest
import me.digi.sdk.entities.api.DMESessionRequest
import java.lang.reflect.Type

object DMESessionRequestAdapter: JsonSerializer<DMESessionRequest> {

    override fun serialize(src: DMESessionRequest?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {

        val json = JsonObject()

        if (src != null && context != null) {

            json.addProperty("appId", src.appId)
            json.addProperty("contractId", src.contractId)
            json.add("accept", context.serialize(DMEDataAcceptCondition(src.compression)))
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

    private fun serializeDataRequest(src: DMEDataRequest): JsonObject {

        val json = JsonObject()

        val timeRangesJSON = JsonArray()

        src.timeRanges.forEach { timeRange ->
            val timeRangeJSON = JsonObject()

            val timeFrom = timeRange.from
            val timeTo = timeRange.to
            val timeLast = timeRange.last

            if (timeLast != null) {
                timeRangeJSON.addProperty("last", timeLast)
            }
            else if (timeFrom != null && timeTo != null) {
                timeRangeJSON.addProperty("from", timeFrom.time)
                timeRangeJSON.addProperty("to", timeTo.time)
            }

            if (timeRangeJSON.size() > 0) {
                timeRangesJSON.add(timeRangeJSON)
            }
        }

        if (timeRangesJSON.count() > 0) {
            json.add("timeRanges", timeRangesJSON)
        }

        return json
    }
}