package com.example.barberpro.repository

import com.example.barberpro.data.api.*
import com.example.barberpro.model.BarberProfile
import okhttp3.ResponseBody
import org.json.JSONObject

class AuthRepository(
    private val apiService: ApiService = RetrofitClient.apiService
) {

    suspend fun register(
        email: String,
        password: String,
        fullName: String,
        barbershopName: String,
        phone: String
    ): Result<String> {
        return try {
            val request = RegisterRequest(email, password, fullName, barbershopName, phone)
            val response = apiService.register(request)

            if (response.isSuccessful) {
                val body = response.body()

                if (body?.success == true) {
                    val token = body.data.token
                    RetrofitClient.setToken(token)
                    Result.success(token)
                } else {
                    Result.failure(Exception(body?.message ?: "Erro no registro"))
                }

            } else {
                Result.failure(Exception(parseError(response.errorBody())))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<String> {
        return try {
            val request = LoginRequest(email, password)
            val response = apiService.login(request)

            if (response.isSuccessful) {
                val body = response.body()

                if (body?.success == true) {
                    val token = body.data.token
                    RetrofitClient.setToken(token)
                    Result.success(token)
                } else {
                    Result.failure(Exception(body?.message ?: "Erro ao fazer login"))
                }

            } else {
                Result.failure(Exception(parseError(response.errorBody())))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            val response = apiService.logout()

            if (response.isSuccessful) {
                RetrofitClient.clearToken()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Erro ao fazer logout"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Extrai mensagem de erro da API
     */
    private fun parseError(errorBody: ResponseBody?): String {
        return try {
            val json = JSONObject(errorBody?.string() ?: "")
            json.optString("message", "Erro desconhecido")
        } catch (e: Exception) {
            "Erro de comunicação com servidor"
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AuthRepository? = null

        fun getInstance(): AuthRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthRepository().also { INSTANCE = it }
            }
        }
    }

    suspend fun updateProfile(profile: BarberProfile): Result<Unit> {
        return try {
            val response = ApiClient.apiService.updateProfile(
                UpdateProfileRequest(
                    email = profile.email,
                    full_name = profile.nomeCompleto,
                    barbershop_name = profile.barbeariaNome,
                    phone = profile.telefone
                )
            )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Erro ao atualizar"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}