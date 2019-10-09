package me.digi.sdk.entities

class DMEScope: DMEDataRequest {

    override lateinit var timeRanges: List<DMETimeRange>

    override val context: String = "scope" // Context defaults to 'scope'.
}