package me.digi.examples.ongoing.utils

import me.digi.examples.ongoing.model.Artist
import me.digi.examples.ongoing.model.GenreInsight
import me.digi.examples.ongoing.model.Song

object GenreInsightGenerator {

    fun generateInsights(songs: List<Song>): List<GenreInsight> {

        val genreMap: MutableMap<String, Int> = mutableMapOf()

        val artists: List<Artist> = songs.map { it.track.artists }.flatten()
        val genres: List<String> = artists.map { it.genres }.flatten()

        genres.takeIf { !it.isNullOrEmpty() }

        genres.forEach { genreMap[it] = 1 + (genreMap[it] ?: 0) }

        return genreMap.toList().map { GenreInsight(it.first, it.second, genres.count()) }
            .sortedByDescending { it.playCount }
    }
}