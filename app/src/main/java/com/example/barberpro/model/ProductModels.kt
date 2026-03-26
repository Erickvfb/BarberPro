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
 * Categorias de produtos
 */
enum class ProductCategory(val displayName: String, val colorBg: String, val colorText: String) {
    CABELO("Capilar", "#1E3A5F", "#60A5FA"),
    BARBA("Barba", "#1E3A5F", "#60A5FA"),
    LAMINAS("Equipamento", "#1E3A5F", "#60A5FA"),
    LIMPEZA("Limpeza", "#1E3A5F", "#60A5FA"),
    POS_BARBA("Pós-Barba", "#1E3A5F", "#60A5FA"),
    OUTROS("Outros", "#1E3A5F", "#60A5FA");

    companion object {
        fun fromString(value: String): ProductCategory {
            return values().find { it.name.equals(value, ignoreCase = true) } ?: OUTROS
        }
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