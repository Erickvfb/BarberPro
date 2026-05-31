package com.example.barberpro.model

import java.util.Calendar

data class DaySchedule(

    var enabled: Boolean = true,

    var startHour: Int = 8,
    var startMinute: Int = 0,

    var endHour: Int = 18,
    var endMinute: Int = 0
)

data class BarberConfig(

    var workingDays: MutableMap<Int, DaySchedule> = mutableMapOf(

        Calendar.MONDAY to DaySchedule(
            enabled = true,
            startHour = 8,
            endHour = 19
        ),

        Calendar.TUESDAY to DaySchedule(
            enabled = true,
            startHour = 8,
            endHour = 19
        ),

        Calendar.WEDNESDAY to DaySchedule(
            enabled = true,
            startHour = 8,
            endHour = 19
        ),

        Calendar.THURSDAY to DaySchedule(
            enabled = true,
            startHour = 8,
            endHour = 19
        ),

        Calendar.FRIDAY to DaySchedule(
            enabled = true,
            startHour = 8,
            endHour = 19
        ),

        Calendar.SATURDAY to DaySchedule(
            enabled = true,
            startHour = 8,
            endHour = 18
        ),

        Calendar.SUNDAY to DaySchedule(
            enabled = false
        )
    ),

    var slotDurationMinutes: Int = 30,

    var hasLunchBreak: Boolean = true,

    var lunchStartHour: Int = 12,
    var lunchStartMinute: Int = 0,

    var lunchEndHour: Int = 13,
    var lunchEndMinute: Int = 0
) {

    companion object {

        private var instance = BarberConfig()

        fun getInstance(): BarberConfig =
            instance

        fun updateInstance(
            config: BarberConfig
        ) {
            instance = config
        }
    }
}