package com.example.barberpro.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.barberpro.R
import com.example.barberpro.model.Service
import com.google.android.material.card.MaterialCardView

/**
 * Adapter for services list
 */
class ServicosAdapter(
    private val onServiceClick: (Service) -> Unit,
    private val onServiceLongClick: (Service) -> Unit
) : RecyclerView.Adapter<ServicosAdapter.ServiceViewHolder>() {

    private val servicos = mutableListOf<Service>()

    fun submitList(newServicos: List<Service>) {
        servicos.clear()
        servicos.addAll(newServicos)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        holder.bind(servicos[position])
    }

    override fun getItemCount(): Int = servicos.size

    inner class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nomeText: TextView = itemView.findViewById(R.id.servicoNomeText)
        private val precoText: TextView = itemView.findViewById(R.id.servicoPrecoText)


        fun bind(service: Service) {
            nomeText.text = service.name
            precoText.text = "R$ ${String.format("%.2f", service.price)}"

            itemView.setOnClickListener {
                onServiceClick(service)
            }

            itemView.setOnLongClickListener {
                onServiceLongClick(service)
                true
            }
        }
    }


}