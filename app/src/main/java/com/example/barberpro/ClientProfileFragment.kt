package com.example.barberpro

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.barberpro.adapter.ClientHistoryAdapter
import com.example.barberpro.model.Client
import com.example.barberpro.model.ClientHistory
import com.example.barberpro.repository.ClientsRepository
import com.google.android.material.card.MaterialCardView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class ClientProfileFragment : Fragment() {

    private lateinit var backButton: ImageView
    private lateinit var clientNameText: TextView
    private lateinit var clientSinceText: TextView
    private lateinit var agendarButton: MaterialButton
    private lateinit var chatButton: MaterialCardView
    private lateinit var totalGastoText: TextView
    private lateinit var visitasText: TextView
    private lateinit var phoneCard: MaterialCardView
    private lateinit var phoneText: TextView
    private lateinit var emailCard: MaterialCardView
    private lateinit var emailText: TextView
    private lateinit var historyRecyclerView: RecyclerView

    private lateinit var historyAdapter: ClientHistoryAdapter
    private val repository = ClientsRepository.getInstance()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    private lateinit var client: Client

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_client_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        setupClicks()
        loadArguments()
    }

    private fun initViews(view: View) {
        backButton = view.findViewById(R.id.backButton)
        clientNameText = view.findViewById(R.id.clientNameText)
        clientSinceText = view.findViewById(R.id.clientSinceText)
        agendarButton = view.findViewById(R.id.agendarButton)
        chatButton = view.findViewById(R.id.chatButton)
        totalGastoText = view.findViewById(R.id.totalGastoText)
        visitasText = view.findViewById(R.id.visitasText)
        phoneCard = view.findViewById(R.id.phoneCard)
        phoneText = view.findViewById(R.id.phoneText)
        emailCard = view.findViewById(R.id.emailCard)
        emailText = view.findViewById(R.id.emailText)
        historyRecyclerView = view.findViewById(R.id.historyRecyclerView)
    }

    private fun setupRecyclerView() {
        historyAdapter = ClientHistoryAdapter()
        historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        historyRecyclerView.adapter = historyAdapter
    }

    private fun setupClicks() {
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        agendarButton.setOnClickListener {
            Toast.makeText(requireContext(), "Agendar para ${client.name}", Toast.LENGTH_SHORT).show()
        }

        chatButton.setOnClickListener { openWhatsApp() }
        phoneCard.setOnClickListener { makePhoneCall() }
        emailCard.setOnClickListener { sendEmail() }

        val moreOptionsButton: ImageView = requireView().findViewById(R.id.moreOptionsButton)
        moreOptionsButton.setOnClickListener {
            showOptionsMenu()
        }
    }

        private fun showOptionsMenu() {
            val options = arrayOf("Editar Cliente", "Excluir Cliente")

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Selecione uma opção:")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> editarCliente()
                        1 -> confirmarExclusao()
                    }
                }
                .show()
        }

    private fun loadArguments() {
        arguments?.let {
            val id = it.getString("CLIENT_ID")
            if (id != null) {
                loadClient(id)
            } else {
                Toast.makeText(requireContext(), "Cliente não encontrado", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun loadClient(clientId: String) {
        lifecycleScope.launch {
            val result = repository.getClientById(clientId)

            result.onSuccess {
                client = it
                updateUI()
                loadHistory()
            }

            result.onFailure {
                Toast.makeText(requireContext(), "Erro: ${it.message}", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun updateUI() {
        clientNameText.text = client.name
        clientSinceText.text = client.getFormattedCreatedDate()
        totalGastoText.text = currencyFormat.format(client.totalSpent)
        visitasText.text = client.visitCount.toString()
        phoneText.text = client.phone
        emailText.text = client.email
    }

    private fun loadHistory() {
        val history = listOf(
            ClientHistory(
                id = "1",
                clientId = client.id,
                serviceId = "1",
                serviceName = "Corte Social",
                date = System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000),
                price = 46.80,
                iconRes = R.drawable.ic_scissors
            )
        )
        historyAdapter.submitList(history)
    }

    // Chamadas externas
    private fun makePhoneCall() {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${client.phone}")
        }
        startActivity(intent)
    }

    private fun sendEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:${client.email}")
            putExtra(Intent.EXTRA_SUBJECT, "BarberPro - Contato")
        }
        startActivity(intent)
    }

    private fun openWhatsApp() {
        val phoneNumber = client.phone.replace("[^0-9]".toRegex(), "")
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://wa.me/55$phoneNumber")
        }
        startActivity(intent)
    }

    companion object {
        fun newInstance(client: Client): ClientProfileFragment {
            return ClientProfileFragment().apply {
                arguments = Bundle().apply {
                    putString("CLIENT_ID", client.id)
                }
            }
        }
    }

    private fun editarCliente() {
        // Abre o fragment de edição passando o cliente atual
        parentFragmentManager.beginTransaction()
            .replace(
                R.id.fragmentContainer, // container do fragment principal
                NewClientFragment.newInstance(client)
            )
            .addToBackStack(null)
            .commit()
    }

    private fun confirmarExclusao() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Excluir Cliente")
            .setMessage("Tem certeza que deseja excluir ${client.name}?")
            .setPositiveButton("Excluir") { _, _ ->
                excluirCliente()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun excluirCliente() {
        lifecycleScope.launch {
            val result = repository.deleteClient(client.id)

            result.onSuccess {
                Toast.makeText(requireContext(), "Cliente excluído com sucesso!", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }

            result.onFailure { error ->
                Toast.makeText(requireContext(), "Erro ao excluir: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}