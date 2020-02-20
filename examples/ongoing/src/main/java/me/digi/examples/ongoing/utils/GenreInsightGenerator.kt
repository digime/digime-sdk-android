package me.digi.examples.ongoing.utils

import me.digi.examples.ongoing.model.Genre
import me.digi.examples.ongoing.model.Song

object GenreInsightGenerator {

    fun generateGenrePair(songs: List<Song>) : List<Genre> {
        val genres = songs.map { it.genre.split(",") }.flatten()
        val genreMap = mutableMapOf<String, Int>()

        genres.forEach { genreMap[it] = 1 + (genreMap[it]?.let { it } ?: 0) }

        return genreMap.toList().map { Genre(it.first, it.second, genres.count()) }.sortedByDescending { it.playCount }
    }

}