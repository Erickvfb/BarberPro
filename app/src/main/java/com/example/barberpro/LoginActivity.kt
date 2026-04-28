package com.example.barberpro

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.barberpro.repository.AuthRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    // Views
    private lateinit var toolbar: MaterialToolbar
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var forgotPasswordText: TextView
    private lateinit var signUpText: TextView

    // Repository
    private val authRepository = AuthRepository.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initializeViews()
        setupToolbar()
        setupClickListeners()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        emailInputLayout = findViewById(R.id.emailInputLayout)
        emailEditText = findViewById(R.id.emailEditText)
        passwordInputLayout = findViewById(R.id.passwordInputLayout)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        forgotPasswordText = findViewById(R.id.forgotPasswordText)
        signUpText = findViewById(R.id.signUpText)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupClickListeners() {

        loginButton.setOnClickListener {
            performLogin()
        }

        forgotPasswordText.setOnClickListener {
            navigateToForgotPassword()
        }

        signUpText.setOnClickListener {
            navigateToSignUp()
        }
    }

    private fun performLogin() {
        val email = emailEditText.text?.toString()?.trim() ?: ""
        val password = passwordEditText.text?.toString()?.trim() ?: ""

        if (!validateInputs(email, password)) return

        setLoadingState(true)

        lifecycleScope.launch {
            try {
                val result = authRepository.login(email, password)

                result.onSuccess {
                    Toast.makeText(
                        this@LoginActivity,
                        "Login realizado com sucesso!",
                        Toast.LENGTH_SHORT
                    ).show()

                    navigateToMain()
                }

                result.onFailure { error ->
                    setLoadingState(false)

                    val errorMessage = when {
                        error is IOException ->
                            "Sem conexão com a internet."
                        error.message?.contains("401", true) == true ->
                            "Email ou senha incorretos."
                        error.message?.contains("timeout", true) == true ->
                            "Servidor demorou para responder."
                        else ->
                            error.message ?: "Erro ao fazer login."
                    }

                    Toast.makeText(
                        this@LoginActivity,
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                setLoadingState(false)

                Toast.makeText(
                    this@LoginActivity,
                    "Erro inesperado: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        // Email
        if (email.isEmpty()) {
            emailInputLayout.error = "Informe seu e-mail"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.error = "E-mail inválido"
            isValid = false
        } else {
            emailInputLayout.error = null
        }

        // Senha
        if (password.isEmpty()) {
            passwordInputLayout.error = "Informe sua senha"
            isValid = false
        } else if (password.length < 8) {
            passwordInputLayout.error = "Mínimo de 8 caracteres"
            isValid = false
        } else {
            passwordInputLayout.error = null
        }

        return isValid
    }

    private fun setLoadingState(isLoading: Boolean) {
        loginButton.isEnabled = !isLoading
        emailEditText.isEnabled = !isLoading
        passwordEditText.isEnabled = !isLoading

        loginButton.text = if (isLoading) "Entrando..." else "Entrar"
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainContainerActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToForgotPassword() {
        val intent = Intent(this, RecoverPasswordActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToSignUp() {
        val intent = Intent(this, RegisterBarberActivity::class.java)
        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}