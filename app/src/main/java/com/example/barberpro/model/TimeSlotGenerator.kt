package com.example.barberpro.model

import java.text.SimpleDateFormat
import java.util.*

object TimeSlotGenerator {

    fun generate(config: com.example.barberpro.model.BarberScheduleConfig): List<String> {
        val slots = mutableListOf<String>()
        val calendar = Calendar.getInstance()

        calendar.set(Calendar.HOUR_OF_DAY, config.openingHour)
        calendar.set(Calendar.MINUTE, 0)

        val end = Calendar.getInstance()
        end.set(Calendar.HOUR_OF_DAY, config.closingHour)
        end.set(Calendar.MINUTE, 0)

        while (calendar.before(end)) {
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            slots.add(String.format("%02d:%02d", hour, minute))
            calendar.add(Calendar.MINUTE, config.slotMinutes)
        }

        return slots
    }
}