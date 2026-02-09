package com.example.barberpro.repository

import com.example.barberpro.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Repository for managing appointments with backend simulation
 * In a real app, this would make API calls to your backend server
 */
class AppointmentRepository {

    // Simulated in-memory database
    private val appointments = mutableListOf<Appointment>()
    private val clients = mutableListOf<Client>()
    private val services = mutableListOf<Service>()

    init {
        loadMockData()
    }

    /**
     * Get all appointments
     */
    suspend fun getAllAppointments(): Result<List<Appointment>> = withContext(Dispatchers.IO) {
        try {
            delay(500) // Simulate network delay
            Result.success(appointments.toList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get appointments for a specific date
     */
    suspend fun getAppointmentsByDate(date: Date): Result<List<Appointment>> = withContext(Dispatchers.IO) {
        try {
            delay(300) // Simulate network delay
            val filtered = appointments.filter { appointment ->
                isSameDay(appointment.startTime, date)
            }.sortedBy { it.startTime }
            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get appointments for a date range
     */
    suspend fun getAppointmentsByDateRange(startDate: Date, endDate: Date): Result<List<Appointment>> =
        withContext(Dispatchers.IO) {
            try {
                delay(400) // Simulate network delay
                val filtered = appointments.filter { appointment ->
                    appointment.startTime >= startDate && appointment.startTime <= endDate
                }.sortedBy { it.startTime }
                Result.success(filtered)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Create a new appointment
     */
    suspend fun createAppointment(appointment: Appointment): Result<Appointment> = withContext(Dispatchers.IO) {
        try {
            delay(600) // Simulate network delay

            // Check for conflicts
            val hasConflict = appointments.any { existing ->
                existing.id != appointment.id &&
                        isSameDay(existing.startTime, appointment.startTime) &&
                        (appointment.startTime in existing.startTime..existing.endTime ||
                                appointment.endTime in existing.startTime..existing.endTime)
            }

            if (hasConflict) {
                return@withContext Result.failure(Exception("Conflito de horário"))
            }

            appointments.add(appointment)
            Result.success(appointment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update an appointment
     */
    suspend fun updateAppointment(appointment: Appointment): Result<Appointment> = withContext(Dispatchers.IO) {
        try {
            delay(600) // Simulate network delay

            val index = appointments.indexOfFirst { it.id == appointment.id }
            if (index == -1) {
                return@withContext Result.failure(Exception("Agendamento não encontrado"))
            }

            appointments[index] = appointment
            Result.success(appointment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete an appointment
     */
    suspend fun deleteAppointment(appointmentId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            delay(400) // Simulate network delay

            val removed = appointments.removeIf { it.id == appointmentId }
            if (!removed) {
                return@withContext Result.failure(Exception("Agendamento não encontrado"))
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update appointment status
     */
    suspend fun updateAppointmentStatus(appointmentId: String, status: AppointmentStatus): Result<Appointment> =
        withContext(Dispatchers.IO) {
            try {
                delay(400) // Simulate network delay

                val index = appointments.indexOfFirst { it.id == appointmentId }
                if (index == -1) {
                    return@withContext Result.failure(Exception("Agendamento não encontrado"))
                }

                val updated = appointments[index].copy(status = status)
                appointments[index] = updated
                Result.success(updated)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Get day summary with statistics
     */
    suspend fun getDaySummary(date: Date): Result<DaySummary> = withContext(Dispatchers.IO) {
        try {
            delay(300) // Simulate network delay

            val dayAppointments = appointments.filter { isSameDay(it.startTime, date) }
            val servicesCount = dayAppointments.size
            val productsCount = dayAppointments.sumOf { it.products.size }
            val totalRevenue = dayAppointments.sumOf { it.getTotalRevenue() }

            val summary = DaySummary(
                date = date,
                servicesCount = servicesCount,
                productsCount = productsCount,
                totalRevenue = totalRevenue,
                appointments = dayAppointments
            )

            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all clients
     */
    suspend fun getAllClients(): Result<List<Client>> = withContext(Dispatchers.IO) {
        try {
            delay(300) // Simulate network delay
            Result.success(clients.toList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all services
     */
    suspend fun getAllServices(): Result<List<Service>> = withContext(Dispatchers.IO) {
        try {
            delay(300) // Simulate network delay
            Result.success(services.toList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Search clients by name
     */
    suspend fun searchClients(query: String): Result<List<Client>> = withContext(Dispatchers.IO) {
        try {
            delay(200) // Simulate network delay
            val filtered = clients.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.phone.contains(query)
            }
            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Helper functions
    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Load mock data for testing
     */
    private fun loadMockData() {
        // Mock clients
        clients.addAll(listOf(
            Client("1", "Carlos Silva", "11999999999", "carlos@email.com"),
            Client("2", "Igor Santos", "11988888888", "igor@email.com"),
            Client("3", "Diego Oliveira", "11977777777", "diego@email.com"),
            Client("4", "Rafael Costa", "11966666666", "rafael@email.com"),
            Client("5", "Lucas Pereira", "11955555555", "lucas@email.com")
        ))

        // Mock services
        services.addAll(listOf(
            Service("1", "Barba", 20, 20.0, "Apara e modelagem de barba"),
            Service("2", "Corte social", 20, 45.80, "Corte masculino tradicional"),
            Service("3", "Corte social e outros", 40, 60.0, "Corte + acabamento"),
            Service("4", "Platinado", 60, 120.0, "Descoloração completa"),
            Service("5", "Química", 45, 80.0, "Alisamento ou relaxamento")
        ))

        // Create sample appointments for today and next few days
        val cal = Calendar.getInstance()

        // Today
        createSampleAppointment(cal, 9, 0, clients[0], services[0])
        createSampleAppointment(cal, 9, 30, clients[1], services[1])
        createSampleAppointment(cal, 10, 30, clients[2], services[2])

        // Tomorrow
        cal.add(Calendar.DAY_OF_MONTH, 1)
        createSampleAppointment(cal, 10, 0, clients[3], services[1])
        createSampleAppointment(cal, 11, 0, clients[4], services[3])

        // Day after tomorrow
        cal.add(Calendar.DAY_OF_MONTH, 1)
        createSampleAppointment(cal, 14, 0, clients[0], services[4])
    }

    private fun createSampleAppointment(cal: Calendar, hour: Int, minute: Int, client: Client, service: Service) {
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)

        val start = cal.time
        cal.add(Calendar.MINUTE, service.durationMinutes)
        val end = cal.time

        val statuses = AppointmentStatus.values()
        val status = statuses.random()

        appointments.add(
            Appointment(
                id = UUID.randomUUID().toString(),
                client = client,
                service = service,
                startTime = start,
                endTime = end,
                status = status
            )
        )

        // Reset calendar
        cal.add(Calendar.MINUTE, -service.durationMinutes)
    }

    companion object {
        @Volatile
        private var INSTANCE: AppointmentRepository? = null

        fun getInstance(): AppointmentRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppointmentRepository().also { INSTANCE = it }
            }
        }
    }
}