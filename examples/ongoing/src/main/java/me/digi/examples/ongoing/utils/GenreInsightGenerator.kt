package me.digi.examples.ongoing.utils

import me.digi.examples.ongoing.model.GenreInsight
import me.digi.examples.ongoing.model.Song

object GenreInsightGenerator {

    fun generateInsights(songs: List<Song>) : List<GenreInsight> {
        val genres = songs.map { it.genres }.flatten()
        val genreMap = mutableMapOf<String, Int>()

        genres.forEach { genreMap[it] = 1 + (genreMap[it]?.let { it } ?: 0) }

        return genreMap.toList().map { GenreInsight(it.first, it.second, genres.count()) }.sortedByDescending { it.playCount }
    }

}