package com.example.barberpro.model

import java.util.*

object TimeSlotGenerator {

    /**
     * Gera lista de horários disponíveis
     * Já filtra o horário de almoço automaticamente
     */
    fun generate(config: BarberConfig = BarberConfig.getInstance()): List<String> {
        val slots = mutableListOf<String>()

        var currentHour = config.openingHour
        var currentMinute = 0

        while (currentHour < config.closingHour ||
            (currentHour == config.closingHour && currentMinute == 0)) {

            //Pula horário de almoço
            if (!isLunchTime(currentHour, currentMinute, config)) {
                slots.add(String.format("%02d:%02d", currentHour, currentMinute))
            }

            // Avança para o próximo slot
            currentMinute += config.slotDurationMinutes
            if (currentMinute >= 60) {
                currentHour++
                currentMinute = 0
            }
        }

        return slots
    }

    /**
     * Verifica se um horário está no período de almoço
     */
    private fun isLunchTime(hour: Int, minute: Int, config: BarberConfig): Boolean {
        if (!config.hasLunchBreak) return false

        val timeInMinutes = hour * 60 + minute
        val lunchStart = config.lunchStartHour * 60 + config.lunchStartMinute
        val lunchEnd = config.lunchEndHour * 60 + config.lunchEndMinute

        return timeInMinutes >= lunchStart && timeInMinutes < lunchEnd
    }
}