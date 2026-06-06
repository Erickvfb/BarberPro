package com.example.barberpro

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.barberpro.data.api.ChangePasswordRequest
import com.example.barberpro.data.api.RetrofitClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class ChangePasswordFragment : Fragment() {

    private lateinit var currentPasswordLayout: TextInputLayout
    private lateinit var currentPasswordInput: TextInputEditText

    private lateinit var newPasswordLayout: TextInputLayout
    private lateinit var newPasswordInput: TextInputEditText

    private lateinit var confirmPasswordLayout: TextInputLayout
    private lateinit var confirmPasswordInput: TextInputEditText

    private lateinit var saveButton: MaterialButton
    private lateinit var backButton: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_change_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideBottomNavigation()
        initViews(view)
        setupValidationListeners()
        setupClicks()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        showBottomNavigation()
    }

    private fun hideBottomNavigation() {
        (requireActivity() as MainContainerActivity)
            .findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                R.id.bottomNavigation
            ).visibility = View.GONE
    }

    private fun showBottomNavigation() {
        (requireActivity() as MainContainerActivity)
            .findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                R.id.bottomNavigation
            ).visibility = View.VISIBLE
    }

    private fun initViews(view: View) {
        currentPasswordLayout = view.findViewById(R.id.currentPasswordLayout)
        currentPasswordInput = view.findViewById(R.id.currentPasswordInput)

        newPasswordLayout = view.findViewById(R.id.newPasswordLayout)
        newPasswordInput = view.findViewById(R.id.newPasswordInput)

        confirmPasswordLayout = view.findViewById(R.id.confirmPasswordLayout)
        confirmPasswordInput = view.findViewById(R.id.confirmPasswordInput)

        saveButton = view.findViewById(R.id.savePasswordButton)
        backButton = view.findViewById(R.id.backButton)
    }

    private fun setupClicks() {
        saveButton.setOnClickListener {
            changePassword()
        }

        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun changePassword() {
        val current = currentPasswordInput.text.toString()
        val newPass = newPasswordInput.text.toString()
        val confirm = confirmPasswordInput.text.toString()

        // Limpar erros anteriores
        currentPasswordLayout.error = null
        newPasswordLayout.error = null
        confirmPasswordLayout.error = null

        // Validações locais
        var valid = true

        if (current.isEmpty()) {
            currentPasswordLayout.error = "Informe a senha atual"
            valid = false
        }

        if (newPass.isEmpty()) {
            newPasswordLayout.error = "Informe a nova senha"
            valid = false
        } else if (newPass.length < 8) {
            newPasswordLayout.error = "Mínimo 8 caracteres"
            valid = false
        }

        if (confirm.isEmpty()) {
            confirmPasswordLayout.error = "Confirme a nova senha"
            valid = false
        } else if (newPass != confirm) {
            confirmPasswordLayout.error = "Senhas não conferem"
            valid = false
        }

        if (!valid) return

        // Validação local: nova igual à atual (antes de ir ao backend)
        // Feita APENAS como otimização — o backend também rejeita se for igual
        if (current == newPass) {
            newPasswordLayout.error = "A nova senha deve ser diferente da senha atual"
            return
        }

        // ✅ Enviar direto para o backend
        // O backend valida a senha atual via signInWithPassword
        // e retorna 401 se incorreta — SEM passar pelo authLimiter
        saveButton.isEnabled = false
        saveButton.text = "Atualizando..."

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val request = ChangePasswordRequest(
                    current_password = current,
                    new_password = newPass
                )

                val response = RetrofitClient.apiService.changePassword(request)

                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Senha alterada com sucesso!",
                        Toast.LENGTH_SHORT
                    ).show()

                    requireActivity().onBackPressedDispatcher.onBackPressed()

                } else {
                    // ✅ Backend retornou erro — mostrar no campo correto
                    val code = response.code()

                    when (code) {
                        401 -> currentPasswordLayout.error = "Senha atual incorreta"
                        400 -> newPasswordLayout.error = "Nova senha inválida"
                        else -> Toast.makeText(
                            requireContext(),
                            "Erro ao alterar senha",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    Log.e("CHANGE_PASSWORD", "Erro $code: ${response.message()}")
                }

            } catch (e: Exception) {
                Log.e("CHANGE_PASSWORD_ERROR", "Exceção: ${e.message}", e)
                Toast.makeText(
                    requireContext(),
                    "Erro de conexão: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

            saveButton.isEnabled = true
            saveButton.text = "Alterar senha"
        }
    }

    private fun setupValidationListeners() {

        currentPasswordInput.doAfterTextChanged {
            currentPasswordLayout.error = null
        }

        newPasswordInput.doAfterTextChanged {
            val senha = it.toString()

            if (senha.isNotEmpty() && senha.length < 8) {
                newPasswordLayout.error = "Mínimo 8 caracteres"
            } else {
                newPasswordLayout.error = null
            }

            val confirmacao = confirmPasswordInput.text.toString()
            if (confirmacao.isNotEmpty()) {
                confirmPasswordLayout.error = if (senha != confirmacao) "Senhas não conferem" else null
            }
        }

        confirmPasswordInput.doAfterTextChanged {
            val senha = newPasswordInput.text.toString()
            val confirmacao = it.toString()

            if (confirmacao.isNotEmpty() && senha != confirmacao) {
                confirmPasswordLayout.error = "Senhas não conferem"
            } else {
                confirmPasswordLayout.error = null
            }
        }
    }
}