package com.example.barberpro

import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.barberpro.model.Client
import com.example.barberpro.repository.ClientsRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.util.*

class NewClientFragment : Fragment() {

    private lateinit var backButton: ImageView
    private lateinit var photoCard: MaterialCardView
    private lateinit var nomeLayout: TextInputLayout
    private lateinit var nomeInput: TextInputEditText
    private lateinit var emailLayout: TextInputLayout
    private lateinit var emailInput: TextInputEditText
    private lateinit var contatoLayout: TextInputLayout
    private lateinit var contatoInput: TextInputEditText
    private lateinit var cadastrarButton: MaterialButton

    private val repository = ClientsRepository.getInstance()

    private var clientId: String? = null
    private var isEditMode = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_new_client, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        loadArguments()
        setupClickListeners()
    }

    private fun initializeViews(view: View) {
        backButton = view.findViewById(R.id.backButton)
        photoCard = view.findViewById(R.id.photoCard)
        nomeLayout = view.findViewById(R.id.nomeLayout)
        nomeInput = view.findViewById(R.id.nomeInput)
        emailLayout = view.findViewById(R.id.emailLayout)
        emailInput = view.findViewById(R.id.emailInput)
        contatoLayout = view.findViewById(R.id.contatoLayout)
        contatoInput = view.findViewById(R.id.contatoInput)
        cadastrarButton = view.findViewById(R.id.cadastrarButton)
    }

    private fun loadArguments() {
        arguments?.let {
            clientId = it.getString("CLIENT_ID")
            isEditMode = clientId != null

            if (isEditMode) {
                cadastrarButton.text = "Atualizar Cliente"
                nomeInput.setText(it.getString("CLIENT_NAME", ""))
                emailInput.setText(it.getString("CLIENT_EMAIL", ""))
                contatoInput.setText(it.getString("CLIENT_PHONE", ""))
            }
        }
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        photoCard.setOnClickListener {
            Toast.makeText(requireContext(), "Selecionar foto (não implementado)", Toast.LENGTH_SHORT).show()
        }

        cadastrarButton.setOnClickListener {
            if (validateInputs()) {
                saveClient()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var valid = true

        val nome = nomeInput.text.toString().trim()
        if (TextUtils.isEmpty(nome)) {
            nomeLayout.error = "Digite o nome do cliente"
            valid = false
        } else nomeLayout.error = null

        val email = emailInput.text.toString().trim()
        if (TextUtils.isEmpty(email)) {
            emailLayout.error = "Digite o e-mail"
            valid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = "E-mail inválido"
            valid = false
        } else emailLayout.error = null

        val phone = contatoInput.text.toString().trim()
        if (TextUtils.isEmpty(phone) || phone.replace("[^0-9]".toRegex(), "").length < 10) {
            contatoLayout.error = "Telefone inválido"
            valid = false
        } else contatoLayout.error = null

        return valid
    }

    private fun saveClient() {
        val client = Client(
            id = clientId ?: UUID.randomUUID().toString(),
            name = nomeInput.text.toString().trim(),
            email = emailInput.text.toString().trim(),
            phone = contatoInput.text.toString().trim()
        )

        cadastrarButton.isEnabled = false
        cadastrarButton.text = if (isEditMode) "Atualizando..." else "Salvando..."

        lifecycleScope.launch {
            val result = if (isEditMode) {
                repository.updateClient(client)
            } else {
                repository.addClient(client)
            }

            result.onSuccess {
                Toast.makeText(
                    requireContext(),
                    if (isEditMode) "Cliente atualizado!" else "Cliente cadastrado!",
                    Toast.LENGTH_SHORT
                ).show()
                parentFragmentManager.popBackStack()
            }

            result.onFailure { error ->
                Toast.makeText(
                    requireContext(),
                    "Erro: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
                cadastrarButton.isEnabled = true
                cadastrarButton.text = if (isEditMode) "Atualizar Cliente" else "Cadastrar Cliente"
            }
        }
    }

    companion object {
        fun newInstance(client: Client? = null): NewClientFragment {
            return NewClientFragment().apply {
                arguments = client?.let {
                    Bundle().apply {
                        putString("CLIENT_ID", it.id)
                        putString("CLIENT_NAME", it.name)
                        putString("CLIENT_EMAIL", it.email)
                        putString("CLIENT_PHONE", it.phone)
                    }
                }
            }
        }
    }
}