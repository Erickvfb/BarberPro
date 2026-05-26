package com.example.barberpro.repository

import com.example.barberpro.data.api.*
import com.example.barberpro.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AppointmentRepository private constructor() {

    private val apiService = RetrofitClient.apiService

    private val apiDateFormat = SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        Locale.US
    ).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private fun mapAppointment(api: AppointmentFull): Appointment {

        val parsedDate = try {

            apiDateFormat.parse(api.start_time)

        } catch (e: Exception) {

            e.printStackTrace()
            null
        }

        println("API DATE: ${api.start_time}")
        println("PARSED DATE: $parsedDate")

        return Appointment(
            id = api.id,

            client = Client(
                id = api.clients.id,
                name = api.clients.name,
                email = "",
                phone = api.clients.phone
            ),

            service = Service(
                id = api.services.id,
                name = api.services.name,
                price = api.services.price
            ),

            startTime = parsedDate ?: Date(),

            status = AppointmentStatus.fromString(api.status),

            notes = api.notes
        )
    }

    suspend fun getAllAppointments(): Result<List<Appointment>> =
        withContext(Dispatchers.IO) {

            try {

                val response = apiService.getAppointments()

                if (response.isSuccessful) {

                    val appointments =
                        response.body()?.data?.map {
                            mapAppointment(it)
                        } ?: emptyList()

                    Result.success(appointments)

                } else {

                    Result.failure(
                        Exception(response.message())
                    )
                }

            } catch (e: Exception) {

                Result.failure(e)
            }
        }

    suspend fun getAppointmentsByDate(
        date: Date
    ): Result<List<Appointment>> =
        withContext(Dispatchers.IO) {

            try {

                val formattedDate = SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                ).format(date)

                val response =
                    apiService.getAppointments(formattedDate)

                if (response.isSuccessful) {

                    val appointments =
                        response.body()?.data?.map {
                            mapAppointment(it)
                        } ?: emptyList()

                    Result.success(appointments)

                } else {

                    Result.failure(
                        Exception(response.message())
                    )
                }

            } catch (e: Exception) {

                Result.failure(e)
            }
        }

    suspend fun getAppointmentById(
        appointmentId: String
    ): Result<Appointment> =
        withContext(Dispatchers.IO) {

            try {

                val response =
                    apiService.getAppointmentById(appointmentId)

                if (response.isSuccessful) {

                    val appointment =
                        response.body()?.data

                    if (appointment != null) {

                        Result.success(
                            mapAppointment(appointment)
                        )

                    } else {

                        Result.failure(
                            Exception("Agendamento não encontrado")
                        )
                    }

                } else {

                    Result.failure(
                        Exception(response.message())
                    )
                }

            } catch (e: Exception) {

                Result.failure(e)
            }
        }

    suspend fun completeAppointment(
        appointmentId: String,
        status: String,
        consumptions: List<ConsumptionItem>
    ): Result<AttendanceRecord> =
        withContext(Dispatchers.IO) {

            try {

                val request =
                    CompleteAppointmentRequest(
                        status = status,
                        consumption_items =
                        consumptions.map {

                            ConsumptionItemRequest(
                                product_id = it.productId,
                                quantity = it.quantity,
                                unit_price = it.unitPrice
                            )
                        }
                    )

                val response =
                    apiService.completeAppointment(
                        id = appointmentId,
                        data = request
                    )

                if (response.isSuccessful) {

                    val attendance =
                        response.body()?.data

                    if (attendance != null) {

                        Result.success(attendance)

                    } else {

                        Result.failure(
                            Exception("Resposta vazia da API")
                        )
                    }

                } else {

                    Result.failure(
                        Exception(
                            response.errorBody()?.string()
                                ?: response.message()
                        )
                    )
                }

            } catch (e: Exception) {

                Result.failure(e)
            }
        }

    suspend fun createAppointment(
        appointment: Appointment
    ): Result<Appointment> =
        withContext(Dispatchers.IO) {

            try {

                val dateFormat = SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ssXXX",
                    Locale.US
                ).apply {
                    timeZone = TimeZone.getTimeZone("America/Sao_Paulo")
                }

                val request =
                    AppointmentRequest(
                        client_id = appointment.client.id,
                        service_id = appointment.service.id,
                        start_time = dateFormat.format(
                            appointment.startTime
                        ),
                        notes = appointment.notes
                    )

                val response =
                    apiService.createAppointment(request)

                if (response.isSuccessful) {

                    val createdAppointment =
                        response.body()?.data

                    if (createdAppointment != null) {

                        Result.success(
                            mapAppointment(createdAppointment)
                        )

                    } else {

                        Result.failure(
                            Exception("Resposta vazia da API")
                        )
                    }

                } else {

                    Result.failure(
                        Exception(response.message())
                    )
                }

            } catch (e: Exception) {

                Result.failure(e)
            }
        }

    suspend fun getBookedSlotsForDay(
        date: Date
    ): List<String> =
        withContext(Dispatchers.IO) {

            try {

                val result =
                    getAppointmentsByDate(date)

                result.getOrNull()
                    ?.map { appointment ->

                        SimpleDateFormat(
                            "HH:mm",
                            Locale.getDefault()
                        ).format(
                            appointment.startTime
                        )

                    } ?: emptyList()

            } catch (e: Exception) {

                emptyList()
            }
        }
    suspend fun deleteAppointment(
        appointmentId: String
    ): Result<Boolean> =
        withContext(Dispatchers.IO) {

            try {

                val response =
                    apiService.deleteAppointment(appointmentId)

                if (response.isSuccessful) {

                    Result.success(true)

                } else {

                    Result.failure(
                        Exception(response.message())
                    )
                }

            } catch (e: Exception) {

                Result.failure(e)
            }
        }

    companion object {

        @Volatile
        private var INSTANCE: AppointmentRepository? = null

        fun getInstance(): AppointmentRepository {

            return INSTANCE ?: synchronized(this) {

                INSTANCE ?: AppointmentRepository()
                    .also { INSTANCE = it }
            }
        }
    }
}