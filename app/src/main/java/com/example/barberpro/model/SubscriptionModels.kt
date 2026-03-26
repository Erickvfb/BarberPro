package com.example.barberpro.model

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Tipos de plano - APENAS Trial e Mensal
 */
enum class SubscriptionPlan(
    val planName: String,
    val price: Double,
    val durationDays: Int
) {
    TRIAL("Trial Gratuito", 0.0, 7),
    MONTHLY("Mensal", 29.90, 30);

    fun getFormattedPrice(): String {
        if (price == 0.0) return "Gratuito"
        val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        return formatter.format(price)
    }
}

/**
 * Status da assinatura
 */
enum class SubscriptionStatus {
    ACTIVE,
    EXPIRED,
    CANCELLED
}

/**
 * Modelo de assinatura
 */
data class Subscription(
    val id: String,
    val userId: String,
    val plan: SubscriptionPlan,
    val status: SubscriptionStatus,
    val startDate: Long,
    val endDate: Long,
    val autoRenew: Boolean = true,
    val cancelledAt: Long? = null,
    val paymentMethod: String? = null,
    val isTrialUsed: Boolean = false
) {
    fun isActive(): Boolean {
        return status == SubscriptionStatus.ACTIVE && System.currentTimeMillis() < endDate
    }

    fun getDaysRemaining(): Int {
        val diff = endDate - System.currentTimeMillis()
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }

    fun getFormattedEndDate(): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        return formatter.format(Date(endDate))
    }

    fun getFormattedNextBillingDate(): String {
        if (plan == SubscriptionPlan.TRIAL) {
            return "Não renova (período de teste)"
        }
        if (!autoRenew || status == SubscriptionStatus.CANCELLED) {
            return "Não renova automaticamente"
        }
        return getFormattedEndDate()
    }

    fun isExpiringSoon(): Boolean {
        return getDaysRemaining() <= 3 && getDaysRemaining() > 0
    }

    fun hasExpired(): Boolean {
        return System.currentTimeMillis() >= endDate
    }

    fun isTrial(): Boolean {
        return plan == SubscriptionPlan.TRIAL
    }

    fun canUpgradeToMonthly(): Boolean {
        return plan == SubscriptionPlan.TRIAL && isActive()
    }
}