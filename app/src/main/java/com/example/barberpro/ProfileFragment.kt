package com.example.barberpro

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.barberpro.data.api.RetrofitClient
import com.example.barberpro.data.api.UpdateProfileRequest
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch


class ProfileFragment : Fragment() {

    private lateinit var backButton: ImageView
    private lateinit var barbeariaNomeText: TextView
    private lateinit var nomeCompletoText: TextView
    private lateinit var emailText: TextView
    private lateinit var telefoneText: TextView
    private lateinit var changePasswordCard: MaterialCardView
    private lateinit var logoutButton: MaterialButton
    private lateinit var subscriptionCard: MaterialCardView

    private lateinit var editBarbeariaButton: ImageView
    private lateinit var editNomeButton: ImageView
    private lateinit var editTelefoneButton: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupClickListeners()
        loadProfile()
    }

    private fun initializeViews(view: View) {
        backButton = view.findViewById(R.id.backButton)
        barbeariaNomeText = view.findViewById(R.id.barbeariaNomeText)
        nomeCompletoText = view.findViewById(R.id.nomeCompletoText)
        emailText = view.findViewById(R.id.emailText)
        telefoneText = view.findViewById(R.id.telefoneText)

        changePasswordCard = view.findViewById(R.id.changePasswordCard)
        logoutButton = view.findViewById(R.id.logoutButton)
        subscriptionCard = view.findViewById(R.id.subscriptionCard)

        editBarbeariaButton = view.findViewById(R.id.editBarbeariaButton)
        editNomeButton = view.findViewById(R.id.editNomeButton)
        editTelefoneButton = view.findViewById(R.id.editTelefoneButton)
    }

    private fun setupClickListeners() {

        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        subscriptionCard.setOnClickListener {
            val intent = Intent(requireContext(), SubscriptionManagementActivity::class.java)
            startActivity(intent)
        }

        logoutButton.setOnClickListener {
            showLogoutDialog()
        }

        changePasswordCard.setOnClickListener {

            val fragment = ChangePasswordFragment()

            parentFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }

        editBarbeariaButton.setOnClickListener {
            showEditDialog(
                "Nome da Barbearia",
                barbeariaNomeText.text.toString()
            ) {
                barbeariaNomeText.text = it
                updateProfile()
            }
        }

        editNomeButton.setOnClickListener {
            showEditDialog(
                "Nome Completo",
                nomeCompletoText.text.toString()
            ) {
                nomeCompletoText.text = it
                updateProfile()
            }
        }


        editTelefoneButton.setOnClickListener {
            showEditDialog(
                "Telefone",
                telefoneText.text.toString()
            ) {
                telefoneText.text = it
                updateProfile()
            }
        }
    }

    private fun loadProfile() {
        viewLifecycleOwner.lifecycleScope.launch {

            try {

                val response = RetrofitClient.apiService.getProfile()

                Log.d("PROFILE_DEBUG", "Code: ${response.code()}")
                Log.d("PROFILE_DEBUG", "Body: ${response.body()}")

                if (response.isSuccessful) {

                    val user = response.body()?.data

                    if (user != null) {

                        displayProfile(
                            barbeariaNome = user.barbershop_name,
                            nomeCompleto = user.full_name,
                            email = user.email,
                            telefone = user.phone
                        )

                    } else {
                        showError("Usuário não encontrado")
                    }

                } else {
                    showError("Erro ${response.code()}")
                }

            } catch (e: Exception) {

                Log.e("PROFILE_EXCEPTION", e.message ?: "Erro")

                showError("Erro: ${e.message}")
            }
        }
    }

    private fun displayProfile(
        barbeariaNome: String,
        nomeCompleto: String,
        email: String,
        telefone: String?
    ) {

        barbeariaNomeText.text = barbeariaNome
        nomeCompletoText.text = nomeCompleto
        emailText.text = email

        telefoneText.text =
            if (telefone.isNullOrEmpty()) {
                "Não informado"
            } else {
                formatPhone(telefone)
            }
    }

    private fun updateProfile() {

        viewLifecycleOwner.lifecycleScope.launch {

            try {

                val request = UpdateProfileRequest(
                    full_name = nomeCompletoText.text.toString(),
                    barbershop_name = barbeariaNomeText.text.toString(),
                    email = emailText.text.toString(),
                    phone = telefoneText.text.toString()
                )

                val response = RetrofitClient.apiService.updateProfile(request)

                if (response.isSuccessful) {

                    Toast.makeText(
                        requireContext(),
                        "Perfil atualizado com sucesso!",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {

                    Toast.makeText(
                        requireContext(),
                        "Erro ao atualizar perfil",
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

    private fun showEditDialog(
        title: String,
        currentValue: String,
        onSave: (String) -> Unit
    ) {

        val input = TextInputEditText(requireContext())

        input.setText(currentValue)

        input.setTextColor(Color.BLACK)

        input.setHintTextColor(Color.GRAY)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setView(input)
            .setPositiveButton("Salvar") { _, _ ->

                val newValue = input.text.toString().trim()

                if (newValue.isNotEmpty()) {
                    onSave(newValue)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showLogoutDialog() {

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sair da Conta")
            .setMessage("Tem certeza que deseja sair?")
            .setPositiveButton("Sair") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun performLogout() {

        RetrofitClient.clearToken()

        val intent = Intent(requireContext(), LoginActivity::class.java)

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)

        requireActivity().finish()
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun formatPhone(phone: String): String {

        return if (phone.length == 11) {

            "(${phone.substring(0, 2)}) ${
                phone.substring(2, 7)
            }-${phone.substring(7)}"

        } else {
            phone
        }
    }
}