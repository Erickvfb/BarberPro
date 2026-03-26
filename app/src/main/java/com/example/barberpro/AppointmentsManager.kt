package com.example.barberpro

import com.example.barberpro.model.Appointment

/**
 * Singleton para gerenciar a lista de agendamentos
 * Permite que diferentes Activities acessem e modifiquem a mesma lista
 */
object AppointmentsManager {

    val appointments = mutableListOf<Appointment>()

    /**
     * Remove um agendamento pelo ID
     */
    fun removeAppointment(appointmentId: String) {
        appointments.removeIf { it.id == appointmentId }
    }

    /**
     * Adiciona um agendamento
     */
    fun addAppointment(appointment: Appointment) {
        appointments.add(appointment)
    }

    /**
     * Limpa todos os agendamentos
     */
    fun clear() {
        appointments.clear()
    }

    /**
     * Retorna todos os agendamentos
     */
    fun getAll(): List<Appointment> {
        return appointments.toList()
    }
}