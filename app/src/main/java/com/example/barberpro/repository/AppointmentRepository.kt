package com.example.barberpro.repository

import com.example.barberpro.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AppointmentRepository private constructor() {

    private val appointments = mutableListOf<Appointment>()
    private val clients = mutableListOf<Client>()
    private val services = mutableListOf<Service>()
    private val config = BarberConfig()

    init {
        loadMockData()
    }

    /* ===================== APPOINTMENTS ===================== */

    suspend fun getAllAppointments(): Result<List<Appointment>> =
        withContext(Dispatchers.IO) {
            delay(300)
            Result.success(appointments.toList())
        }

    suspend fun getAppointmentsByDate(date: Date): Result<List<Appointment>> =
        withContext(Dispatchers.IO) {
            delay(300)
            Result.success(
                appointments
                    .filter { isSameDay(it.startTime, date) }
                    .sortedBy { it.startTime }
            )
        }

    suspend fun createAppointment(appointment: Appointment): Result<Appointment> =
        withContext(Dispatchers.IO) {
            delay(400)

            val cal = Calendar.getInstance().apply {
                time = appointment.startTime
            }

            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val minute = cal.get(Calendar.MINUTE)

            // ⏰ horário de funcionamento
            if (hour < config.openingHour || hour >= config.closingHour) {
                return@withContext Result.failure(
                    Exception("Fora do horário de funcionamento")
                )
            }

            // ⏱ slots fixos (30 em 30)
            if (minute % config.slotDurationMinutes != 0) {
                return@withContext Result.failure(
                    Exception("Agendamentos apenas de 30 em 30 minutos")
                )
            }

            // ⛔ conflito de horário
            val conflict = appointments.any {
                it.startTime == appointment.startTime
            }

            if (conflict) {
                return@withContext Result.failure(
                    Exception("Horário já ocupado")
                )
            }

            appointments.add(appointment)
            Result.success(appointment)
        }

    suspend fun updateAppointmentStatus(
        appointmentId: String,
        status: AppointmentStatus
    ): Result<Appointment> =
        withContext(Dispatchers.IO) {
            delay(300)

            val index = appointments.indexOfFirst { it.id == appointmentId }
            if (index == -1) {
                return@withContext Result.failure(Exception("Agendamento não encontrado"))
            }

            val updated = appointments[index].copy(status = status)
            appointments[index] = updated
            Result.success(updated)
        }

    suspend fun deleteAppointment(appointmentId: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            delay(300)

            val removed = appointments.removeIf { it.id == appointmentId }
            if (!removed) {
                return@withContext Result.failure(Exception("Agendamento não encontrado"))
            }

            Result.success(true)
        }

    /* ===================== HELPERS ===================== */

    private fun isSameDay(d1: Date, d2: Date): Boolean {
        val c1 = Calendar.getInstance().apply { time = d1 }
        val c2 = Calendar.getInstance().apply { time = d2 }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }

    suspend fun getBookedSlotsForDay(date: Date): List<String> {
        val result = getAppointmentsByDate(date)
        if (result.isFailure) return emptyList()

        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        return result.getOrNull()?.map {
            formatter.format(it.startTime)
        } ?: emptyList()
    }

    /* ===================== MOCK DATA ===================== */

    private fun loadMockData() {
        clients.addAll(
            listOf(
                Client("1", "Carlos Silva", "11999999999", "carlos@email.com"),
                Client("2", "Igor Santos", "11988888888", "igor@email.com"),
                Client("3", "Diego Oliveira", "11977777777", "diego@email.com")
            )
        )

        services.addAll(
            listOf(
                Service("1", "Barba", 20.0),
                Service("2", "Corte social", 45.0),
                Service("3", "Corte + Barba", 65.0)
            )
        )

        val cal = Calendar.getInstance()

        fun add(hour: Int, minute: Int, client: Client, service: Service) {
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            cal.set(Calendar.SECOND, 0)

            val start = cal.time
            cal.add(Calendar.MINUTE, config.slotDurationMinutes)
            val end = cal.time

            appointments.add(
                Appointment(
                    id = UUID.randomUUID().toString(),
                    client = client,
                    service = service,
                    startTime = start,
                    status = AppointmentStatus.SCHEDULED
                )
            )

            cal.add(Calendar.MINUTE, -config.slotDurationMinutes)
        }

        add(9, 0, clients[0], services[0])
        add(9, 30, clients[1], services[1])
        add(10, 0, clients[2], services[2])
    }

    companion object {
        @Volatile
        private var INSTANCE: AppointmentRepository? = null

        fun getInstance(): AppointmentRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppointmentRepository().also { INSTANCE = it }
            }
    }
}