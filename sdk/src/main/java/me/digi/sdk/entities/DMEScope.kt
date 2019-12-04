package me.digi.sdk.entities

class DMEScope: DMEDataRequest {

    override lateinit var serviceGroups: List<DMEServiceGroup>

    override lateinit var timeRanges: List<DMETimeRange>

    override val context: String = "scope" // Context defaults to 'scope'.

    override fun serviceGroupsInitialized() = ::serviceGroups.isInitialized

    override fun timeRangesInitialized() = ::timeRanges.isInitialized

}