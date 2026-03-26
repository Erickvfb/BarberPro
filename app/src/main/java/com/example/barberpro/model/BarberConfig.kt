package com.example.barberpro.model

/**
 * Configuração única da barbearia
 * Horário de funcionamento + Horário de almoço
 */
data class BarberConfig(
    // Horário de funcionamento
    val openingHour: Int = 9,
    val closingHour: Int = 18,
    val slotDurationMinutes: Int = 30,

    // Horário de almoço
    val hasLunchBreak: Boolean = true,
    val lunchStartHour: Int = 12,
    val lunchStartMinute: Int = 0,
    val lunchEndHour: Int = 13,
    val lunchEndMinute: Int = 0
) {
    companion object {
        // Instância única que será usada em todo o app
        private var instance: BarberConfig = BarberConfig()

        fun getInstance() = instance

        fun updateInstance(config: BarberConfig) {
            instance = config
        }
    }
}