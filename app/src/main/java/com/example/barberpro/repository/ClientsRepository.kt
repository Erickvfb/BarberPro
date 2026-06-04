package com.example.barberpro.repository

import android.util.Log
import com.example.barberpro.R
import com.example.barberpro.data.api.ApiService
import com.example.barberpro.data.api.ClientRequest
import com.example.barberpro.data.api.RetrofitClient
import com.example.barberpro.model.Client
import com.example.barberpro.model.ClientHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

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


     //TODO: Implementar lógica de clientes inativos no backend

    suspend fun getInactiveClients(): Result<List<Client>> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Por enquanto retorna lista vazia
            // Futuramente: filtrar clientes sem atendimento há X dias
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

    suspend fun getClientHistory(clientId: String): Result<List<ClientHistory>> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                Log.d("CLIENTS_REPO", "Buscando histórico do cliente: $clientId")

                // 1. Chamar API
                val response = RetrofitClient.apiService.getClientHistory(clientId)

                if (response.isSuccessful && response.body()?.success == true) {
                    // 2. Obter AttendanceRecordResponse do backend
                    val attendanceRecords = response.body()!!.data

                    Log.d("CLIENTS_REPO", "${attendanceRecords.size} registros encontrados na API")

                    // 3. Mapear AttendanceRecordResponse → ClientHistory
                    val clientHistoryList = attendanceRecords.mapNotNull { record ->
                        try {
                            // Extrair informações do serviço
                            val serviceName = record.services?.name ?: "Serviço"
                            val servicePrice = record.service_price ?: record.services?.price ?: 0.0

                            // Converter data do backend (ISO 8601)
                            val date = parseBackendDate(record.finished_at ?: record.created_at)

                            // Selecionar ícone baseado no nome do serviço
                            val iconRes = selectIconForService(serviceName)

                            Log.d("CLIENTS_REPO",
                                "Mapeado: $serviceName - R$ $servicePrice")

                            ClientHistory(
                                id = record.id ?: "",
                                clientId = record.client_id ?: "",
                                serviceId = record.service_id ?: "",
                                serviceName = serviceName,
                                date = date,
                                price = servicePrice,
                                iconRes = iconRes
                            )
                        } catch (e: Exception) {
                            Log.e("CLIENTS_REPO", "Erro ao mapear registro: ${e.message}")
                            null
                        }
                    }

                    Log.d("CLIENTS_REPO", " ${clientHistoryList.size} históricos mapeados com sucesso")

                    if (clientHistoryList.isEmpty()) {
                        Log.d("CLIENTS_REPO", "Cliente não possui histórico de atendimentos")
                    }

                    // 4. Retornar ClientHistory para o Fragment
                    Result.success(clientHistoryList)
                } else {
                    val errorMsg = response.body()?.message ?: response.message()
                    Log.e("CLIENTS_REPO_ERROR", "Erro na resposta: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Log.e("CLIENTS_REPO_ERROR", "Exceção ao buscar histórico: ${e.message}", e)
                Result.failure(e)
            }
        }


    private fun parseBackendDate(dateString: String?): Long {
        if (dateString.isNullOrEmpty()) {
            return System.currentTimeMillis()
        }

        return try {
            // Limpar timezone
            val cleanDate = dateString
                .replace("+00:00", "")
                .replace("Z", "")
                .replace("z", "")

            // Formato ISO 8601
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = dateFormat.parse(cleanDate)

            date?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            Log.w("DATE_PARSER", "Erro ao parsear data: $dateString - ${e.message}")
            System.currentTimeMillis()
        }
    }

    /**
     * Selecionar ícone apropriado baseado no nome do serviço
     */
    private fun selectIconForService(serviceName: String): Int {
        val name = serviceName.lowercase()

        return when {
            name.contains("corte") -> com.example.barberpro.R.drawable.ic_scissors
            name.contains("barba") -> com.example.barberpro.R.drawable.ic_scissors
            name.contains("cabelo") -> com.example.barberpro.R.drawable.ic_scissors
            name.contains("combo") -> com.example.barberpro.R.drawable.ic_scissors
            name.contains("limpeza") -> com.example.barberpro.R.drawable.ic_scissors
            else -> com.example.barberpro.R.drawable.ic_scissors // Padrão
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