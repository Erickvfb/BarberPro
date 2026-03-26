package com.example.barberpro.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.barberpro.R
import com.example.barberpro.model.*
import com.example.barberpro.repository.AppointmentRepository
import com.example.barberpro.repository.ClientsRepository
import com.example.barberpro.util.TimeSlotAvailabilityChecker
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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
    private val horarioSelecionado: Calendar = Calendar.getInstance()

    private val clientsRepository = ClientsRepository.getInstance()
    private val scheduleConfig: BarberScheduleConfig
        get() = ScheduleConfigManager.getScheduleConfig()

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

        nomeclienteCard.setOnClickListener { selecionarCliente() }
        servicoCard.setOnClickListener { selecionarServico() }
        dataCard.setOnClickListener { selecionarData() }
        horarioCard.setOnClickListener { selecionarHorario() }
        agendarButton.setOnClickListener { realizarAgendamento() }
    }

    private fun selecionarCliente() {
        lifecycleScope.launch {
            val result = clientsRepository.getAllClients()

            result.onSuccess { clientes ->
                if (clientes.isEmpty()) {
                    toast("Nenhum cliente cadastrado")
                    return@onSuccess
                }

                val nomes = clientes.map { it.name }.toTypedArray()

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Selecionar Cliente")
                    .setItems(nomes) { _, which ->
                        clienteSelecionado = clientes[which]
                        clienteSelecionadoText.text = clientes[which].name
                    }
                    .show()
            }
        }
    }

    private fun selecionarServico() {
        val servicos = listOf(
            Service("1", "Corte Social", 45.0, durationMinutes = 30),
            Service("2", "Barba Completa", 25.0, durationMinutes = 20),
            Service("3", "Corte + Barba", 65.0, durationMinutes = 50),
            Service("4", "Platinado", 60.0, durationMinutes = 60),
            Service("5", "Química", 80.0, durationMinutes = 90)
        )

        val nomes = servicos.map {
            "${it.name} - R$ ${String.format("%.2f", it.price)}"
        }.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Selecionar Serviço")
            .setItems(nomes) { _, which ->
                servicoSelecionado = servicos[which]
                servicoSelecionadoText.text = servicos[which].name
                precoText.text = "R$ ${String.format("%.2f", servicos[which].price)}"
            }
            .show()
    }

    private fun selecionarData() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Selecionar data")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setTheme(R.style.ThemeOverlay_BarberPro_MaterialCalendar)
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = selection
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            dataSelecionada = calendar

            val format = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
            dataText.text = format.format(calendar.time)

            // ✅ Limpar horário selecionado quando troca de data
            horarioText.text = "Selecionar horário"

            // ✅ Verificar se tem horários disponíveis
            val availableSlots = TimeSlotAvailabilityChecker
                .getAvailableSlotsCount(calendar, scheduleConfig)

            if (availableSlots == 0) {
                toast("⚠️ Nenhum horário disponível nesta data")
            } else {
                toast("✅ $availableSlots horários disponíveis")
            }
        }

        picker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun selecionarHorario() {
        val data = dataSelecionada ?: run {
            toast("Selecione uma data primeiro")
            return
        }

        // ✅ Buscar apenas horários disponíveis
        val horariosDisponiveis = TimeSlotAvailabilityChecker
            .getAvailableTimeSlots(data, scheduleConfig)

        if (horariosDisponiveis.isEmpty()) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Sem Horários Disponíveis")
                .setMessage(buildString {
                    append("Não há horários disponíveis para esta data.\n\n")
                    append("Motivos possíveis:\n")
                    append("• Todos os horários já estão ocupados\n")
                    append("• Data fora do horário de funcionamento\n")
                    append("• Horário de almoço\n\n")
                    append("Selecione outra data.")
                })
                .setPositiveButton("OK", null)
                .show()
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Selecionar Horário (${horariosDisponiveis.size} disponíveis)")
            .setItems(horariosDisponiveis.toTypedArray()) { _, which ->
                val horario = horariosDisponiveis[which]
                val parts = horario.split(":")
                horarioSelecionado.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                horarioSelecionado.set(Calendar.MINUTE, parts[1].toInt())
                horarioText.text = horario
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun realizarAgendamento() {
        val cliente = clienteSelecionado ?: run {
            toast("Selecione um cliente")
            return
        }

        val servico = servicoSelecionado ?: run {
            toast("Selecione um serviço")
            return
        }

        val data = dataSelecionada ?: run {
            toast("Selecione uma data")
            return
        }

        if (horarioText.text.isNullOrBlank() || horarioText.text == "Selecionar horário") {
            toast("Selecione um horário")
            return
        }

        val startCalendar = Calendar.getInstance().apply {
            time = data.time
            set(Calendar.HOUR_OF_DAY, horarioSelecionado.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, horarioSelecionado.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // ✅ Verificação final de disponibilidade
        val parts = horarioText.text.toString().split(":")
        val isAvailable = TimeSlotAvailabilityChecker.getAvailableTimeSlots(data, scheduleConfig)
            .contains("${parts[0]}:${parts[1]}")

        if (!isAvailable) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Horário Indisponível")
                .setMessage("Este horário foi ocupado recentemente. Por favor, selecione outro horário.")
                .setPositiveButton("OK") { _, _ ->
                    horarioText.text = "Selecionar horário"
                }
                .show()
            return
        }

        val appointment = Appointment(
            id = UUID.randomUUID().toString(),
            client = cliente,
            service = servico,
            startTime = startCalendar.time,
            status = AppointmentStatus.SCHEDULED
        )

        lifecycleScope.launch {
            val result = AppointmentRepository
                .getInstance()
                .createAppointment(appointment)

            if (result.isSuccess) {
                toast("✅ Agendamento realizado com sucesso!")
                parentFragmentManager.popBackStack()
            } else {
                toast("❌ ${result.exceptionOrNull()?.message ?: "Erro ao agendar"}")
            }
        }
    }

    private fun toast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}