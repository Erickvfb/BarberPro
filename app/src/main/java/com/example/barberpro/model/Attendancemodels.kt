package com.example.barberpro.model

/**
 * Status do comparecimento do cliente
 */
enum class AttendanceStatus {
    PENDING,       // Aguardando confirmação
    ATTENDED,      // Compareceu
    NO_SHOW        // Não compareceu
}

/**
 * Item de consumo adicionado ao atendimento (produto extra)
 */
data class ConsumptionItem(
    val id: String,
    val productId: String,
    val name: String,
    val unitPrice: Double,
    val quantity: Int
) {
    fun getTotal(): Double {
        return unitPrice * quantity
    }
}

/**
 * Registro completo do atendimento finalizado
 */
data class AttendanceRecord(
    val id: String,
    val appointmentId: String,
    val clientId: String,
    val clientName: String,
    val serviceId: String,
    val serviceName: String,
    val servicePrice: Double,
    val scheduledTime: Long,
    val status: AttendanceStatus,
    val consumptions: List<ConsumptionItem> = emptyList(),
    val finishedAt: Long = System.currentTimeMillis()
)
{

    fun getConsumptionsTotal(): Double {
        return consumptions.sumOf { it.getTotal() }
    }

    fun getTotalValue(): Double {
        if (status == AttendanceStatus.NO_SHOW) return 0.0
        return servicePrice + getConsumptionsTotal()
    }
}
data class ServiceInfoResponse(
    val id: String? = null,
    val name: String? = null,
    val price: Double? = null
)

data class AttendanceRecordResponse(
    val id: String? = null,
    val user_id: String? = null,
    val client_id: String? = null,
    val service_id: String? = null,
    val service_price: Double? = null,
    val total_value: Double? = null,
    val status: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val finished_at: String? = null,
    val started_at: String? = null,
    val scheduled_time: String? = null,
    val services: ServiceInfoResponse? = null  // ← Serviço aninhado
)