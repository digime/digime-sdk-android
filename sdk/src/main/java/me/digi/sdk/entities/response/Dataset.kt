package me.digi.sdk.entities.response

data class DataSet (
    val description: String = "",
    val name: String = "",
)

data class DataSetsResponse (
    val datasets: Map<String, DataSet>
)