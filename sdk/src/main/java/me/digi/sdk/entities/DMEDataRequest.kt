package me.digi.sdk.entities

interface DMEDataRequest {

    var serviceGroups: List<DMEServiceGroup>

    var timeRanges: List<DMETimeRange>

    val context: String

    fun serviceGroupsInitialized(): Boolean
    fun timeRangesInitialized(): Boolean
}