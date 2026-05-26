package com.example.barberpro.repository

import com.example.barberpro.data.api.RetrofitClient
import com.example.barberpro.data.api.ServiceRequest
import com.example.barberpro.model.Service
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ServicesRepository private constructor() {

    private val apiService = RetrofitClient.apiService

    suspend fun getAllServices(): Result<List<Service>> {

        return withContext(Dispatchers.IO) {

            try {

                val response = apiService.getServices()

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
                                ?: "Erro ao carregar serviços"
                        )
                    )
                }

            } catch (e: Exception) {

                Result.failure(e)
            }
        }
    }

    suspend fun searchServices(
        query: String
    ): Result<List<Service>> {

        return withContext(Dispatchers.IO) {

            try {

                val response = apiService.getServices()

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
                        Exception("Erro ao pesquisar serviços")
                    )
                }

            } catch (e: Exception) {

                Result.failure(e)
            }
        }
    }

    suspend fun getServiceById(
        id: String
    ): Result<Service> {

        return withContext(Dispatchers.IO) {

            try {

                val response =
                    apiService.getService(id)

                if (
                    response.isSuccessful &&
                    response.body()?.success == true
                ) {

                    response.body()?.data?.let {

                        Result.success(it)

                    } ?: Result.failure(
                        Exception("Serviço não encontrado")
                    )

                } else {

                    Result.failure(
                        Exception("Erro ao buscar serviço")
                    )
                }

            } catch (e: Exception) {

                Result.failure(e)
            }
        }
    }

    suspend fun addService(
        request: ServiceRequest
    ): Result<Service> {

        return withContext(Dispatchers.IO) {

            try {

                val response =
                    apiService.createService(request)

                if (
                    response.isSuccessful &&
                    response.body()?.success == true
                ) {

                    response.body()?.data?.let {

                        Result.success(it)

                    } ?: Result.failure(
                        Exception("Serviço inválido")
                    )

                } else {

                    Result.failure(
                        Exception("Erro ao criar serviço")
                    )
                }

            } catch (e: Exception) {

                Result.failure(e)
            }
        }
    }

    suspend fun updateService(
        id: String,
        request: ServiceRequest
    ): Result<Service> {

        return withContext(Dispatchers.IO) {

            try {

                val response =
                    apiService.updateService(
                        id,
                        request
                    )

                if (
                    response.isSuccessful &&
                    response.body()?.success == true
                ) {

                    response.body()?.data?.let {

                        Result.success(it)

                    } ?: Result.failure(
                        Exception("Serviço inválido")
                    )

                } else {

                    Result.failure(
                        Exception("Erro ao atualizar serviço")
                    )
                }

            } catch (e: Exception) {

                Result.failure(e)
            }
        }
    }

    suspend fun deleteService(
        serviceId: String
    ): Result<Boolean> {

        return withContext(Dispatchers.IO) {

            try {

                val response =
                    apiService.deleteService(serviceId)

                if (
                    response.isSuccessful &&
                    response.body()?.success == true
                ) {

                    Result.success(true)

                } else {

                    Result.failure(
                        Exception("Erro ao excluir serviço")
                    )
                }

            } catch (e: Exception) {

                Result.failure(e)
            }
        }
    }

    suspend fun hasServiceInSales(
        serviceId: String
    ): Boolean =
        withContext(Dispatchers.IO) {

            try {

                val response =
                    apiService.hasServiceSales(serviceId)

                if (response.isSuccessful) {

                    response.body()?.data ?: false

                } else {

                    false
                }

            } catch (e: Exception) {

                false
            }
        }

    companion object {

        @Volatile
        private var INSTANCE: ServicesRepository? = null

        fun getInstance(): ServicesRepository {

            return INSTANCE ?: synchronized(this) {

                INSTANCE ?: ServicesRepository().also {
                    INSTANCE = it
                }
            }
        }
    }
}