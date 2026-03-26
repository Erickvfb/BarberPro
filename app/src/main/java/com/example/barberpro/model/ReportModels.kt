package com.example.barberpro.model

import java.util.*

/**
 * Revenue Report Model
 */
data class RevenueReport(
    val period: Period,
    val totalRevenue: Double,
    val totalServices: Double,
    val totalProducts: Double,
    val servicesCount: Int,
    val productsCount: Int,
    val transactions: List<Transaction>,
    val dailyBreakdown: List<DailyRevenue>
)

/**
 * Daily revenue breakdown
 */
data class DailyRevenue(
    val date: Date,
    val revenue: Double,
    val servicesCount: Int,
    val productsCount: Int
)

/**
 * Transaction model
 */
data class Transaction(
    val id: String,
    val date: Date,
    val type: TransactionType,
    val description: String,
    val amount: Double,
    val clientName: String? = null
)

enum class TransactionType {
    SERVICE, PRODUCT, PACKAGE
}

/**
 * Client Report Model
 */
data class ClientReport(
    val totalClients: Int,
    val newClientsThisMonth: Int,
    val topClients: List<TopClient>,
    val clientsByMonth: List<ClientGrowth>,
    val averageTicket: Double,
    val retentionRate: Double
)

/**
 * Top client
 */
data class TopClient(
    val client: Client,
    val totalSpent: Double,
    val visitsCount: Int,
    val lastVisit: Date
)

/**
 * Client growth data
 */
data class ClientGrowth(
    val month: String,
    val newClients: Int,
    val totalClients: Int
)

/**
 * Product Report Model
 */
data class ProductReport(
    val totalProducts: Int,
    val lowStockProducts: List<ProductStock>,
    val topSellingProducts: List<ProductSales>,
    val totalInventoryValue: Double,
    val productsByCategory: Map<String, Int>
)

/**
 * Product stock info
 */
data class ProductStock(
    val product: Product,
    val currentStock: Int,
    val minStock: Int,
    val status: StockStatus
)

enum class StockStatus {
    OK, LOW, OUT_OF_STOCK
}

/**
 * Product sales data
 */
data class ProductSales(
    val product: Product,
    val quantitySold: Int,
    val revenue: Double,
    val profitMargin: Double
)

/**
 * Service Report Model
 */
data class ServiceReport(
    val totalServices: Int,
    val topServices: List<ServiceStats>,
    val averageServiceDuration: Int,
    val servicesByCategory: Map<String, Int>,
    val revenue: Double
)

/**
 * Service statistics
 */
data class ServiceStats(
    val service: Service,
    val timesPerformed: Int,
    val revenue: Double,
    val customerSatisfaction: Double? = null
)

/**
 * Appointment Report Model
 */
data class AppointmentReport(
    val totalAppointments: Int,
    val completedAppointments: Int,
    val cancelledAppointments: Int,
    val noShowAppointments: Int,
    val completionRate: Double,
    val cancellationRate: Double,
    val appointmentsByStatus: Map<AppointmentStatus, Int>,
    val appointmentsByDayOfWeek: Map<String, Int>,
    val peakHours: List<PeakHour>
)

/**
 * Peak hour data
 */
data class PeakHour(
    val hour: Int,
    val appointmentCount: Int
)

/**
 * Time period for reports
 */
data class Period(
    val startDate: Date,
    val endDate: Date,
    val type: PeriodType
)

enum class PeriodType {
    TODAY, THIS_WEEK, THIS_MONTH, LAST_MONTH, CUSTOM
}

/**
 * Complete dashboard statistics
 */
data class DashboardStats(
    val revenueReport: RevenueReport,
    val clientReport: ClientReport,
    val productReport: ProductReport,
    val serviceReport: ServiceReport,
    val appointmentReport: AppointmentReport
)