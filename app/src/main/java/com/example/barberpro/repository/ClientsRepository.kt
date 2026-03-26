package com.example.barberpro.repository

import com.example.barberpro.model.Client
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Repository for client management
 */
class ClientsRepository private constructor() {

    private val clients = mutableListOf<Client>()

    init {
        loadMockClients()
    }

    /**
     * Get all clients
     */
    suspend fun getAllClients(): Result<List<Client>> = withContext(Dispatchers.IO) {
        try {
            delay(500)
            Result.success(clients.sortedBy { it.name }.toList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Search clients by name or phone
     */
    suspend fun searchClients(query: String): Result<List<Client>> = withContext(Dispatchers.IO) {
        try {
            delay(300)
            val filtered = clients.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.phone.contains(query)
            }
            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get client by ID
     */
    suspend fun getClientById(id: String): Result<Client> = withContext(Dispatchers.IO) {
        try {
            delay(300)
            clients.find { it.id == id }
                ?.let { Result.success(it) }
                ?: Result.failure(Exception("Cliente não encontrado"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Add new client
     */
    suspend fun addClient(client: Client): Result<Client> = withContext(Dispatchers.IO) {
        try {
            delay(600)
            clients.add(client)
            Result.success(client)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update client
     */
    suspend fun updateClient(client: Client): Result<Client> = withContext(Dispatchers.IO) {
        try {
            delay(600)
            val index = clients.indexOfFirst { it.id == client.id }
            if (index != -1) {
                clients[index] = client
                Result.success(client)
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
        try {
            delay(400)
            Result.success(clients.removeIf { it.id == clientId })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get frequent clients (mock)
     */
    suspend fun getFrequentClients(): Result<List<Client>> = withContext(Dispatchers.IO) {
        try {
            delay(400)
            Result.success(clients.take(20))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get inactive clients (mock)
     */
    suspend fun getInactiveClients(): Result<List<Client>> = withContext(Dispatchers.IO) {
        try {
            delay(400)
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get new clients (mock)
     */
    suspend fun getNewClients(): Result<List<Client>> = withContext(Dispatchers.IO) {
        try {
            delay(400)
            Result.success(clients.takeLast(10))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Load mock clients
     */
    private fun loadMockClients() {
        clients.addAll(
            listOf(
                Client("1", "Arthur Silva", "arthur@email.com", "11987654321"),
                Client("2", "André Mattos", "andre@email.com", "11912345678"),
                Client("3", "Amanda Costa", "", "11923456789"),

                Client("4", "Bruno Oliveira", "bruno@email.com", "11934567890"),
                Client("5", "Beatriz Santos", "", "11945678901"),

                Client("6", "Carlos Ferreira", "carlos@email.com", "11956443322"),
                Client("7", "Caio Mendes", "caio@email.com", "11994432211"),
                Client("8", "César Augusto", "", "11988776655"),
                Client("9", "Cristina Lima", "", "11977665544"),

                Client("10", "Diego Souza", "diego@email.com", "11966554433"),
                Client("11", "Daniel Rocha", "", "11955443322"),

                Client("12", "Eduardo Pereira", "eduardo@email.com", "11944332211"),
                Client("13", "Eliana Martins", "", "11933221100"),

                Client("14", "Fernando Costa", "fernando@email.com", "11922110099"),
                Client("15", "Fabiana Silva", "", "11911009988"),

                Client("16", "Gabriel Santos", "gabriel@email.com", "11900998877"),
                Client("17", "Giovana Alves", "", "11899887766"),

                Client("18", "Henrique Lima", "henrique@email.com", "11988776655"),
                Client("19", "Helena Dias", "", "11977665544"),

                Client("20", "Igor Mendes", "igor@email.com", "11966554433"),
                Client("21", "Isabela Rocha", "", "11955443322"),

                Client("22", "João Pedro", "joao@email.com", "11944332211"),
                Client("23", "Julia Fernandes", "", "11933221100"),

                Client("24", "Kevin Oliveira", "", "11922110099"),
                Client("25", "Karina Santos", "", "11911009988"),

                Client("26", "Lucas Costa", "lucas@email.com", "11900998877"),
                Client("27", "Larissa Souza", "", "11899887766"),
                Client("28", "Leonardo Silva", "", "11988776655"),

                Client("29", "Marcelo Pereira", "marcelo@email.com", "11977665544"),
                Client("30", "Marina Santos", "", "11966554433"),
                Client("31", "Mateus Oliveira", "", "11955443322"),

                Client("32", "Nicolas Ferreira", "nicolas@email.com", "11944332211"),
                Client("33", "Natália Costa", "", "11933221100"),

                Client("34", "Otávio Lima", "", "11922110099"),
                Client("35", "Olívia Dias", "", "11911009988"),

                Client("36", "Paulo Roberto", "paulo@email.com", "11900998877"),
                Client("37", "Patricia Alves", "", "11899887766"),
                Client("38", "Pedro Henrique", "", "11988776655"),

                Client("39", "Rafael Souza", "rafael@email.com", "11977665544"),
                Client("40", "Renata Silva", "", "11966554433"),
                Client("41", "Rodrigo Costa", "", "11955443322"),

                Client("42", "Samuel Oliveira", "samuel@email.com", "11944332211"),
                Client("43", "Sofia Santos", "", "11933221100"),
                Client("44", "Sérgio Pereira", "", "11922110099"),

                Client("45", "Thiago Ferreira", "thiago@email.com", "11911009988"),
                Client("46", "Tatiana Lima", "", "11900998877"),

                Client("47", "Vitor Hugo", "vitor@email.com", "11899887766"),
                Client("48", "Vanessa Costa", "", "11988776655"),
                Client("49", "Vinícius Souza", "", "11977665544"),

                Client("50", "Wagner Silva", "", "11966554433"),
                Client("51", "Wanda Santos", "", "11955443322")
            )
        )
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