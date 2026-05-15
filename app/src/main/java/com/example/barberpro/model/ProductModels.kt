package com.example.barberpro.model
import com.google.gson.annotations.SerializedName
/**
 * Modelo de Produto para estoque
 */

data class StockProducts(

    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("quantity")
    var quantity: Int,

    @SerializedName("alert_threshold")
    val alertThreshold: Int,

    @SerializedName("unit_price")
    val unitPrice: Double,

    @SerializedName("cost_price")
    val costPrice: Double,

    @SerializedName("type")
    val type: ProductType,

    @SerializedName("created_at")
    val createdAt: String? = null

) {

    // Verifica se está em estoque baixo

    fun isLowStock(): Boolean {
        return quantity <= alertThreshold
    }


    //Calcula o valor total em estoque
    fun getTotalValue(): Double {
        return quantity * unitPrice
    }

    //Calcula a margem de lucro
    fun getProfitMargin(): Double {
        return if (costPrice > 0) {
            ((unitPrice - costPrice) / costPrice) * 100
        } else {
            0.0
        }
    }
}

//Tipo de produto
enum class ProductType(val displayName: String) {
    REVENDA("Revenda"),
    INSUMO("Insumo");

    companion object {

        fun fromString(value: String): ProductType {
            return values().find {
                it.name.equals(value, ignoreCase = true)
            } ?: INSUMO
        }
    }
}