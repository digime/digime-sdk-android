package me.digi.examples.ongoing.model

data class Album(
    val accountentityid: String = "",
    val artists: List<Artist> = listOf(),
    val availablemarkets: List<Any> = listOf(),
    val copyrights: List<Copyright> = listOf(),
    val entityid: String = "",
    val genres: List<Any> = listOf(),
    val id: String = "",
    val label: String = "",
    val link: String = "",
    val name: String = "",
    val popularity: Int = 0,
    val releasedate: Long = 0,
    val type: String = ""
)