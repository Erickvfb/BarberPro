package com.example.barberpro.repository

import com.example.barberpro.data.api.ApiService
import com.example.barberpro.data.api.ClientRequest
import com.example.barberpro.data.api.RetrofitClient
import com.example.barberpro.model.Client
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for client management
 * Conectado com a API REST
 */
class ClientsRepository private constructor(
    private val apiService: ApiService = RetrofitClient.apiService
) {

    /**
     * Get all clients
     */
    suspend fun getAllClients(): Result<List<Client>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = apiService.getClients()

            if (response.isSuccessful && response.body()?.success == true) {
                val apiClients = response.body()!!.data
                // Converter de API model para Model local
                val clients = apiClients.map {
                    Client(
                        id = it.id,
                        name = it.name,
                        email = it.email ?: "",
                        phone = it.phone
                    )
                }
                Result.success(clients)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Search clients by name or phone
     */
    suspend fun searchClients(query: String): Result<List<Client>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = apiService.getClients(search = query)

            if (response.isSuccessful && response.body()?.success == true) {
                val apiClients = response.body()!!.data
                val clients = apiClients.map {
                    Client(it.id, it.name, it.email ?: "", it.phone)
                }
                Result.success(clients)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get client by ID
     */
    suspend fun getClientById(id: String): Result<Client> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = apiService.getClient(id)

            if (response.isSuccessful && response.body()?.success == true) {
                val apiClient = response.body()!!.data
                val client = Client(
                    id = apiClient.id,
                    name = apiClient.name,
                    email = apiClient.email ?: "",
                    phone = apiClient.phone
                )
                Result.success(client)
            } else {
                Result.failure(Exception("Cliente não encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Add new client
     */
    suspend fun addClient(client: Client): Result<Client> = withContext(Dispatchers.IO) {
        return@withContext try {
            val request = ClientRequest(
                name = client.name,
                email = client.email.ifEmpty { null },
                phone = client.phone
            )

            val response = apiService.createClient(request)

            if (response.isSuccessful && response.body()?.success == true) {
                val apiClient = response.body()!!.data
                val newClient = Client(
                    id = apiClient.id,
                    name = apiClient.name,
                    email = apiClient.email ?: "",
                    phone = apiClient.phone
                )
                Result.success(newClient)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update client
     */
    suspend fun updateClient(client: Client): Result<Client> = withContext(Dispatchers.IO) {
        return@withContext try {
            val request = ClientRequest(
                name = client.name,
                email = client.email.ifEmpty { null },
                phone = client.phone
            )

            val response = apiService.updateClient(client.id, request)

            if (response.isSuccessful && response.body()?.success == true) {
                val apiClient = response.body()!!.data
                val updatedClient = Client(
                    id = apiClient.id,
                    name = apiClient.name,
                    email = apiClient.email ?: "",
                    phone = apiClient.phone
                )
                Result.success(updatedClient)
            } else {
                Result.failure(Exception("Cliente não encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete client
     */
    suspend fun deleteClient(clientId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = apiService.deleteClient(clientId)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(true)
            } else {
                Result.failure(Exception("Erro ao deletar cliente"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get frequent clients
     * Retorna os primeiros 20 clientes ordenados por nome
     */
    suspend fun getFrequentClients(): Result<List<Client>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = apiService.getClients()

            if (response.isSuccessful && response.body()?.success == true) {
                val apiClients = response.body()!!.data
                val clients = apiClients.map {
                    Client(it.id, it.name, it.email ?: "", it.phone)
                }
                // Retorna os 20 primeiros
                Result.success(clients.take(20))
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get inactive clients
     * TODO: Implementar lógica de clientes inativos no backend
     */
    suspend fun getInactiveClients(): Result<List<Client>> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Por enquanto retorna lista vazia
            // Futuramente: filtrar clientes sem atendimento há X dias
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get new clients
     * Retorna os últimos 10 clientes
     */
    suspend fun getNewClients(): Result<List<Client>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = apiService.getClients()

            if (response.isSuccessful && response.body()?.success == true) {
                val apiClients = response.body()!!.data
                val clients = apiClients.map {
                    Client(it.id, it.name, it.email ?: "", it.phone)
                }
                // Retorna os últimos 10
                Result.success(clients.takeLast(10))
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ClientsRepository? = null

        fun getInstance(): ClientsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ClientsRepository().also { INSTANCE = it }
            }
        }
    }
}