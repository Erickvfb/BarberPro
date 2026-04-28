package com.example.barberpro.data.api

import com.example.barberpro.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // AUTH
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<MessageResponse>

    @GET("auth/me")
    suspend fun getProfile(): Response<ApiResponse<User>>

    // CLIENTS
    @GET("clients")
    suspend fun getClients(
        @Query("search") search: String? = null
    ): Response<ApiResponse<List<Client>>>

    @GET("clients/{id}")
    suspend fun getClient(@Path("id") id: String): Response<ApiResponse<Client>>

    @POST("clients")
    suspend fun createClient(@Body client: ClientRequest): Response<ApiResponse<Client>>

    @PUT("clients/{id}")
    suspend fun updateClient(
        @Path("id") id: String,
        @Body client: ClientRequest
    ): Response<ApiResponse<Client>>

    @DELETE("clients/{id}")
    suspend fun deleteClient(@Path("id") id: String): Response<MessageResponse>

    @GET("clients/{id}/history")
    suspend fun getClientHistory(@Path("id") id: String): Response<ApiResponse<List<AttendanceRecord>>>


    // SERVICES
    @GET("services")
    suspend fun getServices(): Response<ApiResponse<List<Service>>>

    @POST("services")
    suspend fun createService(@Body service: ServiceRequest): Response<ApiResponse<Service>>

    @PUT("services/{id}")
    suspend fun updateService(
        @Path("id") id: String,
        @Body service: ServiceRequest
    ): Response<ApiResponse<Service>>

    @DELETE("services/{id}")
    suspend fun deleteService(@Path("id") id: String): Response<MessageResponse>


    // APPOINTMENTS
    @GET("appointments")
    suspend fun getAppointments(
        @Query("date") date: String? = null
    ): Response<ApiResponse<List<AppointmentFull>>>

    @POST("appointments")
    suspend fun createAppointment(
        @Body appointment: AppointmentRequest
    ): Response<ApiResponse<AppointmentFull>>

    @PUT("appointments/{id}")
    suspend fun updateAppointment(
        @Path("id") id: String,
        @Body appointment: AppointmentUpdateRequest
    ): Response<ApiResponse<AppointmentFull>>

    @DELETE("appointments/{id}")
    suspend fun deleteAppointment(@Path("id") id: String): Response<MessageResponse>

    @POST("appointments/{id}/complete")
    suspend fun completeAppointment(
        @Path("id") id: String,
        @Body data: CompleteAppointmentRequest
    ): Response<ApiResponse<AttendanceRecord>>


    // PRODUCTS
    @GET("products")
    suspend fun getProducts(
        @Query("type") type: String? = null,
        @Query("low_stock") lowStock: Boolean? = null
    ): Response<ApiResponse<List<StockProducts>>>

    @POST("products")
    suspend fun createProduct(@Body product: ProductRequest): Response<ApiResponse<StockProducts>>

    @PUT("products/{id}")
    suspend fun updateProduct(
        @Path("id") id: String,
        @Body product: ProductRequest
    ): Response<ApiResponse<StockProducts>>

    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: String): Response<MessageResponse>

    // CONFIG
    @GET("config")
    suspend fun getConfig(): Response<ApiResponse<BarberConfigAPI>>

    @PUT("config")
    suspend fun updateConfig(@Body config: BarberConfigAPI): Response<ApiResponse<BarberConfigAPI>>


    // SUBSCRIPTION
    @GET("subscription")
    suspend fun getSubscription(): Response<ApiResponse<SubscriptionAPI>>

    @POST("subscription/upgrade")
    suspend fun upgradeSubscription(
        @Body request: UpgradeRequest
    ): Response<ApiResponse<SubscriptionAPI>>

    @POST("subscription/cancel")
    suspend fun cancelSubscription(): Response<ApiResponse<SubscriptionAPI>>

    @POST("subscription/reactivate")
    suspend fun reactivateSubscription(): Response<ApiResponse<SubscriptionAPI>>


    // REPORTS
    @GET("reports/revenue")
    suspend fun getRevenue(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): Response<ApiResponse<RevenueReport>>

    @GET("reports/dashboard")
    suspend fun getDashboard(): Response<ApiResponse<DashboardData>>
}

// RESPONSE MODELS
data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T
)

data class MessageResponse(
    val success: Boolean,
    val message: String
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: AuthData
)

data class AuthData(
    val user: User,
    val token: String
)

// REQUEST MODELS
data class RegisterRequest(
    val email: String,
    val password: String,
    val full_name: String,
    val barbershop_name: String,
    val phone: String?
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class ClientRequest(
    val name: String,
    val email: String?,
    val phone: String
)

data class ServiceRequest(
    val name: String,
    val price: Double
)

data class AppointmentRequest(
    val client_id: String,
    val service_id: String,
    val start_time: String, // ISO 8601
    val notes: String?
)

data class AppointmentUpdateRequest(
    val status: String?,
    val notes: String?
)

data class CompleteAppointmentRequest(
    val status: String,
    val consumption_items: List<ConsumptionItemRequest>?
)

data class ConsumptionItemRequest(
    val product_id: String,
    val quantity: Int,
    val unit_price: Double
)

data class ProductRequest(
    val name: String,
    val quantity: Int,
    val alert_threshold: Int,
    val unit_price: Double,
    val cost_price: Double,
    val type: String // "REVENDA" ou "INSUMO"
)

data class UpgradeRequest(
    val payment_method: String
)


// API MODELS (podem ser diferentes dos locais)
data class User(
    val id: String,
    val email: String,
    val barbershop_name: String,
    val full_name: String,
    val phone: String
)

data class AppointmentFull(
    val id: String,
    val user_id: String,
    val client_id: String,
    val service_id: String,
    val start_time: String,
    val status: String,
    val notes: String?,
    val clients: ClientAPI,
    val services: ServiceAPI
)

data class ClientAPI(
    val id: String,
    val name: String,
    val phone: String
)

data class ServiceAPI(
    val id: String,
    val name: String,
    val price: Double
)

data class BarberConfigAPI(
    val opening_hour: Int,
    val closing_hour: Int,
    val slot_duration_minutes: Int,
    val has_lunch_break: Boolean,
    val lunch_start_hour: Int,
    val lunch_start_minute: Int,
    val lunch_end_hour: Int,
    val lunch_end_minute: Int
)

data class SubscriptionAPI(
    val id: String,
    val plan: String,
    val status: String,
    val start_date: String,
    val end_date: String,
    val auto_renew: Boolean,
    val payment_method: String?
)

data class DashboardData(
    val revenue: RevenueData,
    val total_clients: Int,
    val today_appointments: Int,
    val low_stock_products: Int
)

data class RevenueData(
    val total_revenue: Double,
    val total_services: Double,
    val total_products: Double,
    val attendance_count: Int
)