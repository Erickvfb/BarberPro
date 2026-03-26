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

class RecoverPasswordActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var emailLayout: TextInputLayout
    private lateinit var emailInput: TextInputEditText
    private lateinit var enviarLinkButton: MaterialButton
    private lateinit var loginLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recover_password)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        emailLayout = findViewById(R.id.emailLayout)
        emailInput = findViewById(R.id.emailInput)
        enviarLinkButton = findViewById(R.id.enviarLinkButton)
        loginLink = findViewById(R.id.loginLink)
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }

        enviarLinkButton.setOnClickListener {
            enviarLink()
        }

        loginLink.setOnClickListener {
            finish()
        }
    }

    private fun enviarLink() {
        val email = emailInput.text.toString().trim()

        // Validar email
        if (email.isEmpty()) {
            emailLayout.error = "Digite seu e-mail"
            return
        }

        if (!isValidEmail(email)) {
            emailLayout.error = "E-mail inválido"
            return
        }

        emailLayout.error = null

        // Mostrar loading
        enviarLinkButton.isEnabled = false
        enviarLinkButton.text = "Enviando..."

        // TODO: Implementar envio real de e-mail
        enviarLinkButton.postDelayed({
            Toast.makeText(
                this,
                "Link de recuperação enviado para $email",
                Toast.LENGTH_LONG
            ).show()

            // Navegar para tela de Nova Senha (simulação)
            // Em produção, o link será enviado por e-mail
            val intent = Intent(this, NewPasswordActivity::class.java)
            intent.putExtra("EMAIL", email)
            startActivity(intent)
            finish()
        }, 1500)
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}