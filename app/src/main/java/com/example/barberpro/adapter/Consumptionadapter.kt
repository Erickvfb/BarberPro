package com.example.barberpro.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.barberpro.R
import com.example.barberpro.model.ConsumptionItem
import java.text.NumberFormat
import java.util.*

class ConsumptionAdapter(
    private val onRemoveItem: (ConsumptionItem) -> Unit
) : RecyclerView.Adapter<ConsumptionAdapter.ViewHolder>() {

    private val items = mutableListOf<ConsumptionItem>()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    fun submitList(newItems: List<ConsumptionItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun addItem(item: ConsumptionItem) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun removeItem(item: ConsumptionItem) {
        val index = items.indexOf(item)
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun getTotal(): Double = items.sumOf { it.getTotal() }

    fun getItems(): List<ConsumptionItem> = items.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_consumption, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.consumptionNameText)
        private val priceText: TextView = itemView.findViewById(R.id.consumptionPriceText)
        private val removeButton: ImageView = itemView.findViewById(R.id.removeConsumptionButton)

        fun bind(item: ConsumptionItem) {
            nameText.text = "${item.name} (${item.quantity}x)"
            priceText.text = currencyFormat.format(item.getTotal())

            removeButton.setOnClickListener {
                onRemoveItem(item)  // ✅ CORREÇÃO: Chama o callback com o item
            }
        }
    }
}