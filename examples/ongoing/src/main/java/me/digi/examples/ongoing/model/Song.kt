package me.digi.examples.ongoing.model

import com.google.gson.annotations.SerializedName

data class Song(
    @SerializedName("accountentityid")
    val accountEntityId: String = "",
    @SerializedName("createddate")
    val createdDate: Long = 0,
    @SerializedName("entityid")
    val entityId: String = "",
    val id: String = "",
    val track: Track = Track()
)