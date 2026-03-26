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
            // Voltar para login
            finish()
        }
    }

    private fun cadastrarConta() {
        val barbeariaNome = barbeariaNomeInput.text.toString().trim()
        val nome = nomeInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val senha = senhaInput.text.toString()

        // Validações
        if (!validateInputs(barbeariaNome, nome, email, senha)) {
            return
        }

        // Mostrar loading
        cadastrarButton.isEnabled = false
        cadastrarButton.text = "Criando conta..."

        // TODO: Implementar cadastro real (API/Firebase)
        // Simulação de cadastro
        cadastrarButton.postDelayed({
            Toast.makeText(this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()

            // Navegar para MainActivity ou Login
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }, 1500)
    }

    private fun validateInputs(
        barbeariaNome: String,
        nome: String,
        email: String,
        senha: String
    ): Boolean {
        var isValid = true

        // Validar nome da barbearia
        if (barbeariaNome.isEmpty()) {
            barbeariaNomeLayout.error = "Digite o nome da barbearia"
            isValid = false
        } else {
            barbeariaNomeLayout.error = null
        }

        // Validar nome
        if (nome.isEmpty()) {
            nomeLayout.error = "Digite seu nome completo"
            isValid = false
        } else {
            nomeLayout.error = null
        }

        // Validar email
        if (email.isEmpty()) {
            emailLayout.error = "Digite seu e-mail"
            isValid = false
        } else if (!isValidEmail(email)) {
            emailLayout.error = "E-mail inválido"
            isValid = false
        } else {
            emailLayout.error = null
        }

        // Validar senha
        if (senha.isEmpty()) {
            senhaLayout.error = "Digite sua senha"
            isValid = false
        } else if (senha.length < 8) {
            senhaLayout.error = "A senha deve ter no mínimo 8 caracteres"
            isValid = false
        } else {
            senhaLayout.error = null
        }

        return isValid
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}