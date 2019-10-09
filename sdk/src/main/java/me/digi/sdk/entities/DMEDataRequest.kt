package me.digi.sdk.entities

interface DMEDataRequest {

    var timeRanges: List<DMETimeRange>

    val context: String

}