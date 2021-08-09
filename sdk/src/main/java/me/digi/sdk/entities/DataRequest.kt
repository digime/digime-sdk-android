package me.digi.sdk.entities

interface DataRequest {
    var serviceGroups: List<ServiceGroup>
    var timeRanges: List<TimeRange>
    val context: String

    fun serviceGroupsInitialized(): Boolean
    fun timeRangesInitialized(): Boolean
}