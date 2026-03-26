package com.example.barberpro.repository


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import com.example.barberpro.model.BarberProfile

/**
 * Repository for profile management
 */
class ProfileRepository {

    private var currentProfile: BarberProfile = BarberProfile(
        id = "1",
        barbeariaNome = "Vintage Barber Shop",
        nomeCompleto = "Ricardo Oliveira",
        email = "ricardo.barber@email.com",
        telefone = "11987654321",
        especialidade = "Corte Moderno & Barba",
        photoUrl = null
    )

    /**
     * Get current profile
     */
    suspend fun getProfile(): Result<BarberProfile> = withContext(Dispatchers.IO) {
        try {
            delay(500) // Simulate network delay
            Result.success(currentProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update profile
     */
    suspend fun updateProfile(profile: BarberProfile): Result<BarberProfile> = withContext(Dispatchers.IO) {
        try {
            delay(600)
            currentProfile = profile
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload profile photo
     */
    suspend fun uploadProfilePhoto(photoUri: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            delay(1000) // Simulate upload
            val photoUrl = "https://example.com/photos/${System.currentTimeMillis()}.jpg"
            currentProfile = currentProfile.copy(photoUrl = photoUrl)
            Result.success(photoUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Change password
     */
    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                delay(800)

                // TODO: Implement actual password validation
                if (oldPassword.isEmpty() || newPassword.isEmpty()) {
                    return@withContext Result.failure(Exception("Senhas não podem ser vazias"))
                }

                if (newPassword.length < 8) {
                    return@withContext Result.failure(Exception("Nova senha deve ter no mínimo 8 caracteres"))
                }

                Result.success(true)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Delete account
     */
    suspend fun deleteAccount(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            delay(600)
            // TODO: Implement account deletion
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ProfileRepository? = null

        fun getInstance(): ProfileRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ProfileRepository().also { INSTANCE = it }
            }
        }
    }
}