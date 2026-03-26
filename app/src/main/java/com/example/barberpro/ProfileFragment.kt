package com.example.barberpro

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.barberpro.model.BarberProfile
import com.example.barberpro.repository.ProfileRepository
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

    private val repository = ProfileRepository.getInstance()
    private var currentProfile: BarberProfile? = null

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
    }

    private fun setupClickListeners() {

        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        subscriptionCard.setOnClickListener {
            val intent = Intent(requireContext(), SubscriptionManagementActivity::class.java)
            startActivity(intent)
        }

        requireView().findViewById<ImageView>(R.id.editBarbeariaButton).setOnClickListener {
            showEditDialog(
                "Nome da Barbearia",
                barbeariaNomeText.text.toString()
            ) { newValue ->
                barbeariaNomeText.text = newValue
                updateProfile()
            }
        }

        requireView().findViewById<ImageView>(R.id.editNomeButton).setOnClickListener {
            showEditDialog(
                "Nome Completo",
                nomeCompletoText.text.toString()
            ) { newValue ->
                nomeCompletoText.text = newValue
                updateProfile()
            }
        }

        requireView().findViewById<ImageView>(R.id.editEmailButton).setOnClickListener {
            showEditDialog(
                "E-mail",
                emailText.text.toString()
            ) { newValue ->
                emailText.text = newValue
                updateProfile()
            }
        }

        requireView().findViewById<ImageView>(R.id.editTelefoneButton).setOnClickListener {
            showEditDialog(
                "Telefone",
                telefoneText.text.toString()
            ) { newValue ->
                telefoneText.text = newValue
                updateProfile()
            }
        }

        changePasswordCard.setOnClickListener {
            showChangePasswordDialog()
        }

        logoutButton.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun loadProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = repository.getProfile()

            result.onSuccess { profile ->
                currentProfile = profile
                displayProfile(profile)
            }

            result.onFailure { error ->
                Toast.makeText(
                    requireContext(),
                    "Erro ao carregar perfil: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun displayProfile(profile: BarberProfile) {
        barbeariaNomeText.text = profile.barbeariaNome
        nomeCompletoText.text = profile.nomeCompleto
        emailText.text = profile.email
        telefoneText.text = formatPhone(profile.telefone)
    }

    private fun showEditDialog(
        fieldName: String,
        currentValue: String,
        onSave: (String) -> Unit
    ) {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_field, null)

        val editText =
            dialogView.findViewById<TextInputEditText>(R.id.editFieldInput)

        editText.setText(currentValue)

        AlertDialog.Builder(requireContext())
            .setTitle("Editar $fieldName")
            .setView(dialogView)
            .setPositiveButton("Salvar") { _, _ ->
                val newValue = editText.text.toString().trim()
                if (newValue.isNotEmpty()) onSave(newValue)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun updateProfile() {
        val updatedProfile = BarberProfile(
            id = currentProfile?.id ?: "1",
            barbeariaNome = barbeariaNomeText.text.toString(),
            nomeCompleto = nomeCompletoText.text.toString(),
            email = emailText.text.toString(),
            telefone = telefoneText.text.toString().replace(Regex("[^0-9]"), ""),
        )

        viewLifecycleOwner.lifecycleScope.launch {
            val result = repository.updateProfile(updatedProfile)

            result.onSuccess {
                Toast.makeText(
                    requireContext(),
                    "Perfil atualizado!",
                    Toast.LENGTH_SHORT
                ).show()
                currentProfile = updatedProfile
            }

            result.onFailure { error ->
                Toast.makeText(
                    requireContext(),
                    "Erro ao atualizar: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showChangePasswordDialog() {
        val dialogView =
            LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_change_password, null)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Alterar Senha")
            .setView(dialogView)
            .setPositiveButton("Alterar") { _, _ ->
                Toast.makeText(
                    requireContext(),
                    "Senha alterada com sucesso!",
                    Toast.LENGTH_SHORT
                ).show()
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
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun formatPhone(phone: String): String {
        return if (phone.length == 11) {
            "(${phone.substring(0, 2)}) ${phone.substring(2, 7)}-${phone.substring(7)}"
        } else {
            phone
        }
    }
}