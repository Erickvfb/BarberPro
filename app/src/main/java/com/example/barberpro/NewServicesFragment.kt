package com.example.barberpro.ui.services

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.barberpro.R
import com.example.barberpro.model.Service
import com.example.barberpro.repository.ServicesRepository
import kotlinx.coroutines.launch
import java.util.*

class NewServicesFragment (private val serviceToEdit: Service? = null) : Fragment() {

    private lateinit var nomeInput: EditText
    private lateinit var precoInput: EditText
    private lateinit var salvarButton: Button
    private lateinit var excluirButton: Button

    private val repository = ServicesRepository.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_new_services, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nomeInput = view.findViewById(R.id.nomeInput)
        precoInput = view.findViewById(R.id.precoInput)
        salvarButton = view.findViewById(R.id.salvarButton)
        excluirButton = view.findViewById(R.id.excluirButton)

        serviceToEdit?.let {
            nomeInput.setText(it.name)
            precoInput.setText(it.price.toString())
            excluirButton.visibility = View.VISIBLE
        }

        salvarButton.setOnClickListener { saveService() }
        excluirButton.setOnClickListener { deleteService() }
    }

    private fun saveService() {
        val name = nomeInput.text.toString().trim()
        val price = precoInput.text.toString().toDoubleOrNull() ?: 0.0

        if (name.isEmpty() || price <= 0) {
            Toast.makeText(requireContext(), "Preencha todos os campos corretamente", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            if (serviceToEdit != null) {
                val updated = serviceToEdit.copy(name = name, price = price)
                repository.updateService(updated).onSuccess {
                    Toast.makeText(requireContext(), "Serviço atualizado", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }.onFailure { Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show() }
            } else {
                val newService = Service(UUID.randomUUID().toString(), name, price)
                repository.addService(newService).onSuccess {
                    Toast.makeText(requireContext(), "Serviço adicionado", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }.onFailure { Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show() }
            }
        }
    }

    private fun deleteService() {
        serviceToEdit?.let { service ->
            lifecycleScope.launch {
                if (repository.hasServiceInSales(service.id)) {
                    Toast.makeText(requireContext(), "Serviço possui vendas e não pode ser excluído", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                repository.deleteService(service.id).onSuccess {
                    Toast.makeText(requireContext(), "Serviço excluído", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }.onFailure { Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show() }
            }
        }
    }

    companion object {
        fun newInstance(service: Service? = null) = NewServicesFragment(service)
    }
}