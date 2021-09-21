package me.digi.saas.features.read.adapter

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

class ReadAdapter : ListAdapter<FileListItem, ReadAdapter.ReadViewHolder>(Companion) {

    companion object : DiffUtil.ItemCallback<FileListItem>() {

        override fun areItemsTheSame(oldItem: FileListItem, newItem: FileListItem): Boolean =
            oldItem.fileId == newItem.fileId

        override fun areContentsTheSame(
            oldItem: FileListItem,
            newItem: FileListItem
        ): Boolean = oldItem.hashCode() == newItem.hashCode()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pull, parent, false)
        return ReadViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReadViewHolder, position: Int) {
        holder.onBind(getItem(position) ?: return)
    }

    inner class ReadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

            setOnClickListener {
                onFileItemClickListener?.let { click -> click(file) }
            }
        }
    }

    /**
     * Click listeners
     */
    private var onFileItemClickListener: ((FileListItem) -> Unit)? = null

    fun setOnFileItemClickListener(listener: (FileListItem) -> Unit) {
        onFileItemClickListener = listener
    }
}