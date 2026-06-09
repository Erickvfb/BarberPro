package com.example.barberpro

import SettingsCalendarFragment
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.barberpro.adapter.AgendaAdapter
import com.example.barberpro.adapter.AvailableDaysAdapter
import com.example.barberpro.data.api.AvailableDay
import com.example.barberpro.model.Appointment
import com.example.barberpro.model.BarberConfig
import com.example.barberpro.repository.AppointmentRepository
import com.example.barberpro.ui.schedule.NewScheduleFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class HomeFragment : Fragment() {

    private lateinit var daysRecyclerView: RecyclerView
    private lateinit var dateRangeText: TextView
    private lateinit var servicesCountText: TextView
    private lateinit var productsCountText: TextView
    private lateinit var totalRevenueText: TextView
    private lateinit var appointmentsRecyclerView: RecyclerView
    private lateinit var fabAddAppointment: FloatingActionButton

    private lateinit var agendaAdapter: AgendaAdapter
    private lateinit var daysAdapter: AvailableDaysAdapter

    private var selectedDate: Date = Date()

    private val prefs by lazy {
        requireContext().getSharedPreferences(
            "agenda_prefs",
            Context.MODE_PRIVATE
        )
    }

    private val appointmentRepository =
        AppointmentRepository.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(
            R.layout.fragment_home,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)

        setupRecyclerView()

        setupCalendar()

        setupClickListeners()

        listenConfirmResult()

        loadAppointments()
    }

    private fun initializeViews(view: View) {

        daysRecyclerView =
            view.findViewById(R.id.daysRecyclerView)

        dateRangeText =
            view.findViewById(R.id.dateRangeText)

        servicesCountText =
            view.findViewById(R.id.servicesCountText)

        productsCountText =
            view.findViewById(R.id.productsCountText)

        totalRevenueText =
            view.findViewById(R.id.totalRevenueText)

        appointmentsRecyclerView =
            view.findViewById(R.id.appointmentsRecyclerView)

        fabAddAppointment =
            view.findViewById(R.id.fabAddAppointment)
    }

    private fun setupRecyclerView() {

        agendaAdapter = AgendaAdapter(

            onAppointmentDelete = { appointment ->
                deleteAppointment(appointment)
            },

            onAppointmentClick = { appointment ->

                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragmentContainer,
                        ConfirmAttendanceFragment.newInstance(
                            appointment.id
                        )
                    )
                    .addToBackStack(null)
                    .commit()
            }
        )

        appointmentsRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        appointmentsRecyclerView.adapter =
            agendaAdapter
    }

    private fun setupCalendar() {

        daysAdapter = AvailableDaysAdapter { day ->

            selectedDate =
                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                ).parse(day.date) ?: Date()

            loadAppointments()
        }

        daysRecyclerView.layoutManager =
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )

        daysRecyclerView.adapter =
            daysAdapter

        lifecycleScope.launch {

            val savedDays =
                loadSavedAvailableDays()

            if (savedDays.isNotEmpty()) {

                val updatedDays =
                    updateAvailableSlots(savedDays)

                daysAdapter.submitList(updatedDays)

                selectedDate =
                    SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                    ).parse(updatedDays.first().date)
                        ?: Date()

            } else {

                val days =
                    mutableListOf<AvailableDay>()

                val calendar =
                    Calendar.getInstance()

                repeat(7) {

                    val date =
                        SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.getDefault()
                        ).format(calendar.time)

                    days.add(
                        AvailableDay(
                            id = UUID.randomUUID().toString(),
                            date = date,
                            availableSlots = 0
                        )
                    )

                    calendar.add(
                        Calendar.DAY_OF_MONTH,
                        1
                    )
                }

                val updatedDays =
                    updateAvailableSlots(days)

                daysAdapter.submitList(updatedDays)

                saveAvailableDays(updatedDays)
            }

            updateDateRange()
        }
    }

    private suspend fun updateAvailableSlots(
        days: List<AvailableDay>
    ): List<AvailableDay> {

        return days.map { day ->

            lifecycleScope.async {

                try {

                    val formatter =
                        SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.getDefault()
                        )

                    val date =
                        formatter.parse(day.date)
                            ?: Date()

                    val result =
                        appointmentRepository
                            .getAppointmentsByDate(date)

                    var totalAppointments = 0

                    result.onSuccess { appointments ->

                        totalAppointments =
                            appointments.size
                    }

                    day.copy(
                        availableSlots =
                        totalAppointments
                    )

                } catch (e: Exception) {

                    day.copy(
                        availableSlots = 0
                    )
                }
            }
        }.awaitAll()
    }

    private fun saveAvailableDays(days: List<AvailableDay>) {

        if (days.isEmpty()) return

        prefs.edit()
            .putString("start_date", days.first().date)
            .putString("end_date", days.last().date)
            .apply()
    }

    private fun loadSavedAvailableDays(): List<AvailableDay> {

        val startDate =
            prefs.getString("start_date", null)
                ?: return emptyList()

        val endDate =
            prefs.getString("end_date", null)
                ?: return emptyList()

        val formatter =
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            )

        val start =
            formatter.parse(startDate)
                ?: return emptyList()

        val end =
            formatter.parse(endDate)
                ?: return emptyList()

        val config =
            BarberConfig.getInstance()

        val calendar =
            Calendar.getInstance()

        calendar.time = start

        val endCalendar =
            Calendar.getInstance()

        endCalendar.time = end

        val result =
            mutableListOf<AvailableDay>()

        while (!calendar.after(endCalendar)) {

            val dayOfWeek =
                calendar.get(Calendar.DAY_OF_WEEK)

            val schedule =
                config.workingDays[dayOfWeek]

            if (
                schedule != null &&
                schedule.enabled
            ) {

                result.add(
                    AvailableDay(
                        id = UUID.randomUUID().toString(),
                        date = formatter.format(calendar.time),
                        availableSlots = 0
                    )
                )
            }

            calendar.add(
                Calendar.DAY_OF_MONTH,
                1
            )
        }

        return result
    }

    private fun loadAppointments() {

        lifecycleScope.launch {

            val result =
                appointmentRepository
                    .getAppointmentsByDate(
                        selectedDate
                    )

            result.onSuccess { appointments ->

                agendaAdapter.submitList(
                    appointments
                )

                servicesCountText.text =
                    "${appointments.size} serviços"

                productsCountText.text =
                    "${appointments.sumOf { it.products.size }} produtos"

                val total =
                    appointments.sumOf {
                        it.getTotalRevenue()
                    }

                totalRevenueText.text =
                    NumberFormat.getCurrencyInstance(
                        Locale("pt", "BR")
                    ).format(total)

            }

            result.onFailure {

                toast(
                    it.message
                        ?: "Erro ao carregar agenda"
                )
            }
        }
    }

    private fun deleteAppointment(
        appointment: Appointment
    ) {

        MaterialAlertDialogBuilder(
            requireContext()
        )
            .setTitle(
                "Cancelar Agendamento"
            )

            .setMessage(
                "Deseja realmente cancelar o agendamento de ${appointment.client.name}?"
            )

            .setPositiveButton(
                "Sim"
            ) { _, _ ->

                lifecycleScope.launch {

                    val result =
                        appointmentRepository
                            .deleteAppointment(
                                appointment.id
                            )

                    result.onSuccess {

                        toast(
                            "Agendamento cancelado"
                        )

                        loadAppointments()
                    }

                    result.onFailure {

                        toast(
                            it.message
                                ?: "Erro ao cancelar"
                        )
                    }
                }
            }

            .setNegativeButton(
                "Não",
                null
            )

            .show()
    }

    override fun onResume() {

        super.onResume()

        loadAppointments()
    }

    private fun setupClickListeners() {

        fabAddAppointment.setOnClickListener {

            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    NewScheduleFragment()
                )
                .addToBackStack(null)
                .commit()
        }

        view?.findViewById<View>(
            R.id.calendarButton
        )?.setOnClickListener {

            openDateRangePicker()
        }

        view?.findViewById<View>(
            R.id.defineAgendaButton
        )?.setOnClickListener {

            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    SettingsCalendarFragment()
                )
                .addToBackStack(null)
                .commit()
        }
    }

    private fun listenConfirmResult() {

        parentFragmentManager
            .setFragmentResultListener(
                "confirm_attendance_result",
                viewLifecycleOwner
            ) { _, _ ->

                loadAppointments()
            }
    }

    private fun updateDateRange() {

        val currentDays =
            loadSavedAvailableDays()

        if (currentDays.isEmpty()) {

            dateRangeText.text =
                "Nenhum período selecionado"

            return
        }

        val formatter =
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            )

        val displayFormatter =
            SimpleDateFormat(
                "dd MMM yyyy",
                Locale("pt", "BR")
            )

        val start =
            formatter.parse(
                currentDays.first().date
            )

        val end =
            formatter.parse(
                currentDays.last().date
            )

        dateRangeText.text =
            "${displayFormatter.format(start!!)} - ${
                displayFormatter.format(end!!)
            }"
    }

    private fun openDateRangePicker() {

        val datePicker =
            MaterialDatePicker.Builder
                .dateRangePicker()
                .setTitleText(
                    "Selecione o período disponível"
                )
                .setTheme(
                    R.style.ThemeOverlay_BarberPro_MaterialCalendar
                )
                .build()

        datePicker.show(
            parentFragmentManager,
            "DATE_RANGE_PICKER"
        )

        datePicker
            .addOnPositiveButtonClickListener { selection ->

                val startDate =
                    selection.first

                val endDate =
                    selection.second

                if (
                    startDate != null &&
                    endDate != null
                ) {

                    lifecycleScope.launch {

                        val days =
                            mutableListOf<AvailableDay>()

                        val formatter =
                            SimpleDateFormat(
                                "yyyy-MM-dd",
                                Locale.getDefault()
                            ).apply {

                                timeZone =
                                    TimeZone.getTimeZone("UTC")
                            }

                        val calendar =
                            Calendar.getInstance(
                                TimeZone.getTimeZone("UTC")
                            ).apply {

                                timeInMillis =
                                    startDate
                            }

                        val endCalendar =
                            Calendar.getInstance(
                                TimeZone.getTimeZone("UTC")
                            ).apply {

                                timeInMillis =
                                    endDate
                            }

                        while (
                            !calendar.after(
                                endCalendar
                            )
                        ) {

                            val date =
                                formatter.format(
                                    calendar.time
                                )

                            days.add(
                                AvailableDay(
                                    id = UUID.randomUUID().toString(),
                                    date = date,
                                    availableSlots = 0
                                )
                            )

                            calendar.add(
                                Calendar.DAY_OF_MONTH,
                                1
                            )
                        }

                        val updatedDays =
                            updateAvailableSlots(days)

                        daysAdapter.submitList(
                            updatedDays
                        )

                        saveAvailableDays(
                            updatedDays
                        )

                        selectedDate =
                            formatter.parse(
                                updatedDays.first().date
                            ) ?: Date()

                        updateDateRange()

                        loadAppointments()

                        toast(
                            "Agenda atualizada"
                        )
                    }
                }
            }
    }

    private fun formatDate(
        date: Long
    ): String {

        return SimpleDateFormat(
            "dd/MM/yyyy",
            Locale("pt", "BR")
        ).format(Date(date))
    }

    private fun toast(
        message: String
    ) {

        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}