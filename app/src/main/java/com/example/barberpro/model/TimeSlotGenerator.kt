package com.example.barberpro.model

import java.util.Calendar

object TimeSlotGenerator {

    fun generate(
        dayOfWeek: Int,
        config: BarberConfig = BarberConfig.getInstance()
    ): List<String> {

        val schedule =
            config.workingDays[dayOfWeek]
                ?: return emptyList()

        if (!schedule.enabled) {
            return emptyList()
        }

        val slots = mutableListOf<String>()

        var currentHour =
            schedule.startHour

        var currentMinute =
            schedule.startMinute

        val endInMinutes =
            schedule.endHour * 60 +
                    schedule.endMinute

        while (
            currentHour * 60 + currentMinute
            <= endInMinutes
        ) {

            if (
                !isLunchTime(
                    currentHour,
                    currentMinute,
                    config
                )
            ) {

                slots.add(
                    String.format(
                        "%02d:%02d",
                        currentHour,
                        currentMinute
                    )
                )
            }

            currentMinute +=
                config.slotDurationMinutes

            while (currentMinute >= 60) {

                currentHour++

                currentMinute -= 60
            }
        }

        return slots
    }

    private fun isLunchTime(
        hour: Int,
        minute: Int,
        config: BarberConfig
    ): Boolean {

        if (!config.hasLunchBreak) {
            return false
        }

        val current =
            hour * 60 + minute

        val lunchStart =
            config.lunchStartHour * 60 +
                    config.lunchStartMinute

        val lunchEnd =
            config.lunchEndHour * 60 +
                    config.lunchEndMinute

        return current >= lunchStart &&
                current < lunchEnd
    }
}