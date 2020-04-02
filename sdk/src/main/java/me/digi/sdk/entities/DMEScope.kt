package me.digi.sdk.entities

class DMEScope @JvmOverloads constructor(serviceGroups: List<DMEServiceGroup>? = null, timeRanges: List<DMETimeRange>? = null): DMEDataRequest {

    override lateinit var serviceGroups: List<DMEServiceGroup>

    override lateinit var timeRanges: List<DMETimeRange>

    override val context: String = "scope" // Context defaults to 'scope'.

    override fun serviceGroupsInitialized() = ::serviceGroups.isInitialized

    override fun timeRangesInitialized() = ::timeRanges.isInitialized

    init {
        serviceGroups?.also(this::serviceGroups.setter)
        timeRanges?.also(this::timeRanges.setter)
    }

}