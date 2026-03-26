package com.example.barberpro.util

import java.text.NumberFormat
import java.util.*

/**
 * Utilitário único para formatação de valores monetários
 * Centraliza toda formatação de moeda do app
 */
object CurrencyUtils {

    private val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    /**
     * Formata um valor Double para moeda brasileira
     * @param value Valor a ser formatado
     * @return String formatada (ex: "R$ 45,90")
     */
    fun format(value: Double): String {
        return formatter.format(value)
    }

    /**
     * Formata um valor Int para moeda brasileira
     * @param value Valor a ser formatado
     * @return String formatada (ex: "R$ 45,00")
     */
    fun format(value: Int): String {
        return formatter.format(value.toDouble())
    }

    /**
     * Formata valor sem o símbolo R$
     * @param value Valor a ser formatado
     * @return String formatada (ex: "45,90")
     */
    fun formatWithoutSymbol(value: Double): String {
        return formatter.format(value).replace("R$", "").trim()
    }
}