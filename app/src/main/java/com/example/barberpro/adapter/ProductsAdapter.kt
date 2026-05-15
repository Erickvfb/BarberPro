package com.example.barberpro.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.barberpro.R
import com.example.barberpro.model.StockProducts
import com.google.android.material.card.MaterialCardView

/**
 * Adapter para lista de produtos
 */
class ProdutosAdapter(
    private val onProductClick: (StockProducts) -> Unit,
    private val onProductLongClick: (StockProducts) -> Unit
) : RecyclerView.Adapter<ProdutosAdapter.ProductViewHolder>() {

    private val products = mutableListOf<StockProducts>()

    fun submitList(newProducts: List<StockProducts>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nomeText: TextView = itemView.findViewById(R.id.produtoNomeText)
        private val stockText: TextView = itemView.findViewById(R.id.stockText)
        private val precoText: TextView = itemView.findViewById(R.id.produtoPrecoText)

        fun bind(product: StockProducts) {
            // Nome
            nomeText.text = product.name

            //Saldo
            stockText.text = "Qtd: ${product.quantity}"

            // Preço
            precoText.text = "R$ ${String.format("%.2f", product.unitPrice)}"

            // Clicks
            itemView.setOnClickListener {
                onProductClick(product)
            }

            itemView.setOnLongClickListener {
                onProductLongClick(product)
                true
            }
        }
    }
}