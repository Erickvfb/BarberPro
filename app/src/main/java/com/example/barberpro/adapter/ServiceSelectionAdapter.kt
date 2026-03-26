package com.example.barberpro.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.barberpro.R
import com.example.barberpro.model.Service
import java.text.NumberFormat
import java.util.*

class ServiceSelectionAdapter(
    private val onServiceClick: (Service) -> Unit
) : RecyclerView.Adapter<ServiceSelectionAdapter.ViewHolder>() {

    private val services = mutableListOf<Service>()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    fun submitList(newServices: List<Service>) {
        services.clear()
        services.addAll(newServices)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_selection, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(services[position])
    }

    override fun getItemCount(): Int = services.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.serviceNameText)
        private val priceText: TextView = itemView.findViewById(R.id.servicePriceText)

        fun bind(service: Service) {
            nameText.text = service.name
            priceText.text = currencyFormat.format(service.price)

            itemView.setOnClickListener {
                onServiceClick(service)
            }
        }
    }
}