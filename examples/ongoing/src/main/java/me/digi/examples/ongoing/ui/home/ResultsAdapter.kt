package me.digi.examples.ongoing.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.genre_breakdown_item.view.*
import me.digi.examples.ongoing.model.GenreInsight
import me.digi.ongoing.R

class ResultsAdapter(val context: Context) : RecyclerView.Adapter<ResultsAdapter.ViewHolder>() {

    companion object {
        private val colours = listOf(
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
    }

    inner class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {

        private fun setPercentBarColor() {
            val randomColourIndex = (0 until 12).random()
            val color = ContextCompat.getColor(context, colours[randomColourIndex])
            itemView.percentBar.progressDrawable.mutate().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
        }

        fun bind(insight: GenreInsight) {
            itemView.apply {
                genreLabel.text = insight.title
                songCount.text = "${insight.playCount} songs"
                percentBar.progress = insight.playCount
                percentBar.max = insight.sampleSize
            }

            setPercentBarColor()
        }
    }

    private var genreInsights: List<GenreInsight> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemInflater = LayoutInflater.from(parent.context)
        val itemView = itemInflater.inflate(R.layout.genre_breakdown_item, parent, false)
        return  ViewHolder(itemView)
    }

    override fun getItemCount(): Int = genreInsights.count()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(genreInsights[position])

    fun performDiffAndUpdate(newInsights: List<GenreInsight>) {
        val newSampleSize = genreInsights.count() + newInsights.count()
        genreInsights = mutableListOf<GenreInsight>().apply {
            addAll(genreInsights.map { GenreInsight(it.title, it.playCount, newSampleSize) })
            addAll(newInsights.map { GenreInsight(it.title, it.playCount, newSampleSize) })
        }

        notifyDataSetChanged()
    }
}