package com.example.barberpro.model

import java.util.Date

/**
 * Client model
 */
data class Client(
    val id: String,
    val name: String,
    val phone: String,
    val email: String? = null,
    val avatarUrl: String? = null
) {
    fun getInitial(): String = name.firstOrNull()?.uppercase() ?: "?"
}

/**
 * Service model
 */
data class Service(
    val id: String,
    val name: String,
    val durationMinutes: Int,
    val price: Double,
    val description: String? = null
)

/**
 * Appointment status enum
 */
enum class AppointmentStatus(val displayName: String, val color: String) {
    PENDING("pendente", "#FFA726"),
    CONFIRMED("confirmado", "#66BB6A"),
    IN_PROGRESS("em andamento", "#42A5F5"),
    COMPLETED("concluído", "#4CAF50"),
    CANCELLED("cancelado", "#EF5350"),
    NO_SHOW("não compareceu", "#BDBDBD");

    companion object {
        fun fromString(status: String): AppointmentStatus {
            return values().find { it.name.equals(status, ignoreCase = true) } ?: PENDING
        }
    }
}

/**
 * Appointment model
 */
data class Appointment(
    val id: String,
    val client: Client,
    val service: Service,
    val startTime: Date,
    val endTime: Date,
    val status: AppointmentStatus = AppointmentStatus.PENDING,
    val notes: String? = null,
    val barberId: String? = null,
    val products: List<Product> = emptyList()
) {
    fun getTimeRange(): String {
        val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return "${formatter.format(startTime)} - ${formatter.format(endTime)}"
    }

    fun getTotalRevenue(): Double {
        val productTotal = products.sumOf { it.price * it.quantity }
        return service.price + productTotal
    }
}

/**
 * Product model
 */
data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val quantity: Int = 1
)

/**
 * Day summary for agenda
 */
data class DaySummary(
    val date: Date,
    val servicesCount: Int,
    val productsCount: Int,
    val totalRevenue: Double,
    val appointments: List<Appointment>
)

/**
 * Week calendar day
 */
data class CalendarDay(
    val date: Date,
    val dayOfWeek: String,
    val dayOfMonth: Int,
    val isSelected: Boolean = false,
    val isToday: Boolean = false
)