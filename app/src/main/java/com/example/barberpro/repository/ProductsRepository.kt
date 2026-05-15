package com.example.barberpro.repository

import com.example.barberpro.data.api.ProductRequest
import com.example.barberpro.data.api.RetrofitClient
import com.example.barberpro.model.StockProducts

class ProductsRepository {

    private val apiService = RetrofitClient.apiService

    // LISTAR PRODUTOS
    suspend fun getAllProducts(): Result<List<StockProducts>> {

        return try {

            val response = apiService.getProducts()

            if (response.isSuccessful) {

                val products = response.body()?.data ?: emptyList()

                Result.success(products)

            } else {

                Result.failure(
                    Exception("Erro ao carregar produtos")
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    // BUSCAR PRODUTO POR ID
    suspend fun getProductById(id: String): Result<StockProducts> {

        return try {

            val response = apiService.getProducts()

            if (response.isSuccessful) {

                val product = response.body()?.data
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

    // CRIAR PRODUTO
    suspend fun addProduct(
        request: ProductRequest
    ): Result<StockProducts> {

        return try {

            val response = apiService.createProduct(request)

            if (response.isSuccessful) {

                val createdProduct = response.body()?.data

                if (createdProduct != null) {

                    Result.success(createdProduct)

                } else {

                    Result.failure(
                        Exception("Erro ao criar produto")
                    )
                }

            } else {

                Result.failure(
                    Exception("Erro ao criar produto")
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    // ATUALIZAR PRODUTO
    suspend fun updateProduct(
        productId: String,
        request: ProductRequest
    ): Result<StockProducts> {

        return try {

            val response = apiService.updateProduct(
                productId,
                request
            )

            if (response.isSuccessful) {

                val updatedProduct = response.body()?.data

                if (updatedProduct != null) {

                    Result.success(updatedProduct)

                } else {

                    Result.failure(
                        Exception("Erro ao atualizar produto")
                    )
                }

            } else {

                Result.failure(
                    Exception("Erro ao atualizar produto")
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    // DELETAR PRODUTO
    suspend fun deleteProduct(
        productId: String
    ): Result<Boolean> {

        return try {

            val response = apiService.deleteProduct(productId)

            if (response.isSuccessful) {

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

    // VERIFICAR VENDAS
    suspend fun hasProductInSales(
        productId: String
    ): Boolean {

        return try {

            val response = apiService.hasProductSales(productId)

            response.isSuccessful &&
                    response.body()?.data == true

        } catch (e: Exception) {

            false
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