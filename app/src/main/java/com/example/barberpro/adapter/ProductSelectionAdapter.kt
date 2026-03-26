package com.example.barberpro.model.com.example.barberpro.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.barberpro.R
import com.example.barberpro.model.StockProducts
import java.text.NumberFormat
import java.util.*

class ProductSelectionAdapter(
    private val onProductClick: (StockProducts) -> Unit
) : RecyclerView.Adapter<ProductSelectionAdapter.ViewHolder>() {

    private val products = mutableListOf<StockProducts>()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    fun submitList(newProducts: List<StockProducts>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        // Implementar filtro local se necessário
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_selection, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.productNameText)
        private val priceText: TextView = itemView.findViewById(R.id.productPriceText)
        private val stockText: TextView = itemView.findViewById(R.id.productStockText)

        fun bind(product: StockProducts) {
            nameText.text = product.name
            priceText.text = currencyFormat.format(product.unitPrice)


            // Stock
            stockText.text = "${product.quantity} un"
            stockText.setTextColor(
                if (product.isLowStock()) {
                    android.graphics.Color.parseColor("#EF4444") // Vermelho
                } else {
                    android.graphics.Color.parseColor("#059669") // Verde
                }
            )

            itemView.setOnClickListener {
                onProductClick(product)
            }
        }
    }
}