package com.example.barberpro

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.barberpro.data.api.ApiClient
import com.example.barberpro.data.api.RegisterRequest
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class RegisterBarberActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var barbeariaNomeLayout: TextInputLayout
    private lateinit var barbeariaNomeInput: TextInputEditText
    private lateinit var nomeLayout: TextInputLayout
    private lateinit var nomeInput: TextInputEditText
    private lateinit var emailLayout: TextInputLayout
    private lateinit var emailInput: TextInputEditText
    private lateinit var senhaLayout: TextInputLayout
    private lateinit var senhaInput: TextInputEditText
    private lateinit var confirmarSenhaLayout: TextInputLayout
    private lateinit var confirmarSenhaInput: TextInputEditText
    private lateinit var cadastrarButton: MaterialButton
    private lateinit var loginLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_barber)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        barbeariaNomeLayout = findViewById(R.id.barbeariaNomeLayout)
        barbeariaNomeInput = findViewById(R.id.barbeariaNomeInput)
        nomeLayout = findViewById(R.id.nomeLayout)
        nomeInput = findViewById(R.id.nomeInput)
        emailLayout = findViewById(R.id.emailLayout)
        emailInput = findViewById(R.id.emailInput)
        senhaLayout = findViewById(R.id.senhaLayout)
        senhaInput = findViewById(R.id.senhaInput)
        confirmarSenhaLayout = findViewById(R.id.confirmarSenhaLayout)
        confirmarSenhaInput = findViewById(R.id.confirmarSenhaInput)
        cadastrarButton = findViewById(R.id.cadastrarButton)
        loginLink = findViewById(R.id.loginLink)
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }

        cadastrarButton.setOnClickListener {
            cadastrarConta()
        }

        loginLink.setOnClickListener {
            finish()
        }
    }

    private fun cadastrarConta() {
        val barbeariaNome = barbeariaNomeInput.text.toString().trim()
        val nome = nomeInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val senha = senhaInput.text.toString()
        val confirmarSenha = confirmarSenhaInput.text.toString()

        val phoneInputText = ""
        val phoneLimpo = phoneInputText.replace(Regex("[^\\d]"), "")
        val phoneFinal = if (phoneLimpo.isEmpty()) null else phoneLimpo

        if (!validateInputs(barbeariaNome, nome, email, senha, confirmarSenha)) return

        cadastrarButton.isEnabled = false
        cadastrarButton.text = "Criando conta..."

        val request = RegisterRequest(
            email = email,
            password = senha,
            full_name = nome,
            barbershop_name = barbeariaNome,
            phone = phoneFinal
        )

        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.register(request)

                if (response.isSuccessful) {
                    val body = response.body()

                    Toast.makeText(
                        this@RegisterBarberActivity,
                        body?.message ?: "Conta criada com sucesso!",
                        Toast.LENGTH_SHORT
                    ).show()

                    startActivity(Intent(this@RegisterBarberActivity, LoginActivity::class.java))
                    finish()

                } else {
                    val error = response.errorBody()?.string()
                    Log.e("REGISTER_ERROR", error ?: "Erro desconhecido")

                    Toast.makeText(
                        this@RegisterBarberActivity,
                        error ?: "Erro ao cadastrar",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                Log.e("REGISTER_EXCEPTION", e.message ?: "Erro")

                Toast.makeText(
                    this@RegisterBarberActivity,
                    "Erro: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()

            } finally {
                cadastrarButton.isEnabled = true
                cadastrarButton.text = "Criar conta"
            }
        }
    }

    private fun validateInputs(
        barbeariaNome: String,
        nome: String,
        email: String,
        senha: String,
        confirmarSenha: String
    ): Boolean {

        var isValid = true

        if (barbeariaNome.isEmpty()) {
            barbeariaNomeLayout.error = "Digite o nome da barbearia"
            isValid = false
        } else barbeariaNomeLayout.error = null

        if (nome.isEmpty()) {
            nomeLayout.error = "Digite seu nome completo"
            isValid = false
        } else nomeLayout.error = null

        if (email.isEmpty()) {
            emailLayout.error = "Digite seu e-mail"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = "E-mail inválido"
            isValid = false
        } else emailLayout.error = null

        if (senha.isEmpty()) {
            senhaLayout.error = "Digite sua senha"
            isValid = false
        } else if (senha.length < 8) {
            senhaLayout.error = "Mínimo 8 caracteres"
            isValid = false
        } else senhaLayout.error = null

        if (confirmarSenha.isEmpty()) {
            confirmarSenhaLayout.error = "Confirme sua senha"
            isValid = false
        } else if (confirmarSenha != senha) {
            confirmarSenhaLayout.error = "As senhas não coincidem"
            isValid = false
        } else {
            confirmarSenhaLayout.error = null
        }

        return isValid
    }
}