package com.example.barberpro.repository

import com.example.barberpro.model.ProductType
import com.example.barberpro.model.StockProducts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class ProductsRepository {

    private val products = mutableListOf<StockProducts>()

    init {
        loadMockProducts()
    }

    suspend fun getAllProducts(): Result<List<StockProducts>> = withContext(Dispatchers.IO) {
        delay(300)
        Result.success(products.sortedBy { it.name })
    }

    suspend fun getProductById(id: String): Result<StockProducts> = withContext(Dispatchers.IO) {
        delay(200)
        val product = products.find { it.id == id }
        if (product != null) {
            Result.success(product)
        } else {
            Result.failure(Exception("Produto não encontrado"))
        }
    }

    suspend fun addProduct(product: StockProducts): Result<StockProducts> = withContext(Dispatchers.IO) {
        delay(400)
        products.add(product)
        Result.success(product)
    }

    suspend fun updateProduct(product: StockProducts): Result<StockProducts> = withContext(Dispatchers.IO) {
        delay(400)
        val index = products.indexOfFirst { it.id == product.id }
        if (index != -1) {
            products[index] = product
            Result.success(product)
        } else {
            Result.failure(Exception("Produto não encontrado"))
        }
    }

    suspend fun deleteProduct(productId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        delay(300)
        val removed = products.removeIf { it.id == productId }
        if (removed) {
            Result.success(true)
        } else {
            Result.failure(Exception("Produto não encontrado"))
        }
    }

    suspend fun searchProducts(query: String): Result<List<StockProducts>> = withContext(Dispatchers.IO) {
        delay(200)
        val filtered = products.filter {
            it.name.contains(query, ignoreCase = true)
        }
        Result.success(filtered)
    }

    suspend fun hasProductInSales(productId: String): Boolean = withContext(Dispatchers.IO) {
        delay(100)
        false
    }

    private fun loadMockProducts() {
        products.addAll(
            listOf(
                StockProducts(
                    id = "1",
                    name = "Pomada Modeladora Premium",
                    quantity = 20,
                    alertThreshold = 5,
                    unitPrice = 45.0,
                    costPrice = 27.0,
                    type = ProductType.REVENDA
                ),
                StockProducts(
                    id = "2",
                    name = "Óleo para Barba Aromático",
                    quantity = 12,
                    alertThreshold = 8,
                    unitPrice = 58.0,
                    costPrice = 35.0,
                    type = ProductType.REVENDA
                ),
                StockProducts(
                    id = "3",
                    name = "Shampoo Anticaspa",
                    quantity = 5,
                    alertThreshold = 10,
                    unitPrice = 32.0,
                    costPrice = 19.0,
                    type = ProductType.REVENDA
                ),
                StockProducts(
                    id = "4",
                    name = "Cera para Cabelo Forte",
                    quantity = 15,
                    alertThreshold = 5,
                    unitPrice = 38.50,
                    costPrice = 23.0,
                    type = ProductType.REVENDA
                ),
                StockProducts(
                    id = "5",
                    name = "Gel Fixador Profissional",
                    quantity = 8,
                    alertThreshold = 10,
                    unitPrice = 28.90,
                    costPrice = 17.0,
                    type = ProductType.REVENDA
                ),
                StockProducts(
                    id = "6",
                    name = "Espuma de Barbear",
                    quantity = 18,
                    alertThreshold = 12,
                    unitPrice = 22.50,
                    costPrice = 13.50,
                    type = ProductType.INSUMO
                ),
                StockProducts(
                    id = "7",
                    name = "Loção Pós-Barba Refrescante",
                    quantity = 10,
                    alertThreshold = 6,
                    unitPrice = 42.0,
                    costPrice = 25.0,
                    type = ProductType.REVENDA
                ),
                StockProducts(
                    id = "8",
                    name = "Lâminas Descartáveis Pack 10",
                    quantity = 25,
                    alertThreshold = 15,
                    unitPrice = 18.0,
                    costPrice = 10.0,
                    type = ProductType.INSUMO
                ),
                StockProducts(
                    id = "9",
                    name = "Álcool 70% - 1L",
                    quantity = 8,
                    alertThreshold = 5,
                    unitPrice = 15.0,
                    costPrice = 9.0,
                    type = ProductType.INSUMO
                ),
                StockProducts(
                    id = "10",
                    name = "Toalhas Descartáveis Pack 50",
                    quantity = 12,
                    alertThreshold = 8,
                    unitPrice = 25.0,
                    costPrice = 15.0,
                    type = ProductType.INSUMO
                ),
                StockProducts(
                    id = "11",
                    name = "Balm para Barba Hidratante",
                    quantity = 6,
                    alertThreshold = 5,
                    unitPrice = 65.0,
                    costPrice = 39.0,
                    type = ProductType.REVENDA
                ),
                StockProducts(
                    id = "12",
                    name = "Spray Fixador Extra Forte",
                    quantity = 14,
                    alertThreshold = 8,
                    unitPrice = 35.0,
                    costPrice = 21.0,
                    type = ProductType.REVENDA
                )
            )
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: ProductsRepository? = null

        fun getInstance(): ProductsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ProductsRepository().also { INSTANCE = it }
            }
        }
    }
}