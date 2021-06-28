package me.digi.examples.ongoing.model

data class Artist(
    val genres: List<String> = listOf(),
    val link: String = "",
    val name: String = "",
    val popularity: Int = 0,
)