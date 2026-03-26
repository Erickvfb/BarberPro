package com.example.barberpro.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.barberpro.R
import com.example.barberpro.model.ClientHistory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter para histórico de serviços do cliente
 */
class ClientHistoryAdapter : RecyclerView.Adapter<ClientHistoryAdapter.ViewHolder>() {

    private val items = mutableListOf<ClientHistory>()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("pt", "BR"))

    fun submitList(newItems: List<ClientHistory>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_client_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconImageView: ImageView = itemView.findViewById(R.id.historyIcon)
        private val serviceText: TextView = itemView.findViewById(R.id.historyServiceText)
        private val dateText: TextView = itemView.findViewById(R.id.historyDateText)
        private val priceText: TextView = itemView.findViewById(R.id.historyPriceText)

        fun bind(history: ClientHistory) {
            iconImageView.setImageResource(history.iconRes)
            serviceText.text = history.serviceName
            dateText.text = dateFormat.format(Date(history.date))
            priceText.text = currencyFormat.format(history.price)
        }
    }
}