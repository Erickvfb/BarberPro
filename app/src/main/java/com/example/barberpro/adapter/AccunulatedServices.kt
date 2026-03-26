package com.example.barberpro.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.barberpro.R
import com.example.barberpro.model.ServiceSummary
import java.text.NumberFormat
import java.util.*

/**
 * Adapter para lista de serviços/produtos acumulados
 */
class ServicosAcumuladoAdapter : RecyclerView.Adapter<ServicosAcumuladoAdapter.ViewHolder>() {

    private val items = mutableListOf<ServiceSummary>()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    fun submitList(newItems: List<ServiceSummary>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_accumulated_service, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconImageView: ImageView = itemView.findViewById(R.id.servicoIcon)
        private val nomeText: TextView = itemView.findViewById(R.id.servicoNomeText)
        private val quantidadeText: TextView = itemView.findViewById(R.id.servicoQuantidadeText)
        private val valorText: TextView = itemView.findViewById(R.id.servicoValorText)

        fun bind(item: ServiceSummary) {
            iconImageView.setImageResource(item.iconRes)
            nomeText.text = item.nome
            quantidadeText.text = "${item.quantidade} vendas"
            valorText.text = currencyFormat.format(item.valorTotal)
        }
    }
}