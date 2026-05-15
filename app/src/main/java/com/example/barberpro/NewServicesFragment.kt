package com.example.barberpro.ui.services

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.barberpro.R
import com.example.barberpro.data.api.ServiceRequest
import com.example.barberpro.model.Service
import com.example.barberpro.repository.ServicesRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class NewServicesFragment : Fragment() {

    companion object {

        private const val ARG_SERVICE_ID = "service_id"

        fun newInstance(
            service: Service? = null
        ): NewServicesFragment {

            return NewServicesFragment().apply {

                arguments = Bundle().apply {

                    service?.let {

                        putString(
                            ARG_SERVICE_ID,
                            it.id
                        )
                    }
                }
            }
        }
    }

    private lateinit var nomeInput: EditText
    private lateinit var precoInput: EditText

    private lateinit var salvarButton: MaterialButton
    private lateinit var excluirButton: MaterialButton

    private val repository =
        ServicesRepository.getInstance()

    private var serviceToEdit: Service? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.getString(ARG_SERVICE_ID)
            ?.let { serviceId ->

                lifecycleScope.launch {

                    repository.getServiceById(serviceId)

                        .onSuccess { service ->

                            serviceToEdit = service

                            if (isAdded) {

                                fillForm(service)
                            }
                        }

                        .onFailure {

                            Toast.makeText(
                                requireContext(),
                                it.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(
            R.layout.fragment_new_services,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(
            view,
            savedInstanceState
        )

        initializeViews(view)

        setupClickListeners()

        serviceToEdit?.let {
            fillForm(it)
        }
    }

    private fun initializeViews(view: View) {

        nomeInput =
            view.findViewById(R.id.nomeInput)

        precoInput =
            view.findViewById(R.id.precoInput)

        salvarButton =
            view.findViewById(R.id.salvarButton)

        excluirButton =
            view.findViewById(R.id.excluirButton)
    }

    private fun setupClickListeners() {

        salvarButton.setOnClickListener {

            saveService()
        }

        excluirButton.setOnClickListener {

            confirmDelete()
        }

        view?.findViewById<View>(R.id.backButton)
            ?.setOnClickListener {

                parentFragmentManager.popBackStack()
            }
    }

    private fun fillForm(service: Service) {

        nomeInput.setText(service.name)

        precoInput.setText(
            service.price.toString()
        )

        excluirButton.visibility = View.VISIBLE
    }

    private fun saveService() {

        val name =
            nomeInput.text.toString().trim()

        val price =
            precoInput.text.toString()
                .toDoubleOrNull() ?: 0.0

        // VALIDAÇÕES

        if (name.isEmpty()) {

            nomeInput.error =
                "Digite o nome do serviço"

            return
        }

        if (price <= 0) {

            precoInput.error =
                "Preço inválido"

            return
        }

        salvarButton.isEnabled = false
        salvarButton.text = "Salvando..."

        lifecycleScope.launch {

            val request = ServiceRequest(
                name = name,
                price = price
            )

            val result =
                if (serviceToEdit == null) {

                    repository.addService(request)

                } else {

                    repository.updateService(
                        serviceToEdit!!.id,
                        request
                    )
                }

            result.onSuccess {

                Toast.makeText(
                    requireContext(),
                    "Serviço salvo com sucesso!",
                    Toast.LENGTH_SHORT
                ).show()

                parentFragmentManager.popBackStack()

            }.onFailure {

                Toast.makeText(
                    requireContext(),
                    it.message,
                    Toast.LENGTH_LONG
                ).show()

                salvarButton.isEnabled = true
                salvarButton.text = "Salvar"
            }
        }
    }

    private fun confirmDelete() {

        serviceToEdit?.let { service ->

            MaterialAlertDialogBuilder(
                requireContext()
            )
                .setTitle("Excluir Serviço")

                .setMessage(
                    "Deseja excluir ${service.name}?"
                )

                .setPositiveButton("Excluir") { _, _ ->

                    deleteService(service)
                }

                .setNegativeButton(
                    "Cancelar",
                    null
                )

                .show()
        }
    }

    private fun deleteService(
        service: Service
    ) {

        lifecycleScope.launch {

            try {

                // PRIMEIRA CAMADA:
                // validação local

                val hasSales =
                    repository.hasServiceInSales(
                        service.id
                    )

                if (hasSales) {

                    Toast.makeText(
                        requireContext(),
                        "Este serviço possui vendas vinculadas e não pode ser excluído",
                        Toast.LENGTH_LONG
                    ).show()

                    return@launch
                }

                // SEGUNDA CAMADA:
                // backend também valida

                repository.deleteService(service.id)

                    .onSuccess {

                        Toast.makeText(
                            requireContext(),
                            "Serviço excluído com sucesso",
                            Toast.LENGTH_SHORT
                        ).show()

                        parentFragmentManager.popBackStack()
                    }

                    .onFailure {

                        Toast.makeText(
                            requireContext(),
                            it.message
                                ?: "Erro ao excluir serviço",
                            Toast.LENGTH_LONG
                        ).show()
                    }

            } catch (e: Exception) {

                Toast.makeText(
                    requireContext(),
                    e.message
                        ?: "Erro inesperado",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}