package com.example.barberpro.repository

import com.example.barberpro.model.Service
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class ServicesRepository {

    private val services = mutableListOf<Service>()

    init {
        loadMockServices()
    }

    suspend fun getAllServices(): Result<List<Service>> = withContext(Dispatchers.IO) {
        try {
            delay(300)
            Result.success(services.sortedBy { it.name })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getServiceById(id: String): Result<Service> = withContext(Dispatchers.IO) {
        val service = services.find { it.id == id }
        if (service != null) Result.success(service)
        else Result.failure(Exception("Serviço não encontrado"))
    }

    suspend fun addService(service: Service): Result<Service> = withContext(Dispatchers.IO) {
        services.add(service)
        Result.success(service)
    }

    suspend fun updateService(service: Service): Result<Service> = withContext(Dispatchers.IO) {
        val index = services.indexOfFirst { it.id == service.id }
        if (index != -1) {
            services[index] = service
            Result.success(service)
        } else {
            Result.failure(Exception("Serviço não encontrado"))
        }
    }

    suspend fun deleteService(serviceId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val removed = services.removeIf { it.id == serviceId }
        if (removed) Result.success(true)
        else Result.failure(Exception("Serviço não encontrado"))
    }

    // Verifica se o serviço está associado a vendas
    suspend fun hasServiceInSales(serviceId: String): Boolean = withContext(Dispatchers.IO) {
        // TODO: Implementar lógica real de vendas
        false
    }

    private fun loadMockServices() {
        services.addAll(
            listOf(
                Service("1", "Corte Social", 45.0),
                Service("2", "Barba", 30.0),
                Service("3", "Combo: Corte + Barba", 70.0)
            )
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: ServicesRepository? = null

        fun getInstance(): ServicesRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: ServicesRepository().also { INSTANCE = it }
            }
    }

    suspend fun searchServices(query: String): Result<List<Service>> = withContext(Dispatchers.IO) {
        try {
            delay(200) // simula algum processamento
            val filtered = services.filter { it.name.contains(query, ignoreCase = true) }
            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}