package me.digi.sdk.entities

import java.util.*

/**
 * @param id Get service group (example: 5 - Entertainment)
 * @param serviceTypes Get services for this service group
 */
data class ServiceGroup(
    val id: Int,
    val serviceTypes: List<ServiceType> = listOf()
)

/**
 * @param id Get service type (example: 19 - Spotify)
 * @param serviceObjectTypes Get all objects for this service type
 */
data class ServiceType(
    val id: Int,
    val serviceObjectTypes: List<ServiceObjectType> = listOf()
)

/**
 * @param id Get object for service (example: 406 - Play History)
 */
data class ServiceObjectType(val id: Int)

/**
 * Specify time length for your dataset.
 *
 * @param from Start date for dataset
 * @param to End date for dataset
 * @param last Instead of from/to dates, you can specify here string of how many days
 * you want your data to be fetched (example: "1d" will get data from last 24h)
 */
data class TimeRange(
    val from: Date? = null,
    val to: Date? = null,
    val last: String? = null
)

data class Criteria(
    var from: Long? = null,
    var to: Long? = null,
    var last: String? = null,
    var metadata: MetadataScope? = null
)

data class MetadataScope(
    var accountIds: List<String>? = null,
    var mimeTypes: List<String>? = null,
    var references: List<String>? = null,
    var tags: List<String>? = null
)

data class PartnersCriteria(
    var partners: List<String>? = null
)

class CaScope @JvmOverloads constructor(
    serviceGroups: List<ServiceGroup>? = null,
    timeRanges: List<TimeRange>? = null,
    criteria: List<Any>? = null
) : DataRequest {

    /**
     * Control the scope using service groups
     */
    override lateinit var serviceGroups: List<ServiceGroup>

    /**
     * Control the scope using time ranges
     */
    override lateinit var timeRanges: List<TimeRange>

    /**
     * Control the scope using criteria - raw data
     */
    override lateinit var criteria: List<Any>

    /**
     * Context defaults to 'scope'
     */
    override val context: String = "scope"

    override fun serviceGroupsInitialized() = ::serviceGroups.isInitialized

    override fun timeRangesInitialized() = ::timeRanges.isInitialized

    override fun criteriaInitialized() = ::criteria.isInitialized


    init {
        serviceGroups?.also(this::serviceGroups.setter)
        timeRanges?.also(this::timeRanges.setter)
    }
}