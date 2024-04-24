package me.digi.sdk.entities

interface DataRequest {
    var serviceGroups: List<ServiceGroup>
    var timeRanges: List<TimeRange>
    val context: String
    val criteria: List<Any>

    fun serviceGroupsInitialized(): Boolean
    fun timeRangesInitialized(): Boolean
    fun criteriaInitialized(): Boolean
}