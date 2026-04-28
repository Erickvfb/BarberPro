package com.example.barberpro.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Android Emulator: 10.0.2.2
    // Dispositivo Físico: IP real da máquina (ex: 192.168.1.100)
    private const val BASE_URL = "http://10.0.2.2:3000/api/v1/"

    // Token storage (temporário - use SharedPreferences em produção)
    private var authToken: String? = null

    fun setToken(token: String) {
        authToken = token
    }

    fun getToken(): String? = authToken

    fun clearToken() {
        authToken = null
    }

    // Logging
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // HTTP Client
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()

            // Adicionar token se existir
            authToken?.let {
                requestBuilder.addHeader("Authorization", "Bearer $it")
            }

            val request = requestBuilder.build()
            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Retrofit instance
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // API Service
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}