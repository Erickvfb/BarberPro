package com.example.barberpro.repository

import com.example.barberpro.data.api.ProductRequest
import com.example.barberpro.data.api.RetrofitClient
import com.example.barberpro.model.StockProducts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductsRepository private constructor() {

    private val apiService = RetrofitClient.apiService

    suspend fun getAllProducts(): Result<List<StockProducts>> {

        return withContext(Dispatchers.IO) {

            try {

                val response = apiService.getProducts()

                if (
                    response.isSuccessful &&
                    response.body()?.success == true
                ) {

                    Result.success(
                        response.body()?.data ?: emptyList()
                    )

                } else {

                    Result.failure(
                        Exception(
                            response.body()?.message
                                ?: "Erro ao carregar produtos"
                        )
                    )
                }

            } catch (e: Exception) {

                Result.failure(e)
            }
        }
    }

    suspend fun searchProducts(
        query: String
    ): Result<List<StockProducts>> {

        return withContext(Dispatchers.IO) {

            try {

                val response = apiService.getProducts()

                if (
                    response.isSuccessful &&
                    response.body()?.success == true
                ) {

                    val filtered =
                        response.body()?.data
                            ?.filter {

                                it.name.contains(
                                    query,
                                    ignoreCase = true
                                )
                            } ?: emptyList()

                    Result.success(filtered)

                } else {

                    Result.failure(
                        Exception("Erro ao pesquisar produtos")
                    )
                }

            } catch (e: Exception) {

                Result.failure(e)
            }
        }
    }

    suspend fun getProductById(
        id: String
    ): Result<StockProducts> {

        return withContext(Dispatchers.IO) {

            try {

                val response = apiService.getProducts()

                if (
                    response.isSuccessful &&
                    response.body()?.success == true
                ) {

                    val product =
                        response.body()?.data
                            ?.find { it.id == id }

                    if (product != null) {

                        Result.success(product)

                    } else {

                        Result.failure(
                            Exception("Produto não encontrado")
                        )
                    }

                } else {

                    Result.failure(
                        Exception("Erro ao buscar produto")
                    )
                }

            } catch (e: Exception) {

                Result.failure(e)
            }
        }
    }

    suspend fun addProduct(
        request: ProductRequest
    ): Result<StockProducts> {

        return withContext(Dispatchers.IO) {

            try {

                val response =
                    apiService.createProduct(request)

                if (
                    response.isSuccessful &&
                    response.body()?.success == true
                ) {

                    response.body()?.data?.let {

                        Result.success(it)

                    } ?: Result.failure(
                        Exception("Produto inválido")
                    )

                } else {

                    Result.failure(
                        Exception("Erro ao criar produto")
                    )
                }

            } catch (e: Exception) {

                Result.failure(e)
            }
        }
    }

    suspend fun updateProduct(
        productId: String,
        request: ProductRequest
    ): Result<StockProducts> {

        return withContext(Dispatchers.IO) {

            try {

                val response =
                    apiService.updateProduct(
                        productId,
                        request
                    )

                if (
                    response.isSuccessful &&
                    response.body()?.success == true
                ) {

                    response.body()?.data?.let {

                        Result.success(it)

                    } ?: Result.failure(
                        Exception("Produto inválido")
                    )

                } else {

                    Result.failure(
                        Exception("Erro ao atualizar produto")
                    )
                }

            } catch (e: Exception) {

                Result.failure(e)
            }
        }
    }

    suspend fun hasProductInSales(
        productId: String
    ): Boolean =
        withContext(Dispatchers.IO) {

            try {

                val response =
                    apiService.hasProductSales(productId)

                if (response.isSuccessful) {

                    response.body()?.data ?: false

                } else {

                    false
                }

            } catch (e: Exception) {

                false
            }
        }

    suspend fun deleteProduct(
        productId: String
    ): Result<Boolean> {

        return withContext(Dispatchers.IO) {

            try {

                val response =
                    apiService.deleteProduct(productId)

                if (
                    response.isSuccessful &&
                    response.body()?.success == true
                ) {

                    Result.success(true)

                } else {

                    Result.failure(
                        Exception("Erro ao excluir produto")
                    )
                }

            } catch (e: Exception) {

                Result.failure(e)
            }
        }
    }

    companion object {

        @Volatile
        private var INSTANCE: ProductsRepository? = null

        fun getInstance(): ProductsRepository {

            return INSTANCE ?: synchronized(this) {

                INSTANCE ?: ProductsRepository().also {
                    INSTANCE = it
                }
            }
        }
    }
}