package com.example.barberpro.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.barberpro.R
import com.example.barberpro.model.Appointment
import com.example.barberpro.model.AppointmentStatus
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Sealed class for different item types in the agenda
 */
sealed class AgendaItem {
    data class TimeHeader(val time: String) : AgendaItem()
    data class AppointmentItem(val appointment: Appointment) : AgendaItem()
}

/**
 * Adapter for the agenda RecyclerView
 */
class AgendaAdapter(
    private val onAppointmentClick: (Appointment) -> Unit,
    private val onAppointmentDelete: (Appointment) -> Unit,
    private val onAddTimeSlot: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<AgendaItem>()

    companion object {
        private const val VIEW_TYPE_TIME_HEADER = 0
        private const val VIEW_TYPE_APPOINTMENT = 1
    }

    fun submitList(appointments: List<Appointment>) {
        items.clear()

        // Group appointments by hour
        val groupedByTime = appointments.groupBy { appointment ->
            SimpleDateFormat("HH:00", Locale.getDefault()).format(appointment.startTime)
        }

        // Create items with headers
        groupedByTime.forEach { (time, timeAppointments) ->
            items.add(AgendaItem.TimeHeader(time))
            timeAppointments.forEach { appointment ->
                items.add(AgendaItem.AppointmentItem(appointment))
            }
        }

        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is AgendaItem.TimeHeader -> VIEW_TYPE_TIME_HEADER
            is AgendaItem.AppointmentItem -> VIEW_TYPE_APPOINTMENT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_TIME_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_time_header, parent, false)
                TimeHeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_appointment, parent, false)
                AppointmentViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is AgendaItem.TimeHeader -> {
                (holder as TimeHeaderViewHolder).bind(item.time)
            }
            is AgendaItem.AppointmentItem -> {
                (holder as AppointmentViewHolder).bind(item.appointment)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    /**
     * ViewHolder for time headers
     */
    inner class TimeHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeText: TextView = itemView.findViewById(R.id.timeHeaderText)
       private val addButton: View = itemView.findViewById(R.id.addTimeSlotButton)

        fun bind(time: String) {
            timeText.text = time
            addButton.setOnClickListener {
                onAddTimeSlot(time)
            }
        }
    }

    /**
     * ViewHolder for appointments
     */
    inner class AppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val serviceNameText: TextView = itemView.findViewById(R.id.serviceNameText)
        private val priceText: TextView = itemView.findViewById(R.id.priceText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        private val clientInitialText: TextView = itemView.findViewById(R.id.clientInitialText)
        private val clientNameText: TextView = itemView.findViewById(R.id.clientNameText)
        private val statusText: TextView = itemView.findViewById(R.id.statusText)
        private val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)

        fun bind(appointment: Appointment) {
            // Service info
            serviceNameText.text = appointment.service.name

            // Price and duration
            val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
            priceText.text = "${appointment.service.durationMinutes} min • ${formatter.format(appointment.service.price)}"

            // Time range
            timeText.text = appointment.getTimeRange()

            // Client info
            clientInitialText.text = appointment.client.getInitial()
            clientNameText.text = appointment.client.name

            // Status
            statusText.text = appointment.status.displayName
            statusText.setTextColor(android.graphics.Color.parseColor(appointment.status.color))

            // Click listeners
            itemView.setOnClickListener {
                onAppointmentClick(appointment)
            }

            deleteButton.setOnClickListener {
                onAppointmentDelete(appointment)
            }
        }
    }
}