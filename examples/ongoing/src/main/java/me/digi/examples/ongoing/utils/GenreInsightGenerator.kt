package me.digi.examples.ongoing.utils

import me.digi.examples.ongoing.model.GenreInsight
import me.digi.examples.ongoing.model.Song

object GenreInsightGenerator {

    fun generateInsights(songs: List<Song>): List<GenreInsight> {

        val genreMap: MutableMap<String, Int> = mutableMapOf()

        val genres: List<String>? = songs
            .map { it.track.artists }
            .flatten()
            .map { it.genres }
            .flatten()
            .takeIf { !it.isNullOrEmpty() }

        genres?.forEach { genreMap[it] = 1 + (genreMap[it] ?: 0) }

        return genreMap
            .toList()
            .map { GenreInsight(it.first, it.second, genres?.count() ?: 0) }
            .sortedByDescending { it.playCount }
    }
}