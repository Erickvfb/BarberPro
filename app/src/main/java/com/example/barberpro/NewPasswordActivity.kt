package com.example.barberpro

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class NewPasswordActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var emailInput: TextInputEditText
    private lateinit var novaSenhaLayout: TextInputLayout
    private lateinit var novaSenhaInput: TextInputEditText
    private lateinit var confirmarSenhaLayout: TextInputLayout
    private lateinit var confirmarSenhaInput: TextInputEditText
    private lateinit var salvarSenhaButton: MaterialButton
    private lateinit var loginLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_password)

        initializeViews()
        setupClickListeners()

        // Pegar email do intent
        val email = intent.getStringExtra("EMAIL") ?: "joao.barbeiro@exemplo.com"
        emailInput.setText(email)
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        emailInput = findViewById(R.id.emailInput)
        novaSenhaLayout = findViewById(R.id.novaSenhaLayout)
        novaSenhaInput = findViewById(R.id.novaSenhaInput)
        confirmarSenhaLayout = findViewById(R.id.confirmarSenhaLayout)
        confirmarSenhaInput = findViewById(R.id.confirmarSenhaInput)
        salvarSenhaButton = findViewById(R.id.salvarSenhaButton)
        loginLink = findViewById(R.id.loginLink)
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }

        salvarSenhaButton.setOnClickListener {
            salvarNovaSenha()
        }

        loginLink.setOnClickListener {
            voltarParaLogin()
        }
    }

    private fun salvarNovaSenha() {
        val novaSenha = novaSenhaInput.text.toString()
        val confirmarSenha = confirmarSenhaInput.text.toString()

        // Validações
        if (!validatePasswords(novaSenha, confirmarSenha)) {
            return
        }

        // Mostrar loading
        salvarSenhaButton.isEnabled = false
        salvarSenhaButton.text = "Salvando..."

        // TODO: Implementar alteração real de senha
        salvarSenhaButton.postDelayed({
            Toast.makeText(
                this,
                "Senha alterada com sucesso!",
                Toast.LENGTH_SHORT
            ).show()

            // Voltar para login
            voltarParaLogin()
        }, 1500)
    }

    private fun validatePasswords(novaSenha: String, confirmarSenha: String): Boolean {
        var isValid = true

        // Validar nova senha
        if (novaSenha.isEmpty()) {
            novaSenhaLayout.error = "Digite a nova senha"
            isValid = false
        } else if (novaSenha.length < 8) {
            novaSenhaLayout.error = "A senha deve ter no mínimo 8 caracteres"
            isValid = false
        } else {
            novaSenhaLayout.error = null
        }

        // Validar confirmação
        if (confirmarSenha.isEmpty()) {
            confirmarSenhaLayout.error = "Confirme a nova senha"
            isValid = false
        } else if (novaSenha != confirmarSenha) {
            confirmarSenhaLayout.error = "As senhas não coincidem"
            isValid = false
        } else {
            confirmarSenhaLayout.error = null
        }

        return isValid
    }

    private fun voltarParaLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}