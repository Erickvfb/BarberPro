package com.example.barberpro.repository

import com.example.barberpro.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import com.example.barberpro.model.AppointmentReport
import com.example.barberpro.model.ClientGrowth
import com.example.barberpro.model.ClientReport
import com.example.barberpro.model.DailyRevenue
import com.example.barberpro.model.DashboardStats
import com.example.barberpro.model.PeakHour
import com.example.barberpro.model.Period
import com.example.barberpro.model.ProductReport
import com.example.barberpro.model.ProductSales
import com.example.barberpro.model.ProductStock
import com.example.barberpro.model.RevenueReport
import com.example.barberpro.model.ServiceReport
import com.example.barberpro.model.ServiceStats
import com.example.barberpro.model.StockStatus
import com.example.barberpro.model.TopClient
import com.example.barberpro.model.Transaction
import com.example.barberpro.model.TransactionType
import java.util.*
import kotlin.random.Random

/**
 * Repository for reports and statistics
 * Simulates backend with realistic data
 */
class ReportsRepository {

    /**
     * Get revenue report for a period
     */
    suspend fun getRevenueReport(period: Period): Result<RevenueReport> = withContext(Dispatchers.IO) {
        try {
            delay(800) // Simulate network delay

            val dailyBreakdown = generateDailyRevenue(period)
            val transactions = generateTransactions(period)

            val totalRevenue = dailyBreakdown.sumOf { it.revenue }
            val servicesRevenue = transactions.filter { it.type == TransactionType.SERVICE }.sumOf { it.amount }
            val productsRevenue = transactions.filter { it.type == TransactionType.PRODUCT }.sumOf { it.amount }
            val servicesCount = transactions.count { it.type == TransactionType.SERVICE }
            val productsCount = transactions.count { it.type == TransactionType.PRODUCT }

            val report = RevenueReport(
                period = period,
                totalRevenue = totalRevenue,
                totalServices = servicesRevenue,
                totalProducts = productsRevenue,
                servicesCount = servicesCount,
                productsCount = productsCount,
                transactions = transactions,
                dailyBreakdown = dailyBreakdown
            )

            Result.success(report)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get client statistics report
     */
    suspend fun getClientReport(period: Period): Result<ClientReport> = withContext(Dispatchers.IO) {
        try {
            delay(700)

            val topClients = generateTopClients()
            val clientGrowth = generateClientGrowth()

            val report = ClientReport(
                totalClients = 156,
                newClientsThisMonth = 23,
                topClients = topClients,
                clientsByMonth = clientGrowth,
                averageTicket = 87.50,
                retentionRate = 0.78
            )

            Result.success(report)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get product statistics report
     */
    suspend fun getProductReport(): Result<ProductReport> = withContext(Dispatchers.IO) {
        try {
            delay(600)

            val lowStockProducts = generateLowStockProducts()
            val topSellingProducts = generateTopSellingProducts()

            val report = ProductReport(
                totalProducts = 45,
                lowStockProducts = lowStockProducts,
                topSellingProducts = topSellingProducts,
                totalInventoryValue = 12450.00,
                productsByCategory = mapOf(
                    "Pomadas" to 15,
                    "Ceras" to 10,
                    "Shampoos" to 8,
                    "Condicionadores" to 7,
                    "Outros" to 5
                )
            )

            Result.success(report)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get service statistics report
     */
    suspend fun getServiceReport(period: Period): Result<ServiceReport> = withContext(Dispatchers.IO) {
        try {
            delay(600)

            val topServices = generateTopServices()

            val report = ServiceReport(
                totalServices = 342,
                topServices = topServices,
                averageServiceDuration = 35,
                servicesByCategory = mapOf(
                    "Cortes" to 180,
                    "Barba" to 95,
                    "Química" to 42,
                    "Tratamentos" to 25
                ),
                revenue = 15670.00
            )

            Result.success(report)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get appointment statistics report
     */
    suspend fun getAppointmentReport(period: Period): Result<AppointmentReport> = withContext(Dispatchers.IO) {
        try {
            delay(700)

            val total = 342
            val completed = 298
            val cancelled = 32
            val noShow = 12

            val report = AppointmentReport(
                totalAppointments = total,
                completedAppointments = completed,
                cancelledAppointments = cancelled,
                noShowAppointments = noShow,
                completionRate = completed.toDouble() / total,
                cancellationRate = cancelled.toDouble() / total,
                appointmentsByStatus = mapOf(
                    AppointmentStatus.COMPLETED to completed,
                    AppointmentStatus.CANCELED to cancelled,
                    AppointmentStatus.PENDING to 0
                ),
                appointmentsByDayOfWeek = mapOf(
                    "Segunda" to 58,
                    "Terça" to 62,
                    "Quarta" to 51,
                    "Quinta" to 55,
                    "Sexta" to 68,
                    "Sábado" to 48
                ),
                peakHours = listOf(
                    PeakHour(9, 32),
                    PeakHour(10, 45),
                    PeakHour(14, 38),
                    PeakHour(15, 42),
                    PeakHour(16, 35)
                )
            )

            Result.success(report)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get complete dashboard statistics
     */
    suspend fun getDashboardStats(period: Period): Result<DashboardStats> = withContext(Dispatchers.IO) {
        try {
            delay(1000)

            val revenueReport = getRevenueReport(period).getOrThrow()
            val clientReport = getClientReport(period).getOrThrow()
            val productReport = getProductReport().getOrThrow()
            val serviceReport = getServiceReport(period).getOrThrow()
            val appointmentReport = getAppointmentReport(period).getOrThrow()

            val stats = DashboardStats(
                revenueReport = revenueReport,
                clientReport = clientReport,
                productReport = productReport,
                serviceReport = serviceReport,
                appointmentReport = appointmentReport
            )

            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Helper functions to generate mock data

    private fun generateDailyRevenue(period: Period): List<DailyRevenue> {
        val days = mutableListOf<DailyRevenue>()
        val cal = Calendar.getInstance()
        cal.time = period.startDate

        while (cal.time <= period.endDate) {
            days.add(
                DailyRevenue(
                    date = cal.time,
                    revenue = Random.nextDouble(200.0, 1200.0),
                    servicesCount = Random.nextInt(5, 20),
                    productsCount = Random.nextInt(2, 10)
                )
            )
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }

        return days
    }

    private fun generateTransactions(period: Period): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val serviceNames = listOf("Corte Social", "Barba", "Corte + Barba", "Platinado", "Química")
        val productNames = listOf("Pomada", "Cera", "Shampoo", "Condicionador")
        val clientNames = listOf("Carlos Silva", "João Santos", "Pedro Oliveira", "Lucas Costa", "Rafael Souza")

        repeat(50) {
            val isService = Random.nextBoolean()
            transactions.add(
                Transaction(
                    id = UUID.randomUUID().toString(),
                    date = randomDate(period.startDate, period.endDate),
                    type = if (isService) TransactionType.SERVICE else TransactionType.PRODUCT,
                    description = if (isService) serviceNames.random() else productNames.random(),
                    amount = if (isService) Random.nextDouble(30.0, 150.0) else Random.nextDouble(15.0, 80.0),
                    clientName = clientNames.random()
                )
            )
        }

        return transactions.sortedByDescending { it.date }
    }

    private fun generateTopClients(): List<TopClient> {
        val clients = listOf(
            Client("1", "Carlos Silva", "abc@dsfsdf.com","11999999999"),
            Client("2", "João Santos", "abc@dsfsdf.com","11988888888"),
            Client("3", "Pedro Oliveira", "abc@dsfsdf.com","11977777777"),
            Client("4", "Lucas Costa", "abc@dsfsdf.com","11966666666"),
            Client("5", "Rafael Souza", "abc@dsfsdf.com","11955555555")
        )

        return clients.mapIndexed { index, client ->
            TopClient(
                client = client,
                totalSpent = 1500.0 - (index * 200.0),
                visitsCount = 15 - (index * 2),
                lastVisit = Date()
            )
        }
    }

    private fun generateClientGrowth(): List<ClientGrowth> {
        val months = listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun")
        return months.mapIndexed { index, month ->
            ClientGrowth(
                month = month,
                newClients = Random.nextInt(15, 30),
                totalClients = 100 + (index * 10) + Random.nextInt(5, 15)
            )
        }
    }

    private fun generateLowStockProducts(): List<ProductStock> {
        val products = listOf(
            Product("1", "Pomada Modeladora", 35.0),
            Product("2", "Shampoo Anti-Resíduos", 42.0),
            Product("3", "Cera Fixação Forte", 38.0)
        )

        return products.map { product ->
            ProductStock(
                product = product,
                currentStock = Random.nextInt(1, 5),
                minStock = 10,
                status = StockStatus.LOW
            )
        }
    }

    private fun generateTopSellingProducts(): List<ProductSales> {
        val products = listOf(
            Product("4", "Pomada Premium", 45.0),
            Product("5", "Shampoo Professional", 38.0),
            Product("6", "Cera Efeito Mate", 40.0)
        )

        return products.map { product ->
            ProductSales(
                product = product,
                quantitySold = Random.nextInt(20, 50),
                revenue = product.price * Random.nextInt(20, 50),
                profitMargin = 0.35
            )
        }
    }

    private fun generateTopServices(): List<ServiceStats> {
        val services = listOf(
            Service("1", "Corte Social", 45.0),
            Service("2", "Barba Completa", 25.0),
            Service("3", "Corte + Barba", 65.0),
            Service("4", "Platinado", 120.0),
            Service("5", "Química", 80.0)
        )

        return services.map { service ->
            ServiceStats(
                service = service,
                timesPerformed = Random.nextInt(30, 100),
                revenue = service.price * Random.nextInt(30, 100),
                customerSatisfaction = Random.nextDouble(4.0, 5.0)
            )
        }
    }

    private fun randomDate(start: Date, end: Date): Date {
        val diff = end.time - start.time
        return Date(start.time + (Math.random() * diff).toLong())
    }

    companion object {
        @Volatile
        private var INSTANCE: ReportsRepository? = null

        fun getInstance(): ReportsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ReportsRepository().also { INSTANCE = it }
            }
        }
    }
}