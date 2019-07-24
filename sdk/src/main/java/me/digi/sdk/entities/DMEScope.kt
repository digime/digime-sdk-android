package me.digi.sdk.entities

class DMEScope(): DMEDataRequest {

    override lateinit var timeRange: DMETimeRange

    override lateinit var context: String
}