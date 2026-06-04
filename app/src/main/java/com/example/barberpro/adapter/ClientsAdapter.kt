package com.example.barberpro.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.barberpro.R
import com.example.barberpro.model.Client

/**
 * Sealed class for different item types in clients list
 */
sealed class ClientListItem {
    data class SectionHeader(val letter: String) : ClientListItem()
    data class ClientItem(val client: Client, val isVip: Boolean = false) : ClientListItem()
}

/**
 * Adapter for clients list with alphabetical sections
 */
class ClientesAdapter(
    private val onClientClick: (Client) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<ClientListItem>()

    companion object {
        private const val VIEW_TYPE_SECTION = 0
        private const val VIEW_TYPE_CLIENT = 1
    }

    fun submitList(clients: List<Client>) {
        items.clear()

        // Group clients by first letter
        val grouped = clients.groupBy { client ->
            client.name.firstOrNull()?.uppercaseChar() ?: '#'
        }.toSortedMap()

        // Create items with section headers
        grouped.forEach { (letter, clientsInSection) ->
            items.add(ClientListItem.SectionHeader(letter.toString()))
            clientsInSection.forEach { client ->
                // Check if client is VIP (example: spent more than R$ 500)
                val isVip = false // TODO: Implement VIP logic
                items.add(ClientListItem.ClientItem(client, isVip))
            }
        }

        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ClientListItem.SectionHeader -> VIEW_TYPE_SECTION
            is ClientListItem.ClientItem -> VIEW_TYPE_CLIENT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SECTION -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_client_section, parent, false)
                SectionViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_client, parent, false)
                ClientViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ClientListItem.SectionHeader -> {
                (holder as SectionViewHolder).bind(item.letter)
            }
            is ClientListItem.ClientItem -> {
                (holder as ClientViewHolder).bind(item.client, item.isVip)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    /**
     * ViewHolder for section headers
     */
    inner class SectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val letterText: TextView = itemView.findViewById(R.id.sectionLetterText)

        fun bind(letter: String) {
            letterText.text = letter
        }
    }

    /**
     * ViewHolder for client items
     */
    inner class ClientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarInitial: TextView = itemView.findViewById(R.id.avatarInitial)
        private val avatarImage: ImageView = itemView.findViewById(R.id.avatarImage)
        private val clientNameText: TextView = itemView.findViewById(R.id.clientNameText)
        private val clientPhoneText: TextView = itemView.findViewById(R.id.clientPhoneText)
        private val vipIcon: ImageView = itemView.findViewById(R.id.vipIcon)

        fun bind(client: Client, isVip: Boolean) {
            // Set initial
            avatarInitial.text = client.getInitial()

            // Hide image, show initial (for now)
            avatarImage.visibility = View.GONE
            avatarInitial.visibility = View.VISIBLE

            // Client info
            clientNameText.text = client.name
            clientPhoneText.text = formatPhone(client.phone)

            // VIP indicator
            vipIcon.visibility = if (isVip) View.VISIBLE else View.GONE

            // Click listener
            itemView.setOnClickListener {
                onClientClick(client)
            }
        }

        private fun formatPhone(phone: String): String {
            // Formato: (11) 98765-4321
            return if (phone.length == 11) {
                "(${phone.substring(0, 2)}) ${phone.substring(2, 7)}-${phone.substring(7)}"
            } else {
                phone
            }
        }
    }

    class ClientSelectionAdapter(
        private val onClientClick: (Client) -> Unit
    ) : RecyclerView.Adapter<ClientSelectionAdapter.ViewHolder>() {

        private val clients = mutableListOf<Client>()
        private val filteredClients = mutableListOf<Client>()

        fun submitList(list: List<Client>) {

            clients.clear()
            clients.addAll(list)

            filteredClients.clear()
            filteredClients.addAll(list)

            notifyDataSetChanged()
        }

        fun filter(query: String) {

            filteredClients.clear()

            if (query.isBlank()) {

                filteredClients.addAll(clients)

            } else {

                filteredClients.addAll(
                    clients.filter {
                        it.name.contains(
                            query,
                            ignoreCase = true
                        )
                    }
                )
            }

            notifyDataSetChanged()
        }

        override fun getItemCount() = filteredClients.size

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ViewHolder {

            val view = LayoutInflater
                .from(parent.context)
                .inflate(
                    R.layout.item_client_selection,
                    parent,
                    false
                )

            return ViewHolder(view)
        }

        override fun onBindViewHolder(
            holder: ViewHolder,
            position: Int
        ) {

            holder.bind(filteredClients[position])
        }

        inner class ViewHolder(
            itemView: View
        ) : RecyclerView.ViewHolder(itemView) {

            private val nameText =
                itemView.findViewById<TextView>(
                    R.id.clientNameText
                )

            private val phoneText =
                itemView.findViewById<TextView>(
                    R.id.clientPhoneText
                )

            fun bind(client: Client) {

               itemView.findViewById<TextView>(R.id.avatarInitial)
                    .text = client.getInitial()

                nameText.text = client.name

                phoneText.text = client.phone ?: "Sem telefone"

                itemView.setOnClickListener {
                    onClientClick(client)
                }
            }
        }
    }
}