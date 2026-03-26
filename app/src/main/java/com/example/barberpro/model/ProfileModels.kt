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
    val especialidade: String,
    val photoUrl: String? = null,
    val endereco: String? = null,
    val cidade: String? = null,
    val estado: String? = null,
    val cep: String? = null,
    val bio: String? = null,
    val instagram: String? = null,
    val facebook: String? = null,
    val dataCadastro: java.util.Date = java.util.Date()
)