package me.digi.examples.ongoing.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.objectbox.Box
import kotlinx.android.synthetic.main.genre_breakdown_item.view.*
import me.digi.examples.ongoing.model.Genre
import me.digi.examples.ongoing.model.Song
import me.digi.examples.ongoing.service.ObjectBox
import me.digi.examples.ongoing.utils.GenreInsightGenerator
import me.digi.ongoing.R
import kotlin.random.Random

class BreakdownAdapter : RecyclerView.Adapter<BreakdownAdapter.ViewHolder>() {

    private val genreBox: Box<Song> = ObjectBox.boxStore.boxFor(Song::class.java)
    private val genreInsights = GenreInsightGenerator.generateGenrePair(genreBox.query().build().find())

    inner class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        private val genreName = itemView.genreLabel
        private val playCount = itemView.songCount
        private val percentBar = itemView.percentBar

        fun bind(insight: Genre) {
            genreName.text = insight.title
            playCount.text = "${insight.playCount} songs"
            percentBar.progress = insight.playCount
            percentBar.max = insight.sampleSize
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemInflater = LayoutInflater.from(parent.context)
        val itemView = itemInflater.inflate(R.layout.genre_breakdown_item, parent, false)
        return  ViewHolder(itemView)
    }

    override fun getItemCount(): Int = genreInsights.count()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(genreInsights[position])

}