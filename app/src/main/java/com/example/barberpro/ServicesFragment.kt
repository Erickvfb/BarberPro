package com.example.barberpro.ui.services

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.barberpro.R
import com.example.barberpro.adapter.ServicosAdapter
import com.example.barberpro.model.Service
import com.example.barberpro.repository.ServicesRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class ServicesFragment : Fragment() {

    private lateinit var backButton: ImageView
    private lateinit var searchEditText: EditText
    private lateinit var servicosRecyclerView: RecyclerView
    private lateinit var fabAddServico: FloatingActionButton

    private lateinit var servicosAdapter: ServicosAdapter

    private val repository = ServicesRepository.getInstance()

    private var allServicos = listOf<Service>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(
            R.layout.fragment_services,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupRecyclerView()
        setupSearch()
        setupClickListeners()
        loadServicos()
    }

    private fun initializeViews(view: View) {

        backButton =
            view.findViewById(R.id.backButton)

        searchEditText =
            view.findViewById(R.id.searchEditText)

        servicosRecyclerView =
            view.findViewById(R.id.servicosRecyclerView)

        fabAddServico =
            view.findViewById(R.id.fabAddServico)
    }

    private fun setupRecyclerView() {

        servicosAdapter = ServicosAdapter(

            onServiceClick = { service ->
                editarServico(service)
            },

            onServiceLongClick = { service ->
                mostrarOpcoes(service)
            }
        )

        servicosRecyclerView.apply {

            layoutManager =
                LinearLayoutManager(requireContext())

            adapter = servicosAdapter
        }
    }

    private fun setupSearch() {

        searchEditText.addTextChangedListener(

            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {}

                override fun afterTextChanged(
                    s: Editable?
                ) {

                    val query =
                        s.toString().trim()

                    filterServicos(query)
                }
            }
        )
    }

    private fun setupClickListeners() {

        fabAddServico.setOnClickListener {

            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    NewServicesFragment.newInstance()
                )
                .addToBackStack(null)
                .commit()
        }

        backButton.setOnClickListener {

            requireActivity()
                .onBackPressedDispatcher
                .onBackPressed()
        }
    }

    private fun loadServicos() {

        viewLifecycleOwner.lifecycleScope.launch {

            val result =
                repository.getAllServices()

            result.onSuccess { servicos ->

                allServicos = servicos

                servicosAdapter.submitList(servicos)
            }

            result.onFailure { error ->

                Toast.makeText(
                    requireContext(),
                    "Erro ao carregar serviços: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun filterServicos(query: String) {

        viewLifecycleOwner.lifecycleScope.launch {

            val result = if (query.isNotEmpty()) {

                repository.searchServices(query)

            } else {

                repository.getAllServices()
            }

            result.onSuccess { services ->

                servicosAdapter.submitList(services)
            }

            result.onFailure { error ->

                Toast.makeText(
                    requireContext(),
                    error.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun editarServico(service: Service) {

        parentFragmentManager.beginTransaction()
            .replace(
                R.id.fragmentContainer,
                NewServicesFragment.newInstance(service)
            )
            .addToBackStack(null)
            .commit()
    }

    private fun mostrarOpcoes(service: Service) {

        val opcoes = arrayOf(
            "Editar",
            "Excluir"
        )

        AlertDialog.Builder(requireContext())
            .setTitle(service.name)

            .setItems(opcoes) { _, which ->

                when (which) {

                    0 -> editarServico(service)

                    1 -> confirmarExclusao(service)
                }
            }
            .show()
    }

    private fun confirmarExclusao(service: Service) {

        AlertDialog.Builder(requireContext())

            .setTitle("Excluir Serviço")

            .setMessage(
                "Tem certeza que deseja excluir ${service.name}?"
            )

            .setPositiveButton("Excluir") { _, _ ->

                excluirServico(service)
            }

            .setNegativeButton(
                "Cancelar",
                null
            )

            .show()
    }

    private fun excluirServico(service: Service) {

        viewLifecycleOwner.lifecycleScope.launch {

            try {

                // PRIMEIRA CAMADA
                val hasSales =
                    repository.hasServiceInSales(service.id)

                if (hasSales) {

                    Toast.makeText(
                        requireContext(),
                        "Este serviço possui vendas vinculadas e não pode ser excluído",
                        Toast.LENGTH_LONG
                    ).show()

                    return@launch
                }

                // SEGUNDA CAMADA
                val result =
                    repository.deleteService(service.id)

                result.onSuccess {

                    Toast.makeText(
                        requireContext(),
                        "Serviço excluído",
                        Toast.LENGTH_SHORT
                    ).show()

                    loadServicos()
                }

                result.onFailure { error ->

                    Toast.makeText(
                        requireContext(),
                        error.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {

                Toast.makeText(
                    requireContext(),
                    e.message ?: "Erro inesperado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onResume() {

        super.onResume()

        loadServicos()
    }
}