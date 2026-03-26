package com.example.barberpro.model

data class BarberConfig(
    val openingHour: Int = 9,
    val closingHour: Int = 18,
    val slotDurationMinutes: Int = 30
)