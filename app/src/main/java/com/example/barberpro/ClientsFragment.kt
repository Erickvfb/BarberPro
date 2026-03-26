package com.example.barberpro

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.barberpro.adapter.ClientesAdapter
import com.example.barberpro.model.Client
import com.example.barberpro.repository.ClientsRepository
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class ClientsFragment : Fragment() {

    // Views
    private lateinit var clientsRecyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var fabAddClient: FloatingActionButton
    private lateinit var chipTodos: Chip
    private lateinit var chipFrequentes: Chip
    private lateinit var chipInativos: Chip
    private lateinit var chipNovos: Chip

    // Data
    private lateinit var clientesAdapter: ClientesAdapter
    private val repository = ClientsRepository.getInstance()
    private var allClients = listOf<Client>()
    private var currentFilter = FilterType.TODOS

    enum class FilterType {
        TODOS, FREQUENTES, INATIVOS, NOVOS
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_clients, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupRecyclerView()
        setupSearch()
        setupFilters()
        setupClickListeners()
        loadClients()
    }

    /**
     * 🔥 Sincroniza BottomNavigation com o menu Clientes
     */
    override fun onResume() {
        super.onResume()
        (activity as? MainContainerActivity)
            ?.setSelectedMenuItem(R.id.nav_clients)

        loadClients()
    }

    private fun initializeViews(view: View) {
        try {
            clientsRecyclerView = view.findViewById(R.id.clientsRecyclerView)
            searchEditText = view.findViewById(R.id.searchEditText)
            fabAddClient = view.findViewById(R.id.fabAddClient)
            chipTodos = view.findViewById(R.id.chipTodos)
            chipFrequentes = view.findViewById(R.id.chipFrequentes)
            chipInativos = view.findViewById(R.id.chipInativos)
            chipNovos = view.findViewById(R.id.chipNovos)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Erro ao inicializar views: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setupRecyclerView() {
        clientesAdapter = ClientesAdapter { client ->
            showClientDetails(client)
        }

        clientsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = clientesAdapter
        }
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filterClients(s.toString())
            }
        })
    }

    private fun setupFilters() {
        chipTodos.setOnClickListener {
            currentFilter = FilterType.TODOS
            updateClientsDisplay()
        }

        chipFrequentes.setOnClickListener {
            currentFilter = FilterType.FREQUENTES
            updateClientsDisplay()
        }

        chipInativos.setOnClickListener {
            currentFilter = FilterType.INATIVOS
            updateClientsDisplay()
        }

        chipNovos.setOnClickListener {
            currentFilter = FilterType.NOVOS
            updateClientsDisplay()
        }
    }

    private fun setupClickListeners() {
        fabAddClient.setOnClickListener {
            addNewClientFragment()
        }
    }

    private fun loadClients() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = repository.getAllClients()

                result.onSuccess { clients ->
                    allClients = clients
                    updateClientsDisplay()
                }

                result.onFailure { error ->
                    Toast.makeText(
                        requireContext(),
                        "Erro ao carregar clientes: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Erro: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateClientsDisplay() {
        val now = System.currentTimeMillis()
        val thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000

        val filtered = when (currentFilter) {
            FilterType.TODOS -> allClients
            FilterType.FREQUENTES -> allClients.filter { it.visitCount >= 5 }
            FilterType.INATIVOS -> allClients.filter { it.visitCount == 0 }
            FilterType.NOVOS -> allClients.filter { now - it.createdAt <= thirtyDaysInMillis }
        }

        clientesAdapter.submitList(filtered)
        updateChipCounts()
    }

    private fun filterClients(query: String) {
        if (query.isEmpty()) {
            updateClientsDisplay()
            return
        }

        val filtered = allClients.filter { client ->
            client.name.contains(query, ignoreCase = true) ||
                    client.phone.contains(query)
        }

        clientesAdapter.submitList(filtered)
    }

    private fun updateChipCounts() {
        val now = System.currentTimeMillis()
        val thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000

        val frequentesCount = allClients.count { it.visitCount >= 5 }
        val inativosCount = allClients.count { it.visitCount == 0 }
        val novosCount = allClients.count { now - it.createdAt <= thirtyDaysInMillis }

        chipTodos.text = "Todos (${allClients.size})"
        chipFrequentes.text = "Frequentes ($frequentesCount)"
        chipInativos.text = "Inativos ($inativosCount)"
        chipNovos.text = "Novos ($novosCount)"
    }

    /**
     * Abre a fragment de detalhes do cliente
     */
    private fun showClientDetails(client: Client) {
        parentFragmentManager.beginTransaction()
            .replace(
                R.id.fragmentContainer,
                ClientProfileFragment.newInstance(client)
            )
            .addToBackStack(null)
            .commit()
    }

    /**
     * Abre a fragment de cadastro de cliente
     */
    private fun addNewClientFragment() {
        parentFragmentManager.beginTransaction()
            .replace(
                R.id.fragmentContainer,
                NewClientFragment.newInstance()
            )
            .addToBackStack(null)
            .commit()
    }
}