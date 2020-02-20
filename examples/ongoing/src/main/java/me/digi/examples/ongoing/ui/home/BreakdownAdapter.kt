package me.digi.examples.ongoing.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.objectbox.Box
import kotlinx.android.synthetic.main.genre_breakdown_item.view.*
import me.digi.examples.ongoing.model.Genre
import me.digi.examples.ongoing.model.Song
import me.digi.examples.ongoing.service.ObjectBox
import me.digi.examples.ongoing.utils.GenreInsightGenerator
import me.digi.ongoing.R

class BreakdownAdapter(val context: Context) : RecyclerView.Adapter<BreakdownAdapter.ViewHolder>() {

    private val genreBox: Box<Song> = ObjectBox.boxStore.boxFor(Song::class.java)
    private val genreInsights = GenreInsightGenerator.generateGenrePair(genreBox.query().build().find())

    inner class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        private val genreName = itemView.genreLabel
        private val playCount = itemView.songCount
        private val percentBar = itemView.percentBar

        private fun setPercentBarColor() {
            val randomColourIndex = (0 until 12).random()
            val colours = listOf(
                R.color.breakdownBarOne,
                R.color.breakdownBarTwo,
                R.color.breakdownBarThree,
                R.color.breakdownBarFour,
                R.color.breakdownBarFive,
                R.color.breakdownBarSix,
                R.color.breakdownBarSeven,
                R.color.breakdownBarEight,
                R.color.breakdownBarNine,
                R.color.breakdownBarTen,
                R.color.breakdownBarEleven,
                R.color.breakdownBarTwelve
            )
            val color = ContextCompat.getColor(context, colours[randomColourIndex])
            percentBar.progressDrawable.mutate().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
        }

        fun bind(insight: Genre) {
            genreName.text = insight.title
            playCount.text = "${insight.playCount} songs"
            percentBar.progress = insight.playCount
            percentBar.max = insight.sampleSize

            setPercentBarColor()
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