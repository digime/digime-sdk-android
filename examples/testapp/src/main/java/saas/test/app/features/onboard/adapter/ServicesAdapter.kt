package saas.test.app.features.onboard.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import saas.test.app.R
import me.digi.sdk.entities.service.Service

class ServicesAdapter : ListAdapter<Service, ServicesAdapter.ServiceViewHolder>(Companion) {

    companion object : DiffUtil.ItemCallback<Service>() {
        override fun areItemsTheSame(oldItem: Service, newItem: Service): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Service, newItem: Service): Boolean =
            oldItem.hashCode() == newItem.hashCode()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_service, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        holder.bind(getItem(position) ?: return)
    }

    inner class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(service: Service) = with(itemView) {

            /**
             * Init view
             */
            val icon = findViewById<ImageView>(R.id.serviceImage)
            val title = findViewById<TextView>(R.id.serviceName)

            /**
             * Init values
             */
            title.text = service.name
            if (service.resources.isNotEmpty())
                icon.load(service.resources[1].url) {
                    crossfade(true)
                    placeholder(R.drawable.ic_empty_set)
                    error(R.drawable.ic_empty_set)
                }
            else icon.load(R.drawable.ic_empty_set)

            setOnClickListener {
                onServiceClickListener?.let { click ->
                    click(service)
                }
            }
        }
    }

    /**
     * Click listeners
     */
    private var onServiceClickListener: ((Service) -> Unit)? = null

    fun setOnServiceClickListener(listener: (Service) -> Unit) {
        onServiceClickListener = listener
    }
}