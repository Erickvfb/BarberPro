package com.example.barberpro.model

/**
 * Modelo de Produto para estoque
 */

data class StockProducts(
    val id: String,
    val name: String,
    var quantity: Int,
    val alertThreshold: Int,
    val unitPrice: Double,
    val costPrice: Double,
    val type: ProductType,
    val createdAt: Long = System.currentTimeMillis()
) {

    /**
     * Verifica se está em estoque baixo
     */
    fun isLowStock(): Boolean {
        return quantity <= alertThreshold
    }

    /**
     * Calcula o valor total em estoque
     */
    fun getTotalValue(): Double {
        return quantity * unitPrice
    }

    /**
     * Calcula a margem de lucro
     */
    fun getProfitMargin(): Double {
        return if (costPrice > 0) {
            ((unitPrice - costPrice) / costPrice) * 100
        } else 0.0
    }
}

/**
 * Tipo de produto
 */
enum class ProductType(val displayName: String) {
    REVENDA("Revenda"),
    INSUMO("Insumo");

    companion object {
        fun fromString(value: String): ProductType {
            return values().find { it.name.equals(value, ignoreCase = true) } ?: INSUMO
        }
    }
}