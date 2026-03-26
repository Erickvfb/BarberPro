package com.example.barberpro.model

/**
 * Modelo para resumo de serviços ou produtos em relatórios
 */
data class ServiceSummary(
    val id: String,
    val nome: String,
    val quantidade: Int,      // Número de vendas/usos
    val valorTotal: Double,   // Soma total do período
    val iconRes: Int          // Drawable resource do ícone
)