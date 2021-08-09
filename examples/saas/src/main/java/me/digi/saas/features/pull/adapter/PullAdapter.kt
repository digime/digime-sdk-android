package me.digi.saas.features.pull.adapter

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import me.digi.saas.R
import me.digi.sdk.entities.FileListItem

class PullAdapter : ListAdapter<FileListItem, PullAdapter.PullViewHolder>(Companion) {

    companion object : DiffUtil.ItemCallback<FileListItem>() {

        override fun areItemsTheSame(oldItem: FileListItem, newItem: FileListItem): Boolean =
            oldItem.fileId == newItem.fileId

        override fun areContentsTheSame(
            oldItem: FileListItem,
            newItem: FileListItem
        ): Boolean = oldItem.hashCode() == newItem.hashCode()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PullViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pull, parent, false)
        return PullViewHolder(view)
    }

    override fun onBindViewHolder(holder: PullViewHolder, position: Int) {
        holder.onBind(getItem(position) ?: return)
    }

    inner class PullViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onBind(file: FileListItem) = with(itemView) {
            /**
             * Get views
             */
            val title = findViewById<TextView>(R.id.itemTitle)
            val date = findViewById<TextView>(R.id.itemDate)
            val version = findViewById<TextView>(R.id.itemVersion)

            /**
             * Init views
             */
            title.text = file.fileId
            date.text = DateUtils.getRelativeTimeSpanString(file.updatedDate)
            version.text = file.objectVersion
        }
    }
}