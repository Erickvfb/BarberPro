package com.example.barberpro

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.barberpro.adapter.AgendaAdapter
import com.example.barberpro.model.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    // Views
    private lateinit var weekDaysContainer: LinearLayout
    private lateinit var dateRangeText: TextView
    private lateinit var servicesCountText: TextView
    private lateinit var productsCountText: TextView
    private lateinit var totalRevenueText: TextView
    private lateinit var appointmentsRecyclerView: RecyclerView
    private lateinit var fabAddAppointment: FloatingActionButton
    private lateinit var bottomNavigation: BottomNavigationView

    // Data
    private lateinit var agendaAdapter: AgendaAdapter
    private var selectedDate: Date = Date()
    private val appointments = mutableListOf<Appointment>()
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupRecyclerView()
        setupCalendar()
        setupClickListeners()
        loadMockData()
        updateUI()
    }

    private fun initializeViews() {
        weekDaysContainer = findViewById(R.id.weekDaysContainer)
        dateRangeText = findViewById(R.id.dateRangeText)
        servicesCountText = findViewById(R.id.servicesCountText)
        productsCountText = findViewById(R.id.productsCountText)
        totalRevenueText = findViewById(R.id.totalRevenueText)
        appointmentsRecyclerView = findViewById(R.id.appointmentsRecyclerView)
        fabAddAppointment = findViewById(R.id.fabAddAppointment)
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }

    private fun setupRecyclerView() {
        agendaAdapter = AgendaAdapter(
            onAppointmentClick = { appointment ->
                showAppointmentDetails(appointment)
            },
            onAppointmentDelete = { appointment ->
                deleteAppointment(appointment)
            },
            onAddTimeSlot = { time ->
                addNewAppointment(time)
            }
        )

        appointmentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = agendaAdapter
        }
    }

    private fun setupCalendar() {
        val weekDays = getWeekDays()
        weekDaysContainer.removeAllViews()

        weekDays.forEach { day ->
            val dayView = createDayView(day)
            weekDaysContainer.addView(dayView)
        }

        updateDateRange()
    }

    private fun createDayView(calendarDay: CalendarDay): View {
        val dayView = layoutInflater.inflate(R.layout.item_calendar_day, null)

        val dayOfWeekText = dayView.findViewById<TextView>(R.id.dayOfWeekText)
        val dayOfMonthText = dayView.findViewById<TextView>(R.id.dayOfMonthText)
        val dayCard = dayView.findViewById<MaterialCardView>(R.id.dayCard)

        dayOfWeekText.text = calendarDay.dayOfWeek
        dayOfMonthText.text = calendarDay.dayOfMonth.toString()

        // Styling
        if (calendarDay.isSelected) {
            dayCard.setCardBackgroundColor(Color.parseColor("#D4AF37")) // Gold
            dayOfWeekText.setTextColor(Color.parseColor("#1A1A1A"))
            dayOfMonthText.setTextColor(Color.parseColor("#1A1A1A"))
        } else {
            dayCard.setCardBackgroundColor(Color.parseColor("#2A2A2A"))
            dayOfWeekText.setTextColor(Color.parseColor("#999999"))
            dayOfMonthText.setTextColor(Color.WHITE)
        }

        dayCard.setOnClickListener {
            selectedDate = calendarDay.date
            setupCalendar()
            updateUI()
        }

        val params = LinearLayout.LayoutParams(
            dpToPx(60),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.marginEnd = dpToPx(8)
        dayView.layoutParams = params

        return dayView
    }

    private fun getWeekDays(): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()
        val cal = Calendar.getInstance()
        cal.time = selectedDate

        // Get start of week (Monday)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val today = Calendar.getInstance()

        for (i in 0..6) {
            val date = cal.time
            val dayOfWeek = dateFormat.format(date).uppercase().substring(0, 3)
            val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)

            val isSelected = isSameDay(date, selectedDate)
            val isToday = isSameDay(date, today.time)

            days.add(CalendarDay(date, dayOfWeek, dayOfMonth, isSelected, isToday))

            cal.add(Calendar.DAY_OF_MONTH, 1)
        }

        return days
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun updateDateRange() {
        val cal = Calendar.getInstance()
        cal.time = selectedDate
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val startDate = cal.time

        cal.add(Calendar.DAY_OF_MONTH, 6)
        val endDate = cal.time

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        dateRangeText.text = "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"
    }

    private fun updateUI() {
        // Filter appointments for selected date
        val dayAppointments = appointments.filter { isSameDay(it.startTime, selectedDate) }

        // Update summary
        val productsCount = dayAppointments.sumOf { it.products.size }
        val totalRevenue = dayAppointments.sumOf { it.getTotalRevenue() }

        servicesCountText.text = "${dayAppointments.size} serviços"
        productsCountText.text = "$productsCount produtos"

        val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        totalRevenueText.text = "Total: ${formatter.format(totalRevenue)}"

        // Update appointments list
        agendaAdapter.submitList(dayAppointments.sortedBy { it.startTime })
    }

    private fun setupClickListeners() {
        fabAddAppointment.setOnClickListener {
            addNewAppointment()
        }

        findViewById<View>(R.id.calendarButton).setOnClickListener {
            Toast.makeText(this, "Abrir calendário completo", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.defineAgendaButton).setOnClickListener {
            Toast.makeText(this, "Definições de agenda", Toast.LENGTH_SHORT).show()
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_stats -> {
                    Toast.makeText(this, "Estatísticas", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_agenda -> {
                    // Already on agenda
                    true
                }
                R.id.nav_clients -> {
                    Toast.makeText(this, "Clientes", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_profile -> {
                    Toast.makeText(this, "Perfil", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        // Set agenda as selected
        bottomNavigation.selectedItemId = R.id.nav_agenda
    }

    private fun showAppointmentDetails(appointment: Appointment) {
        val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        val message = """
            Cliente: ${appointment.client.name}
            Serviço: ${appointment.service.name}
            Horário: ${appointment.getTimeRange()}
            Duração: ${appointment.service.durationMinutes} min
            Valor: ${formatter.format(appointment.service.price)}
            Status: ${appointment.status.displayName}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Detalhes do Agendamento")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .setNeutralButton("Editar") { _, _ ->
                Toast.makeText(this, "Editar agendamento", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun deleteAppointment(appointment: Appointment) {
        AlertDialog.Builder(this)
            .setTitle("Cancelar Agendamento")
            .setMessage("Deseja realmente cancelar o agendamento de ${appointment.client.name}?")
            .setPositiveButton("Sim") { _, _ ->
                appointments.remove(appointment)
                updateUI()
                Toast.makeText(this, "Agendamento cancelado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun addNewAppointment(time: String? = null) {
        Toast.makeText(this, "Adicionar novo agendamento${time?.let { " às $it" } ?: ""}", Toast.LENGTH_SHORT).show()
        // TODO: Open add appointment dialog/activity
    }

    private fun loadMockData() {
        // Mock clients
        val client1 = Client("1", "Carlos", "11999999999")
        val client2 = Client("2", "Igor", "11988888888")
        val client3 = Client("3", "Diego", "11977777777")

        // Mock services
        val barba = Service("1", "Barba", 20, 20.0)
        val corteSocial = Service("2", "Corte social", 20, 45.80)
        val corteOutros = Service("3", "Corte social e outros", 40, 0.0)

        // Create appointments for today
        val cal = Calendar.getInstance()
        cal.time = selectedDate

        // 09:00 - Barba
        cal.set(Calendar.HOUR_OF_DAY, 9)
        cal.set(Calendar.MINUTE, 0)
        val start1 = cal.time
        cal.add(Calendar.MINUTE, 20)
        appointments.add(Appointment("1", client1, barba, start1, cal.time, AppointmentStatus.PENDING))

        // 09:30 - Corte social
        cal.set(Calendar.HOUR_OF_DAY, 9)
        cal.set(Calendar.MINUTE, 30)
        val start2 = cal.time
        cal.add(Calendar.MINUTE, 20)
        appointments.add(Appointment("2", client2, corteSocial, start2, cal.time, AppointmentStatus.CONFIRMED))

        // 10:30 - Corte social e outros
        cal.set(Calendar.HOUR_OF_DAY, 10)
        cal.set(Calendar.MINUTE, 30)
        val start3 = cal.time
        cal.add(Calendar.MINUTE, 40)
        appointments.add(Appointment("3", client3, corteOutros, start3, cal.time, AppointmentStatus.IN_PROGRESS))
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}