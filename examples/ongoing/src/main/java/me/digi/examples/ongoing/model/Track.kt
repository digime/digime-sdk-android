package me.digi.examples.ongoing.model

data class Track(
    val accountentityid: String = "",
    val album: Album = Album(),
    val artists: List<Artist> = listOf(),
    val entityid: String = "",
    val explicit: Boolean = false,
    val id: String = "",
    val link: String = "",
    val name: String = "",
    val number: Int = 0,
    val popularity: Int = 0
)