package com.example.barberpro

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.widget.TextView
import android.content.Intent

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
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupClickListeners() {
        // Login button click
        loginButton.setOnClickListener {
            performLogin()
        }

        // Forgot password click
        forgotPasswordText.setOnClickListener {
            navigateToForgotPassword()
        }

        // Sign up click
        signUpText.setOnClickListener {
            navigateToSignUp()
        }
    }

    private fun performLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (!validateInputs(email, password)) {
            return
        }

        loginButton.isEnabled = false
        loginButton.text = "Entrando..."

        loginButton.postDelayed({

            val intent = Intent(this, MainContainerActivity::class.java)
            startActivity(intent)
            finish()

        }, 1500)
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        // Validar email
        if (email.isEmpty()) {
            emailInputLayout.error = "Por favor, insira seu e-mail ou usuário"
            isValid = false
        } else {
            emailInputLayout.error = null
        }

        // Validar senha
        if (password.isEmpty()) {
            passwordInputLayout.error = "Por favor, insira sua senha"
            isValid = false
        } else if (password.length < 6) {
            passwordInputLayout.error = "A senha deve ter no mínimo 6 caracteres"
            isValid = false
        } else {
            passwordInputLayout.error = null
        }

        return isValid
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
        onBackPressed()
        return true
    }
}