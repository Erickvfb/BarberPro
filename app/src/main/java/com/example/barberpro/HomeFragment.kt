package com.example.barberpro

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.barberpro.adapter.AgendaAdapter
import com.example.barberpro.model.*
import com.example.barberpro.repository.AppointmentRepository
import com.example.barberpro.ui.schedule.NewScheduleFragment
import com.google.android.material.card.MaterialCardView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var weekDaysContainer: LinearLayout
    private lateinit var dateRangeText: TextView
    private lateinit var servicesCountText: TextView
    private lateinit var productsCountText: TextView
    private lateinit var totalRevenueText: TextView
    private lateinit var appointmentsRecyclerView: RecyclerView
    private lateinit var fabAddAppointment: FloatingActionButton

    private lateinit var agendaAdapter: AgendaAdapter

    private var selectedDate: Date = Date()

    private val appointmentRepository = AppointmentRepository.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupRecyclerView()
        setupCalendar()
        setupClickListeners()
        listenConfirmResult()

        loadAppointments()
    }

    private fun initializeViews(view: View) {
        weekDaysContainer = view.findViewById(R.id.weekDaysContainer)
        dateRangeText = view.findViewById(R.id.dateRangeText)
        servicesCountText = view.findViewById(R.id.servicesCountText)
        productsCountText = view.findViewById(R.id.productsCountText)
        totalRevenueText = view.findViewById(R.id.totalRevenueText)
        appointmentsRecyclerView = view.findViewById(R.id.appointmentsRecyclerView)
        fabAddAppointment = view.findViewById(R.id.fabAddAppointment)
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
                        ConfirmAttendanceFragment.newInstance(appointment.id)
                    )
                    .addToBackStack(null)
                    .commit()
            }
        )

        appointmentsRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        appointmentsRecyclerView.adapter = agendaAdapter
    }

    private fun loadAppointments() {
        lifecycleScope.launch {

            val result = appointmentRepository.getAppointmentsByDate(selectedDate)

            result.onSuccess { appointments ->

                agendaAdapter.submitList(appointments)

                servicesCountText.text =
                    "${appointments.size} serviços"

                productsCountText.text =
                    "${appointments.sumOf { it.products.size }} produtos"

                val total =
                    appointments.sumOf { it.getTotalRevenue() }

                totalRevenueText.text =
                    NumberFormat.getCurrencyInstance(
                        Locale("pt", "BR")
                    ).format(total)
            }

            result.onFailure {
                toast(it.message ?: "Erro ao carregar agenda")
            }
        }
    }

    private fun deleteAppointment(appointment: Appointment) {

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cancelar Agendamento")
            .setMessage(
                "Deseja realmente cancelar o agendamento de ${appointment.client.name}?"
            )
            .setPositiveButton("Sim") { _, _ ->

                lifecycleScope.launch {

                    val result =
                        appointmentRepository.deleteAppointment(appointment.id)

                    result.onSuccess {
                        toast("Agendamento cancelado")
                        loadAppointments()
                    }

                    result.onFailure {
                        toast(it.message ?: "Erro ao cancelar")
                    }
                }
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun setupCalendar() {

        weekDaysContainer.removeAllViews()

        getWeekDays().forEach { day ->

            val view =
                layoutInflater.inflate(R.layout.item_calendar_day, null)

            val card =
                view.findViewById<MaterialCardView>(R.id.dayCard)

            val weekText =
                view.findViewById<TextView>(R.id.dayOfWeekText)

            val dayText =
                view.findViewById<TextView>(R.id.dayOfMonthText)

            weekText.text = day.dayOfWeek
            dayText.text = day.dayOfMonth.toString()

            card.setCardBackgroundColor(
                if (day.isSelected)
                    Color.parseColor("#D4AF37")
                else
                    Color.parseColor("#2A2A2A")
            )

            card.setOnClickListener {
                selectedDate = day.date
                setupCalendar()
                loadAppointments()
            }

            weekDaysContainer.addView(view)
        }

        updateDateRange()
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

        view?.findViewById<View>(R.id.calendarButton)
            ?.setOnClickListener {
                openDateRangePicker()
            }

        view?.findViewById<View>(R.id.defineAgendaButton)
            ?.setOnClickListener {

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

        parentFragmentManager.setFragmentResultListener(
            "confirm_attendance_result",
            viewLifecycleOwner
        ) { _, _ ->

            loadAppointments()
        }
    }

    private fun getWeekDays(): List<CalendarDay> {

        val list = mutableListOf<CalendarDay>()

        val cal = Calendar.getInstance().apply {
            time = selectedDate
        }

        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val formatter =
            SimpleDateFormat("EEE", Locale("pt", "BR"))

        repeat(7) {

            val date = cal.time

            list.add(
                CalendarDay(
                    date,
                    formatter.format(date)
                        .uppercase()
                        .substring(0, 3),
                    cal.get(Calendar.DAY_OF_MONTH),
                    isSameDay(date, selectedDate),
                    false
                )
            )

            cal.add(Calendar.DAY_OF_MONTH, 1)
        }

        return list
    }

    private fun updateDateRange() {

        val cal = Calendar.getInstance().apply {
            time = selectedDate
        }

        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val start = cal.time

        cal.add(Calendar.DAY_OF_MONTH, 6)

        val end = cal.time

        val format =
            SimpleDateFormat("dd MMM yyyy", Locale("pt", "BR"))

        dateRangeText.text =
            "${format.format(start)} - ${format.format(end)}"
    }

    private fun isSameDay(d1: Date, d2: Date): Boolean {

        val c1 = Calendar.getInstance().apply {
            time = d1
        }

        val c2 = Calendar.getInstance().apply {
            time = d2
        }

        return c1.get(Calendar.YEAR) ==
                c2.get(Calendar.YEAR)
                &&
                c1.get(Calendar.DAY_OF_YEAR) ==
                c2.get(Calendar.DAY_OF_YEAR)
    }

    private fun openDateRangePicker() {

        val datePicker =
            MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Selecione o período disponível")
                .setTheme(R.style.ThemeOverlay_BarberPro_MaterialCalendar)
                .build()

        datePicker.show(
            parentFragmentManager,
            "DATE_RANGE_PICKER"
        )

        datePicker.addOnPositiveButtonClickListener { selection ->

            val startDate = selection.first
            val endDate = selection.second

            if (startDate != null && endDate != null) {

                toast(
                    "Disponível de ${formatDate(startDate)} até ${
                        formatDate(
                            endDate
                        )
                    }"
                )
            }
        }
    }

    private fun formatDate(date: Long): String {

        return SimpleDateFormat(
            "dd/MM/yyyy",
            Locale("pt", "BR")
        ).format(Date(date))
    }

    private fun toast(message: String) {
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}