package com.example.barberpro.repository

import android.util.Log
import com.example.barberpro.data.api.ApiService
import com.example.barberpro.data.api.RetrofitClient
import com.example.barberpro.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class ReportsRepository private constructor(
    private val apiService: ApiService = RetrofitClient.apiService
) {

    suspend fun getRevenueReport(
        startDate: Date,
        endDate: Date
    ): Result<RevenueReport> = withContext(Dispatchers.IO) {
        return@withContext try {
            val startDateStr = formatDateForAPI(startDate)
            val endDateStr = formatDateForAPI(endDate)

            Log.d("REPORTS", "Buscando revenue: $startDateStr até $endDateStr")

            val response = apiService.getRevenue(startDateStr, endDateStr)

            if (response.isSuccessful && response.body()?.success == true) {
                val apiData = response.body()!!.data

                val report = RevenueReport(
                    period = Period(startDate, endDate, PeriodType.CUSTOM),
                    totalRevenue = apiData.total_revenue,
                    totalServices = apiData.total_services,
                    totalProducts = apiData.total_products,
                    servicesCount = apiData.servicesCount ?: 0,
                    productsCount = apiData.productsCount ?: 0,
                    transactions = emptyList(),
                    dailyBreakdown = emptyList()
                )

                Log.d("REPORTS", " Receita total: R$ ${report.totalRevenue}")
                Result.success(report)
            } else {
                Result.failure(Exception(response.body()?.message))
            }
        } catch (e: Exception) {
            Log.e("REPORTS_ERROR", "Erro ao buscar revenue: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getTopServices(
        startDate: Date,
        endDate: Date
    ): Result<List<ServiceSalesData>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val startDateStr = formatDateForAPI(startDate)
            val endDateStr = formatDateForAPI(endDate)

            Log.d("REPORTS", "Buscando top services")

            val response = apiService.getTopServices(startDateStr, endDateStr)

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data

                // Mapear para ServiceSalesData
                val services = data.map { service ->
                    ServiceSalesData(
                        id = service.id ?: "",
                        name = service.name ?: "Serviço",
                        quantity = service.quantity ?: 0,
                        totalRevenue = service.totalRevenue ?: 0.0
                    )
                }

                Log.d("REPORTS", " ${services.size} serviços carregados")
                services.forEach {
                    Log.d("REPORTS", "- ${it.name}: ${it.quantity}x = R$ ${it.totalRevenue}")
                }

                Result.success(services)
            } else {
                Log.e("REPORTS_ERROR", "Erro ao buscar services")
                Result.failure(Exception("Erro ao buscar serviços"))
            }
        } catch (e: Exception) {
            Log.e("REPORTS_ERROR", "Exception ao buscar services: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getTopProducts(
        startDate: Date,
        endDate: Date
    ): Result<List<ProductSalesData>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val startDateStr = formatDateForAPI(startDate)
            val endDateStr = formatDateForAPI(endDate)

            Log.d("REPORTS", "Buscando top products")

            val response = apiService.getTopProducts(startDateStr, endDateStr)

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data

                // Mapear para ProductSalesData
                val products = data.map { product ->
                    ProductSalesData(
                        id = product.id ?: "",
                        name = product.name ?: "Produto",
                        quantity = product.quantity ?: 0,
                        totalRevenue = product.totalRevenue ?: 0.0
                    )
                }

                Log.d("REPORTS", " ${products.size} produtos carregados")
                products.forEach {
                    Log.d("REPORTS", "- ${it.name}: ${it.quantity}x = R$ ${it.totalRevenue}")
                }

                Result.success(products)
            } else {
                Log.e("REPORTS_ERROR", "Erro ao buscar products")
                Result.failure(Exception("Erro ao buscar produtos"))
            }
        } catch (e: Exception) {
            Log.e("REPORTS_ERROR", "Exception ao buscar products: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getDashboardStats(): Result<DashboardData> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d("REPORTS", "Buscando dashboard")

            val response = apiService.getDashboard()

            if (response.isSuccessful && response.body()?.success == true) {
                val apiData = response.body()!!.data

                val dashboardData = DashboardData(
                    totalRevenue = apiData.revenue.total_revenue,
                    totalServices = apiData.revenue.total_services,
                    totalProducts = apiData.revenue.total_products,
                    totalClients = apiData.total_clients,
                    todayAppointments = apiData.today_appointments,
                    lowStockProducts = apiData.low_stock_products,
                    attendanceCount = apiData.revenue.attendance_count
                )

                Log.d("REPORTS", " Dashboard carregado")
                Result.success(dashboardData)
            } else {
                Result.failure(Exception("Erro ao buscar dashboard"))
            }
        } catch (e: Exception) {
            Log.e("REPORTS_ERROR", "Erro ao buscar dashboard: ${e.message}")
            Result.failure(e)
        }
    }

    private fun formatDateForAPI(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val year = calendar.get(Calendar.YEAR)
        val month = String.format("%02d", calendar.get(Calendar.MONTH) + 1)
        val day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))
        return "$year-$month-$day"
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

data class ServiceSalesData(
    val id: String,
    val name: String,
    val quantity: Int,
    val totalRevenue: Double
)

data class ProductSalesData(
    val id: String,
    val name: String,
    val quantity: Int,
    val totalRevenue: Double
)


data class TopServiceResponse(
    val id: String?,
    val name: String?,
    val quantity: Int?,
    val totalRevenue: Double?,
    val averagePrice: Double? = null
)

data class TopProductResponse(
    val id: String?,
    val name: String?,
    val quantity: Int?,
    val totalRevenue: Double?
)

data class DashboardData(
    val totalRevenue: Double,
    val totalServices: Double,
    val totalProducts: Double,
    val totalClients: Int,
    val todayAppointments: Int,
    val lowStockProducts: Int,
    val attendanceCount: Int = 0
)