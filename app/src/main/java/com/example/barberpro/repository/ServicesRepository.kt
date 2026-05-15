package com.example.barberpro.repository

import com.example.barberpro.data.api.RetrofitClient
import com.example.barberpro.data.api.ServiceRequest
import com.example.barberpro.model.Service

class ServicesRepository {

    private val apiService = RetrofitClient.apiService

    // LISTAR SERVIÇOS
    suspend fun getAllServices(): Result<List<Service>> {

        return try {

            val response = apiService.getServices()

            if (response.isSuccessful) {

                val services =
                    response.body()?.data ?: emptyList()

                Result.success(services)

            } else {

                Result.failure(
                    Exception("Erro ao carregar serviços")
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    // BUSCAR SERVIÇO POR ID
    suspend fun getServiceById(
        id: String
    ): Result<Service> {

        return try {

            val response = apiService.getService(id)

            if (response.isSuccessful) {

                val service = response.body()?.data

                if (service != null) {

                    Result.success(service)

                } else {

                    Result.failure(
                        Exception("Serviço não encontrado")
                    )
                }

            } else {

                Result.failure(
                    Exception("Erro ao buscar serviço")
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    // CRIAR SERVIÇO
    suspend fun addService(
        request: ServiceRequest
    ): Result<Service> {

        return try {

            val response =
                apiService.createService(request)

            if (response.isSuccessful) {

                val service =
                    response.body()?.data

                if (service != null) {

                    Result.success(service)

                } else {

                    Result.failure(
                        Exception("Erro ao criar serviço")
                    )
                }

            } else {

                Result.failure(
                    Exception("Erro ao criar serviço")
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    // ATUALIZAR SERVIÇO
    suspend fun updateService(
        id: String,
        request: ServiceRequest
    ): Result<Service> {

        return try {

            val response =
                apiService.updateService(id, request)

            if (response.isSuccessful) {

                val service =
                    response.body()?.data

                if (service != null) {

                    Result.success(service)

                } else {

                    Result.failure(
                        Exception("Erro ao atualizar serviço")
                    )
                }

            } else {

                Result.failure(
                    Exception("Erro ao atualizar serviço")
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    // EXCLUIR SERVIÇO
    suspend fun deleteService(
        serviceId: String
    ): Result<Boolean> {

        return try {

            val response =
                apiService.deleteService(serviceId)

            if (response.isSuccessful) {

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

    // VALIDAÇÃO LOCAL
    suspend fun hasServiceInSales(
        serviceId: String
    ): Boolean {

        return try {

            // futura integração com vendas
            false

        } catch (e: Exception) {

            true
        }
    }

    // PESQUISA
    suspend fun searchServices(
        query: String
    ): Result<List<Service>> {

        return try {

            val response = apiService.getServices()

            if (response.isSuccessful) {

                val services =
                    response.body()?.data ?: emptyList()

                val filtered =
                    services.filter {
                        it.name.contains(
                            query,
                            ignoreCase = true
                        )
                    }

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

    companion object {

        @Volatile
        private var INSTANCE: ServicesRepository? = null

        fun getInstance(): ServicesRepository {

            return INSTANCE ?: synchronized(this) {

                INSTANCE ?: ServicesRepository()
                    .also { INSTANCE = it }
            }
        }
    }
}