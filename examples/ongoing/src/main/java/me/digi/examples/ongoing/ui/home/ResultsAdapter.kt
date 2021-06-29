package me.digi.examples.ongoing.ui.home

import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.res.use
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import me.digi.examples.ongoing.model.GenreInsight
import me.digi.ongoing.R

class ResultsAdapter : ListAdapter<GenreInsight, ResultsAdapter.ResultViewHolder>(Companion) {

    companion object : DiffUtil.ItemCallback<GenreInsight>() {
        override fun areItemsTheSame(oldItem: GenreInsight, newItem: GenreInsight): Boolean =
            oldItem.title == newItem.title

        override fun areContentsTheSame(oldItem: GenreInsight, newItem: GenreInsight): Boolean =
            oldItem.hashCode() == newItem.hashCode()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.genre_breakdown_item, parent, false)

        return ResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        holder.bind(getItem(position) ?: return)
    }

    inner class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private fun setPercentBarColor(percentBar: ProgressBar) {
            val colors: IntArray =
                itemView.context.resources.obtainTypedArray(R.array.colors).use { ta ->
                    IntArray(ta.length()) { ta.getColor(it, 0) }
                }
            val randomColourIndex = (0 until (colors.size - 1)).random()
            val color = colors[randomColourIndex]

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                percentBar.progressDrawable
                    .mutate()
                    .colorFilter = BlendModeColorFilter(color, BlendMode.SRC_IN)
            else
                percentBar.progressDrawable
                    .mutate()
                    .setColorFilter(color, PorterDuff.Mode.SRC_IN)
        }

        fun bind(item: GenreInsight) = with(itemView) {

            // Get views
            val genreLabel = findViewById<TextView>(R.id.genreLabel)
            val songCount = findViewById<TextView>(R.id.songCount)
            val percentBar = findViewById<ProgressBar>(R.id.percentBar)

            // Set values
            genreLabel?.text = item.title
            songCount?.text =
                context.getString(R.string.label_songs_count, item.playCount.toString())
            percentBar?.apply {
                progress = item.playCount
                max = item.sampleSize
            }

            setPercentBarColor(percentBar)
        }
    }
}