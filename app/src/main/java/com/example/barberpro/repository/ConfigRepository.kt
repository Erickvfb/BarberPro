package com.example.barberpro.repository

import com.example.barberpro.data.api.BarberConfigAPI
import com.example.barberpro.data.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ConfigRepository private constructor() {

    private val apiService = RetrofitClient.apiService

    suspend fun getConfig(): Result<BarberConfigAPI> {

        return withContext(Dispatchers.IO) {

            try {

                val response = apiService.getConfig()

                if (response.isSuccessful) {

                    val config = response.body()?.data

                    if (config != null) {

                        Result.success(config)

                    } else {

                        Result.failure(
                            Exception("Configuração não encontrada")
                        )
                    }

                } else {

                    Result.failure(
                        Exception("Erro ao carregar configuração")
                    )
                }

            } catch (e: Exception) {

                Result.failure(e)
            }
        }
    }

    suspend fun updateConfig(
        config: BarberConfigAPI
    ): Result<BarberConfigAPI> {

        return withContext(Dispatchers.IO) {

            try {

                val response =
                    apiService.updateConfig(config)

                if (response.isSuccessful) {

                    val updated = response.body()?.data

                    if (updated != null) {

                        Result.success(updated)

                    } else {

                        Result.failure(
                            Exception("Erro ao salvar configuração")
                        )
                    }

                } else {

                    Result.failure(
                        Exception("Erro ao salvar configuração")
                    )
                }

            } catch (e: Exception) {

                Result.failure(e)
            }
        }
    }

    companion object {

        @Volatile
        private var INSTANCE: ConfigRepository? = null

        fun getInstance(): ConfigRepository =
            INSTANCE ?: synchronized(this) {

                INSTANCE ?: ConfigRepository().also {
                    INSTANCE = it
                }
            }
    }
}