package com.example.barberpro

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.barberpro.model.Subscription
import com.example.barberpro.model.SubscriptionStatus
import com.example.barberpro.repository.SubscriptionRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class SubscriptionManagementActivity : BaseActivity() {

    private lateinit var backButton: ImageView
    private lateinit var planBadgeText: TextView
    private lateinit var planPriceText: TextView
    private lateinit var planDurationText: TextView
    private lateinit var statusText: TextView
    private lateinit var nextBillingText: TextView
    private lateinit var paymentMethodText: TextView
    private lateinit var paymentMethodRow: View
    private lateinit var warningCard: MaterialCardView
    private lateinit var warningText: TextView
    private lateinit var changePaymentCard: MaterialCardView
    private lateinit var upgradeCard: MaterialCardView
    private lateinit var upgradeButton: MaterialButton
    private lateinit var cancelSubscriptionButton: MaterialButton

    private val subscriptionRepository = SubscriptionRepository.getInstance()
    private var currentSubscription: Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscription_management)

        initViews()
        setupClicks()
        loadSubscription()
    }

    private fun initViews() {
        backButton = findViewById(R.id.backButton)
        planBadgeText = findViewById(R.id.planBadgeText)
        planPriceText = findViewById(R.id.planPriceText)
        planDurationText = findViewById(R.id.planDurationText)
        statusText = findViewById(R.id.statusText)
        nextBillingText = findViewById(R.id.nextBillingText)
        paymentMethodText = findViewById(R.id.paymentMethodText)
        paymentMethodRow = findViewById(R.id.paymentMethodRow)
        warningCard = findViewById(R.id.warningCard)
        warningText = findViewById(R.id.warningText)
        changePaymentCard = findViewById(R.id.changePaymentCard)
        upgradeCard = findViewById(R.id.upgradeCard)
        upgradeButton = findViewById(R.id.upgradeButton)
        cancelSubscriptionButton = findViewById(R.id.cancelSubscriptionButton)
    }

    private fun setupClicks() {
        backButton.setOnClickListener { finish() }

        changePaymentCard.setOnClickListener {
            Toast.makeText(this, "Recurso em desenvolvimento", Toast.LENGTH_SHORT).show()
        }

        upgradeButton.setOnClickListener {
            val subscription = currentSubscription ?: return@setOnClickListener
            if (subscription.isTrial()) {
                showUpgradeDialog(subscription)
            }
        }

        cancelSubscriptionButton.setOnClickListener {
            val subscription = currentSubscription ?: return@setOnClickListener

            when {
                subscription.isTrial() -> {
                    Toast.makeText(
                        this,
                        "O período de teste expira automaticamente",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                subscription.status == SubscriptionStatus.CANCELLED -> {
                    showReactivateDialog(subscription)
                }
                else -> {
                    showCancelDialog(subscription)
                }
            }
        }
    }

    private fun loadSubscription() {
        lifecycleScope.launch {
            val result = subscriptionRepository.getCurrentSubscription("user_1")

            result.onSuccess { subscription ->
                if (subscription != null) {
                    currentSubscription = subscription
                    updateUI(subscription)
                } else {
                    Toast.makeText(
                        this@SubscriptionManagementActivity,
                        "Nenhuma assinatura ativa",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }

            result.onFailure { error ->
                Toast.makeText(
                    this@SubscriptionManagementActivity,
                    "Erro ao carregar assinatura: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun updateUI(subscription: Subscription) {
        if (subscription.isTrial()) {
            planBadgeText.text = "PERÍODO DE TESTE"
            planBadgeText.setBackgroundColor(android.graphics.Color.parseColor("#1A2B3B"))
            planBadgeText.setTextColor(android.graphics.Color.parseColor("#3B82F6"))
        } else {
            planBadgeText.text = "PLANO MENSAL"
            planBadgeText.setBackgroundColor(android.graphics.Color.parseColor("#1A2415"))
            planBadgeText.setTextColor(android.graphics.Color.parseColor("#D4AF37"))
        }

        planPriceText.text = subscription.plan.getFormattedPrice()
        planDurationText.text = if (subscription.isTrial()) {
            "${subscription.getDaysRemaining()} dias restantes"
        } else {
            "por mês"
        }

        when (subscription.status) {
            SubscriptionStatus.ACTIVE -> {
                statusText.text = if (subscription.isTrial()) "Trial Ativo" else "Ativa"
                statusText.setTextColor(android.graphics.Color.parseColor("#22C55E"))
            }
            SubscriptionStatus.CANCELLED -> {
                statusText.text = "Cancelada"
                statusText.setTextColor(android.graphics.Color.parseColor("#EF4444"))
            }
            SubscriptionStatus.EXPIRED -> {
                statusText.text = "Expirada"
                statusText.setTextColor(android.graphics.Color.parseColor("#F59E0B"))
            }
        }

        nextBillingText.text = subscription.getFormattedNextBillingDate()

        if (subscription.isTrial()) {
            paymentMethodRow.visibility = View.GONE
            changePaymentCard.visibility = View.GONE
        } else {
            paymentMethodRow.visibility = View.VISIBLE
            changePaymentCard.visibility = View.VISIBLE
            paymentMethodText.text = subscription.paymentMethod ?: "Não cadastrado"
        }

        if (subscription.canUpgradeToMonthly()) {
            upgradeCard.visibility = View.VISIBLE
        } else {
            upgradeCard.visibility = View.GONE
        }

        when {
            subscription.isTrial() -> {
                cancelSubscriptionButton.visibility = View.GONE
            }
            subscription.status == SubscriptionStatus.CANCELLED -> {
                cancelSubscriptionButton.text = "REATIVAR ASSINATURA"
                cancelSubscriptionButton.visibility = View.VISIBLE
            }
            else -> {
                cancelSubscriptionButton.text = "CANCELAR ASSINATURA"
                cancelSubscriptionButton.visibility = View.VISIBLE
            }
        }

        when {
            subscription.isTrial() && subscription.isExpiringSoon() -> {
                warningCard.visibility = View.VISIBLE
                warningText.text = "Seu trial expira em ${subscription.getDaysRemaining()} dias. Faça upgrade para continuar!"
            }
            subscription.status == SubscriptionStatus.CANCELLED -> {
                warningCard.visibility = View.VISIBLE
                warningText.text = "Sua assinatura foi cancelada e expira em ${subscription.getFormattedEndDate()}"
            }
            subscription.isExpiringSoon() && !subscription.isTrial() -> {
                warningCard.visibility = View.VISIBLE
                warningText.text = "Sua assinatura expira em ${subscription.getDaysRemaining()} dias"
            }
            else -> {
                warningCard.visibility = View.GONE
            }
        }
    }

    private fun showUpgradeDialog(subscription: Subscription) {
        val message = buildString {
            append("Fazer upgrade para o Plano Mensal?\n\n")
            append("✨ R$ 29,90/mês\n")
            append("✨ Acesso completo ilimitado\n")
            append("✨ Renovação automática\n")
            append("✨ Sem interrupções\n\n")
            append("Você será cobrado R$ 29,90 agora e mensalmente.")
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Upgrade para Mensal")
            .setMessage(message)
            .setPositiveButton("Fazer Upgrade") { _, _ ->
                upgradeToMonthly(subscription.id)
            }
            .setNegativeButton("Não agora", null)
            .show()
    }

    private fun showCancelDialog(subscription: Subscription) {
        val message = buildString {
            append("Tem certeza que deseja cancelar sua assinatura?\n\n")
            append("• Você continuará com acesso até ${subscription.getFormattedEndDate()}\n")
            append("• Não haverá cobrança automática\n")
            append("• Você pode reativar a qualquer momento")
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Cancelar Assinatura?")
            .setMessage(message)
            .setPositiveButton("Sim, cancelar") { _, _ ->
                cancelSubscription(subscription.id)
            }
            .setNegativeButton("Não, manter", null)
            .show()
    }

    private fun showReactivateDialog(subscription: Subscription) {
        val message = buildString {
            append("Reativar sua assinatura?\n\n")
            append("• Plano Mensal: R$ 29,90/mês\n")
            append("• Renovação automática ativada\n")
            append("• Acesso imediato restaurado")
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Reativar Assinatura")
            .setMessage(message)
            .setPositiveButton("Reativar") { _, _ ->
                reactivateSubscription(subscription.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun upgradeToMonthly(subscriptionId: String) {
        lifecycleScope.launch {
            val result = subscriptionRepository.upgradeToMonthly(
                subscriptionId,
                "Cartão de Crédito •••• 1234"
            )

            result.onSuccess { upgraded ->
                currentSubscription = upgraded
                updateUI(upgraded)

                Toast.makeText(
                    this@SubscriptionManagementActivity,
                    "Upgrade realizado com sucesso! 🎉",
                    Toast.LENGTH_SHORT
                ).show()
            }

            result.onFailure { error ->
                Toast.makeText(
                    this@SubscriptionManagementActivity,
                    "Erro no upgrade: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun cancelSubscription(subscriptionId: String) {
        cancelSubscriptionButton.isEnabled = false
        cancelSubscriptionButton.text = "Cancelando..."

        lifecycleScope.launch {
            val result = subscriptionRepository.cancelSubscription(subscriptionId)

            result.onSuccess { cancelled ->
                currentSubscription = cancelled
                updateUI(cancelled)

                Toast.makeText(
                    this@SubscriptionManagementActivity,
                    "Assinatura cancelada com sucesso",
                    Toast.LENGTH_SHORT
                ).show()
            }

            result.onFailure { error ->
                Toast.makeText(
                    this@SubscriptionManagementActivity,
                    "Erro ao cancelar: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

            cancelSubscriptionButton.isEnabled = true
        }
    }

    private fun reactivateSubscription(subscriptionId: String) {
        cancelSubscriptionButton.isEnabled = false
        cancelSubscriptionButton.text = "Reativando..."

        lifecycleScope.launch {
            val result = subscriptionRepository.reactivateSubscription(subscriptionId)

            result.onSuccess { reactivated ->
                currentSubscription = reactivated
                updateUI(reactivated)

                Toast.makeText(
                    this@SubscriptionManagementActivity,
                    "Assinatura reativada com sucesso! 🎉",
                    Toast.LENGTH_SHORT
                ).show()
            }

            result.onFailure { error ->
                Toast.makeText(
                    this@SubscriptionManagementActivity,
                    "Erro ao reativar: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

            cancelSubscriptionButton.isEnabled = true
        }
    }
}