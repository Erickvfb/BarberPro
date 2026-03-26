package com.example.barberpro.model

import java.text.SimpleDateFormat
import java.util.*

/**
 * Modelo de Cliente
 */
data class Client(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    var totalSpent: Double = 0.0,
    var visitCount: Int = 0
) {
    /**
     * Retorna data de cadastro formatada
     */
    fun getFormattedCreatedDate(): String {
        val dateFormat = SimpleDateFormat("MMM yyyy", Locale("pt", "BR"))
        return "Cliente desde ${dateFormat.format(Date(createdAt))}"
    }

    // Retorna inicial do nome
    fun getInitial(): String {
        val parts = name.trim().split(" ")
        return if (parts.size >= 2) {
            // Primeira + Última letra
            "${parts.first().first().uppercase()}${parts.last().first().uppercase()}"
        } else {
            // Só um nome: pega 2 primeiras letras
            name.take(2).uppercase()
        }
    }
}

/**
 * Modelo de Histórico do Cliente
 */
data class ClientHistory(
    val id: String,
    val clientId: String,
    val serviceId: String,
    val serviceName: String,
    val date: Long,
    val price: Double,
    val iconRes: Int
)