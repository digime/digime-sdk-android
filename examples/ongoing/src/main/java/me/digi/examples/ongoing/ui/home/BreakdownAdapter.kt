package me.digi.examples.ongoing.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.genre_breakdown_item.view.*
import me.digi.ongoing.R

class BreakdownAdapter : RecyclerView.Adapter<BreakdownAdapter.ViewHolder>() {

    private val dummyData = listOf(
        listOf("Song 1", 3),
        listOf("Song 2", 5),
        listOf("Song 3", 2)
    )

    inner class ViewHolder(item: View) : RecyclerView.ViewHolder(item)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemInflater = LayoutInflater.from(parent.context)
        val itemView = itemInflater.inflate(R.layout.genre_breakdown_item, parent, false)
        return  ViewHolder(itemView)
    }

    override fun getItemCount(): Int = dummyData.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.genreLabel.text = dummyData[position][0].toString()
        holder.itemView.songCount.text = "${dummyData[position][1]} songs"
    }

}