package com.example.barberpro

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterBarber : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_barber)

        // Botão "Entre agora" - volta para o login
        findViewById<TextView>(R.id.txtLogin).setOnClickListener {
            finish() // Volta para a tela anterior (Login)
        }

        // Botão de cadastrar
        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            cadastrarBarbearia()
        }
    }

    private fun cadastrarBarbearia() {
        val nomeBarbearia = findViewById<EditText>(R.id.inputBarbearia).text.toString()
        val nomeCompleto = findViewById<EditText>(R.id.inputNome).text.toString()
        val email = findViewById<EditText>(R.id.inputEmail).text.toString()
        val senha = findViewById<EditText>(R.id.inputSenha).text.toString()

        // Validações básicas
        if (nomeBarbearia.isEmpty()) {
            Toast.makeText(this, "Digite o nome da barbearia", Toast.LENGTH_SHORT).show()
            return
        }

        if (nomeCompleto.isEmpty()) {
            Toast.makeText(this, "Digite seu nome completo", Toast.LENGTH_SHORT).show()
            return
        }

        if (email.isEmpty()) {
            Toast.makeText(this, "Digite seu e-mail", Toast.LENGTH_SHORT).show()
            return
        }

        if (senha.isEmpty() || senha.length < 8) {
            Toast.makeText(this, "A senha deve ter no mínimo 8 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO: Aqui você implementa a lógica de cadastro (API, Firebase, etc)
        Toast.makeText(this, "Barbearia cadastrada com sucesso!", Toast.LENGTH_SHORT).show()

        // Após cadastro bem-sucedido, pode navegar para MainActivity ou Login
        finish() // Volta para login
    }
}