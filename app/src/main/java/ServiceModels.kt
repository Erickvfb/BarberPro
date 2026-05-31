package com.example.barberpro.model

import java.io.Serializable
import java.util.Date


/**
 * Service model
 */
data class Service(
    val id: String,
    val name: String,
    val price: Double,
    val durationMinutes: Int = 30
) : Serializable


/**
 * Appointment status enum
 */

enum class AppointmentStatus {
    SCHEDULED,
    COMPLETED,
    CANCELED,
    PENDING;
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
    val status: AppointmentStatus = AppointmentStatus.PENDING,
    val notes: String? = null,
    val barberId: String? = null,
    val products: List<Product> = emptyList()
) {

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
    val isSelected: Boolean,
    val isAvailable: Boolean
)