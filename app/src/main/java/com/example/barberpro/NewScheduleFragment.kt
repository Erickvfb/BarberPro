package com.example.barberpro.ui.schedule

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.barberpro.R
import com.example.barberpro.adapter.ServiceSelectionAdapter
import com.example.barberpro.model.Appointment
import com.example.barberpro.model.AppointmentStatus
import com.example.barberpro.model.BarberConfig
import com.example.barberpro.model.Client
import com.example.barberpro.model.Service
import com.example.barberpro.model.TimeSlotGenerator
import com.example.barberpro.repository.AppointmentRepository
import com.example.barberpro.repository.ClientsRepository
import com.example.barberpro.repository.ServicesRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.text.Editable
import android.text.TextWatcher
import com.example.barberpro.adapter.ClientesAdapter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class NewScheduleFragment : Fragment() {

    private lateinit var backButton: ImageView
    private lateinit var nomeclienteCard: MaterialCardView
    private lateinit var clienteSelecionadoText: TextView
    private lateinit var servicoCard: MaterialCardView
    private lateinit var servicoSelecionadoText: TextView
    private lateinit var dataCard: MaterialCardView
    private lateinit var dataText: TextView
    private lateinit var horarioCard: MaterialCardView
    private lateinit var horarioText: TextView
    private lateinit var precoText: TextView
    private lateinit var agendarButton: MaterialButton

    private var clienteSelecionado: Client? = null
    private var servicoSelecionado: Service? = null
    private var dataSelecionada: Calendar? = null
    private val horarioSelecionado = Calendar.getInstance()

    private val clientsRepository = ClientsRepository.getInstance()
    private val servicesRepository = ServicesRepository.getInstance()
    private val appointmentRepository = AppointmentRepository.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_new_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews(view)
        setupClickListeners()
    }

    private fun initializeViews(view: View) {
        backButton = view.findViewById(R.id.backButton)
        nomeclienteCard = view.findViewById(R.id.nomeclienteCard)
        clienteSelecionadoText = view.findViewById(R.id.clienteSelecionadoText)
        servicoCard = view.findViewById(R.id.servicoCard)
        servicoSelecionadoText = view.findViewById(R.id.servicoSelecionadoText)
        dataCard = view.findViewById(R.id.dataCard)
        dataText = view.findViewById(R.id.dataText)
        horarioCard = view.findViewById(R.id.horarioCard)
        horarioText = view.findViewById(R.id.horarioText)
        precoText = view.findViewById(R.id.precoText)
        agendarButton = view.findViewById(R.id.agendarButton)
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        nomeclienteCard.setOnClickListener {
            selecionarCliente()
        }

        servicoCard.setOnClickListener {
            selecionarServico()
        }

        dataCard.setOnClickListener {
            selecionarData()
        }

        horarioCard.setOnClickListener {
            selecionarHorario()
        }

        agendarButton.setOnClickListener {
            realizarAgendamento()
        }
    }


    private fun selecionarCliente() {

        lifecycleScope.launch {

            Log.d("SCHEDULE", "Buscando clientes...")

            val result = clientsRepository.getAllClients()

            result.onSuccess { clientes ->

                Log.d("SCHEDULE", "${clientes.size} clientes carregados")

                if (clientes.isEmpty()) {

                    toast("Nenhum cliente cadastrado")
                    return@onSuccess
                }

                showClientSelectionDialog(clientes)
            }

            result.onFailure { error ->

                Log.e(
                    "SCHEDULE_ERROR",
                    "Erro ao carregar clientes: ${error.message}"
                )

                toast(
                    error.message
                        ?: "Erro ao carregar clientes"
                )
            }
        }
    }

    private fun showClientSelectionDialog(
        clients: List<Client>
    ) {

        val dialogView = LayoutInflater
            .from(requireContext())
            .inflate(
                R.layout.dialog_select_client,
                null
            )

        val searchInput =
            dialogView.findViewById<EditText>(
                R.id.clientSearchInput
            )

        val recyclerView =
            dialogView.findViewById<RecyclerView>(
                R.id.clientsRecyclerView
            )

        recyclerView.layoutManager =
            LinearLayoutManager(requireContext())


        val dialog = AlertDialog.Builder(
            requireContext()
        )
            .setView(dialogView)
            .create()

        val adapter = ClientesAdapter.ClientSelectionAdapter { client ->

            clienteSelecionado = client

            clienteSelecionadoText.text =
                client.name

            dialog.dismiss()
        }

        adapter.submitList(clients)


        recyclerView.adapter = adapter

        searchInput.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    adapter.filter(s.toString())
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {}
            }
        )

        dialog.show()
    }


    private fun selecionarServico() {

        lifecycleScope.launch {

            val result = servicesRepository.getAllServices()

            result.onSuccess { servicos ->

                if (servicos.isEmpty()) {
                    toast("Nenhum serviço cadastrado")
                    return@onSuccess
                }

                showServiceSelectionDialog(servicos)
            }

            result.onFailure {

                toast(
                    it.message
                        ?: "Erro ao carregar serviços"
                )
            }
        }
    }

    private fun showServiceSelectionDialog(
        services: List<Service>
    ) {

        val dialogView = LayoutInflater
            .from(requireContext())
            .inflate(
                R.layout.dialog_select_service,
                null
            )

        val searchInput = dialogView.findViewById<EditText>(
            R.id.serviceSearchInput
        )

        val recyclerView = dialogView.findViewById<RecyclerView>(
            R.id.servicesRecyclerView
        )

        recyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val adapter = ServiceSelectionAdapter { service ->

            servicoSelecionado = service

            servicoSelecionadoText.text =
                service.name

            precoText.text =
                "R$ %.2f".format(service.price)

            dialog.dismiss()
        }

        adapter.submitList(services)

        recyclerView.adapter = adapter

        searchInput.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    adapter.filter(s.toString())
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {
                }
            }
        )

        dialog.show()
    }

    private fun selecionarData() {
        val availableDays = getAvailableDays()

        if (availableDays.isEmpty()) {
            toast("Defina um período de trabalho primeiro")
            return
        }

        val constraints = CalendarConstraints.Builder()
            .setValidator(object : CalendarConstraints.DateValidator {
                override fun isValid(date: Long): Boolean {
                    val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                        timeInMillis = date
                    }

                    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }

                    val formattedDate = formatter.format(utcCalendar.time)
                    return availableDays.contains(formattedDate)
                }

                override fun describeContents(): Int = 0
                override fun writeToParcel(dest: Parcel, flags: Int) {}
            })
            .build()

        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Selecionar data")
            .setCalendarConstraints(constraints)
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = selection
            }

            val localCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR))
                set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val selectedDate = formatter.format(localCalendar.time)

            if (!availableDays.contains(selectedDate)) {
                toast("Data fora do período disponível")
                return@addOnPositiveButtonClickListener
            }

            dataSelecionada = localCalendar

            val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
            dataText.text = displayFormat.format(localCalendar.time)
            horarioText.text = "Selecionar horário"

            Log.d("SCHEDULE", "Data selecionada: ${dataText.text}")
        }

        picker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun selecionarHorario() {
        val data = dataSelecionada ?: run {
            toast("Selecione uma data primeiro")
            return
        }

        lifecycleScope.launch {
            Log.d("SCHEDULE", "Buscando horários disponíveis...")

            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val selectedDateString = formatter.format(data.time)

            val availableDays = getAvailableDays()

            if (!availableDays.contains(selectedDateString)) {
                toast("Data fora do período de trabalho")
                return@launch
            }

            val horariosOcupados =
                appointmentRepository
                    .getBookedSlotsForDay(
                        data.time
                    )

            val config = BarberConfig.getInstance()
            val dayOfWeek = data.get(Calendar.DAY_OF_WEEK)

            val schedule = config.workingDays[dayOfWeek]

            if (schedule == null || !schedule.enabled) {
                toast("Barbearia fechada neste dia")
                return@launch
            }

            // Gerar horários disponíveis
            val horarios =
                TimeSlotGenerator.generate(dayOfWeek)
                    .filterNot {
                        horariosOcupados.contains(it)
                    }

            if (horarios.isEmpty()) {
                toast("Nenhum horário disponível")
                return@launch
            }

            Log.d("SCHEDULE", " ${horarios.size} horários disponíveis")

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Selecionar Horário")
                .setItems(horarios.toTypedArray()) { _, which ->
                    val horario = horarios[which]
                    val parts = horario.split(":")

                    horarioSelecionado.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                    horarioSelecionado.set(Calendar.MINUTE, parts[1].toInt())
                    horarioSelecionado.set(Calendar.SECOND, 0)
                    horarioSelecionado.set(Calendar.MILLISECOND, 0)

                    horarioText.text = horario
                    Log.d("SCHEDULE", " Horário selecionado: $horario")
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun realizarAgendamento() {
        // Validar cliente
        val cliente = clienteSelecionado ?: run {
            toast("Selecione um cliente")
            return
        }

        // Validar serviço
        val servico = servicoSelecionado ?: run {
            toast("Selecione um serviço")
            return
        }

        // Validar data
        val data = dataSelecionada ?: run {
            toast("Selecione uma data")
            return
        }

        // Validar horário
        if (horarioText.text.toString() == "Selecionar horário") {
            toast("Selecione um horário")
            return
        }

        agendarButton.isEnabled = false

        val startCalendar = Calendar.getInstance().apply {
            time = data.time
            set(Calendar.HOUR_OF_DAY, horarioSelecionado.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, horarioSelecionado.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Validar se não é horário passado
        if (startCalendar.before(Calendar.getInstance())) {
            toast("Não é possível agendar horários passados")
            agendarButton.isEnabled = true
            return
        }

        val appointment = Appointment(
            id = "",
            client = cliente,
            service = servico,
            startTime = startCalendar.time,
            status = AppointmentStatus.SCHEDULED,
            notes = null
        )

        Log.d("SCHEDULE", " Criando agendamento:")
        Log.d("SCHEDULE", " Cliente: ${cliente.name}")
        Log.d("SCHEDULE", " Serviço: ${servico.name}")
        Log.d("SCHEDULE", " Data/Hora: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")).format(startCalendar.time)}")

        lifecycleScope.launch {
            val result = appointmentRepository.createAppointment(appointment)

            result.onSuccess {
                Log.d("SCHEDULE", "Agendamento criado com sucesso!")
                toast("Agendamento criado com sucesso")
                parentFragmentManager.popBackStack()
            }

            result.onFailure { error ->
                Log.e("SCHEDULE_ERROR", "Erro ao criar agendamento: ${error.message}")
                toast(error.message ?: "Erro ao criar agendamento")
            }

            agendarButton.isEnabled = true
        }
    }

    private fun getAvailableDays(): Set<String> {
        val prefs = requireContext().getSharedPreferences("agenda_prefs", Context.MODE_PRIVATE)

        val startDate = prefs.getString("start_date", null) ?: return emptySet()
        val endDate = prefs.getString("end_date", null) ?: return emptySet()

        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val start = formatter.parse(startDate) ?: return emptySet()
        val end = formatter.parse(endDate) ?: return emptySet()

        val config = BarberConfig.getInstance()
        val calendar = Calendar.getInstance()
        calendar.time = start

        val endCalendar = Calendar.getInstance()
        endCalendar.time = end

        val result = mutableSetOf<String>()

        while (!calendar.after(endCalendar)) {
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val schedule = config.workingDays[dayOfWeek]

            if (schedule != null && schedule.enabled) {
                result.add(formatter.format(calendar.time))
            }

            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return result
    }

    private fun toast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}