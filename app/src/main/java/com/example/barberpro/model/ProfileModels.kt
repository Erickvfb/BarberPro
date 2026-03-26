package com.example.barberpro.model

/**
 * Barber Profile model
 */
data class BarberProfile(
    val id: String,
    val barbeariaNome: String,
    val nomeCompleto: String,
    val email: String,
    val telefone: String,
    val dataCadastro: java.util.Date = java.util.Date()
)