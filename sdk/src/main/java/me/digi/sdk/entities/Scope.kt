package me.digi.sdk.entities

class ServiceObjectType (val id: Int)

class ServiceType (
    val id: Int,
    val serviceObjectTypes: List<ServiceObjectType>
)

class ServiceGroup (
    val id: Int,
    val serviceTypes: List<ServiceType>
)

class Scope @JvmOverloads constructor(serviceGroups: List<ServiceGroup>? = null, timeRanges: List<TimeRange>? = null): DataRequest {

    override lateinit var serviceGroups: List<ServiceGroup>

    override lateinit var timeRanges: List<TimeRange>

    override val context: String = "scope" // Context defaults to 'scope'.

    override fun serviceGroupsInitialized() = ::serviceGroups.isInitialized

    override fun timeRangesInitialized() = ::timeRanges.isInitialized

    init {
        serviceGroups?.also(this::serviceGroups.setter)
        timeRanges?.also(this::timeRanges.setter)
    }
}