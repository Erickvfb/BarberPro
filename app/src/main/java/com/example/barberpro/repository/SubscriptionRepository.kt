package com.example.barberpro.repository

import com.example.barberpro.model.Subscription
import com.example.barberpro.model.SubscriptionPlan
import com.example.barberpro.model.SubscriptionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.*

class SubscriptionRepository {

    private var currentSubscription: Subscription? = null

    init {
        loadMockSubscription()
    }

    suspend fun getCurrentSubscription(userId: String): Result<Subscription?> = withContext(Dispatchers.IO) {
        try {
            delay(300)
            Result.success(currentSubscription)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTrialSubscription(userId: String): Result<Subscription> = withContext(Dispatchers.IO) {
        try {
            delay(600)

            if (currentSubscription != null) {
                return@withContext Result.failure(Exception("Usuário já possui assinatura"))
            }

            val calendar = Calendar.getInstance()
            val startDate = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_MONTH, 7)
            val endDate = calendar.timeInMillis

            val trial = Subscription(
                id = "sub_trial_${UUID.randomUUID()}",
                userId = userId,
                plan = SubscriptionPlan.TRIAL,
                status = SubscriptionStatus.ACTIVE,
                startDate = startDate,
                endDate = endDate,
                autoRenew = false,
                cancelledAt = null,
                paymentMethod = null,
                isTrialUsed = true
            )

            currentSubscription = trial
            Result.success(trial)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun upgradeToMonthly(
        subscriptionId: String,
        paymentMethod: String
    ): Result<Subscription> = withContext(Dispatchers.IO) {
        try {
            delay(800)

            val subscription = currentSubscription
            if (subscription == null || subscription.id != subscriptionId) {
                return@withContext Result.failure(Exception("Assinatura não encontrada"))
            }

            if (subscription.plan != SubscriptionPlan.TRIAL) {
                return@withContext Result.failure(Exception("Upgrade só é possível do Trial"))
            }

            val calendar = Calendar.getInstance()
            val startDate = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_MONTH, 30)
            val endDate = calendar.timeInMillis

            val monthly = Subscription(
                id = "sub_monthly_${UUID.randomUUID()}",
                userId = subscription.userId,
                plan = SubscriptionPlan.MONTHLY,
                status = SubscriptionStatus.ACTIVE,
                startDate = startDate,
                endDate = endDate,
                autoRenew = true,
                cancelledAt = null,
                paymentMethod = paymentMethod,
                isTrialUsed = true
            )

            currentSubscription = monthly
            Result.success(monthly)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelSubscription(subscriptionId: String): Result<Subscription> = withContext(Dispatchers.IO) {
        try {
            delay(600)

            val subscription = currentSubscription
            if (subscription == null || subscription.id != subscriptionId) {
                return@withContext Result.failure(Exception("Assinatura não encontrada"))
            }

            if (subscription.plan == SubscriptionPlan.TRIAL) {
                return@withContext Result.failure(Exception("Trial não pode ser cancelado"))
            }

            val cancelled = subscription.copy(
                status = SubscriptionStatus.CANCELLED,
                autoRenew = false,
                cancelledAt = System.currentTimeMillis()
            )

            currentSubscription = cancelled
            Result.success(cancelled)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reactivateSubscription(subscriptionId: String): Result<Subscription> = withContext(Dispatchers.IO) {
        try {
            delay(600)

            val subscription = currentSubscription
            if (subscription == null || subscription.id != subscriptionId) {
                return@withContext Result.failure(Exception("Assinatura não encontrada"))
            }

            if (subscription.status != SubscriptionStatus.CANCELLED) {
                return@withContext Result.failure(Exception("Assinatura não está cancelada"))
            }

            val reactivated = subscription.copy(
                status = SubscriptionStatus.ACTIVE,
                autoRenew = true,
                cancelledAt = null
            )

            currentSubscription = reactivated
            Result.success(reactivated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePaymentMethod(
        subscriptionId: String,
        paymentMethod: String
    ): Result<Subscription> = withContext(Dispatchers.IO) {
        try {
            delay(600)

            val subscription = currentSubscription
            if (subscription == null || subscription.id != subscriptionId) {
                return@withContext Result.failure(Exception("Assinatura não encontrada"))
            }

            val updated = subscription.copy(paymentMethod = paymentMethod)
            currentSubscription = updated
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun loadMockSubscription() {
        val calendar = Calendar.getInstance()
        val startDate = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 30)

        currentSubscription = Subscription(
            id = "sub_monthly_123",
            userId = "user_1",
            plan = SubscriptionPlan.MONTHLY,
            status = SubscriptionStatus.ACTIVE,
            startDate = startDate,
            endDate = calendar.timeInMillis,
            autoRenew = true,
            cancelledAt = null,
            paymentMethod = "Cartão de Crédito •••• 1234",
            isTrialUsed = true
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: SubscriptionRepository? = null

        fun getInstance(): SubscriptionRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SubscriptionRepository().also { INSTANCE = it }
            }
        }
    }
}