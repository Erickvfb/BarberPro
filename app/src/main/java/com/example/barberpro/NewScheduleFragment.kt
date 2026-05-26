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
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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

    private val clientsRepository =
        ClientsRepository.getInstance()

    private val servicesRepository =
        ServicesRepository.getInstance()

    private val appointmentRepository =
        AppointmentRepository.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(
            R.layout.fragment_new_schedule,
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

        setupClickListeners()
    }

    private fun initializeViews(view: View) {

        backButton =
            view.findViewById(R.id.backButton)

        nomeclienteCard =
            view.findViewById(R.id.nomeclienteCard)

        clienteSelecionadoText =
            view.findViewById(R.id.clienteSelecionadoText)

        servicoCard =
            view.findViewById(R.id.servicoCard)

        servicoSelecionadoText =
            view.findViewById(R.id.servicoSelecionadoText)

        dataCard =
            view.findViewById(R.id.dataCard)

        dataText =
            view.findViewById(R.id.dataText)

        horarioCard =
            view.findViewById(R.id.horarioCard)

        horarioText =
            view.findViewById(R.id.horarioText)

        precoText =
            view.findViewById(R.id.precoText)

        agendarButton =
            view.findViewById(R.id.agendarButton)
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

            val result =
                clientsRepository.getAllClients()

            result.onSuccess { clientes ->

                val nomes =
                    clientes.map {
                        it.name
                    }.toTypedArray()

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Selecionar Cliente")
                    .setItems(nomes) { _, which ->

                        clienteSelecionado =
                            clientes[which]

                        clienteSelecionadoText.text =
                            clientes[which].name
                    }
                    .show()
            }

            result.onFailure {

                toast(
                    it.message
                        ?: "Erro ao carregar clientes"
                )
            }
        }
    }

    private fun selecionarServico() {

        lifecycleScope.launch {

            val result =
                servicesRepository.getAllServices()

            result.onSuccess { servicos ->

                val nomes =
                    servicos.map {

                        "${it.name} - R$ %.2f"
                            .format(it.price)

                    }.toTypedArray()

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Selecionar Serviço")
                    .setItems(nomes) { _, which ->

                        servicoSelecionado =
                            servicos[which]

                        servicoSelecionadoText.text =
                            servicos[which].name

                        precoText.text =
                            "R$ %.2f".format(
                                servicos[which].price
                            )
                    }
                    .show()
            }

            result.onFailure {

                toast(
                    it.message
                        ?: "Erro ao carregar serviços"
                )
            }
        }
    }

    private fun selecionarData() {

        val picker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecionar data")
                .setSelection(
                    MaterialDatePicker.todayInUtcMilliseconds()
                )
                .build()

        picker.addOnPositiveButtonClickListener { selection ->

            val utcCalendar =
                Calendar.getInstance(
                    java.util.TimeZone.getTimeZone("UTC")
                ).apply {

                    timeInMillis = selection
                }

            val localCalendar =
                Calendar.getInstance().apply {

                    set(
                        Calendar.YEAR,
                        utcCalendar.get(Calendar.YEAR)
                    )

                    set(
                        Calendar.MONTH,
                        utcCalendar.get(Calendar.MONTH)
                    )

                    set(
                        Calendar.DAY_OF_MONTH,
                        utcCalendar.get(Calendar.DAY_OF_MONTH)
                    )

                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

            dataSelecionada = localCalendar

            val format =
                SimpleDateFormat(
                    "dd/MM/yyyy",
                    Locale("pt", "BR")
                )

            dataText.text =
                format.format(localCalendar.time)

            horarioText.text =
                "Selecionar horário"
        }

        picker.show(
            parentFragmentManager,
            "DATE_PICKER"
        )
    }

    private fun selecionarHorario() {

        val data =
            dataSelecionada ?: run {

                toast("Selecione uma data primeiro")
                return
            }

        lifecycleScope.launch {

            val horariosOcupados =
                appointmentRepository
                    .getBookedSlotsForDay(
                        data.time
                    )

            val config =
                BarberConfig.getInstance()

            val horarios =
                TimeSlotGenerator.generate(config)
                    .filterNot {
                        horariosOcupados.contains(it)
                    }

            if (horarios.isEmpty()) {

                toast("Nenhum horário disponível")
                return@launch
            }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Selecionar Horário")
                .setItems(
                    horarios.toTypedArray()
                ) { _, which ->

                    val horario =
                        horarios[which]

                    val parts =
                        horario.split(":")

                    horarioSelecionado.set(
                        Calendar.HOUR_OF_DAY,
                        parts[0].toInt()
                    )

                    horarioSelecionado.set(
                        Calendar.MINUTE,
                        parts[1].toInt()
                    )

                    horarioSelecionado.set(
                        Calendar.SECOND,
                        0
                    )

                    horarioSelecionado.set(
                        Calendar.MILLISECOND,
                        0
                    )

                    horarioText.text =
                        horario
                }
                .show()
        }
    }

    private fun realizarAgendamento() {

        val cliente =
            clienteSelecionado ?: run {

                toast("Selecione um cliente")
                return
            }

        val servico =
            servicoSelecionado ?: run {

                toast("Selecione um serviço")
                return
            }

        val data =
            dataSelecionada ?: run {

                toast("Selecione uma data")
                return
            }

        if (horarioText.text.toString()
            == "Selecionar horário"
        ) {

            toast("Selecione um horário")
            return
        }

        agendarButton.isEnabled = false

        val startCalendar =
            Calendar.getInstance().apply {

                time = data.time

                set(
                    Calendar.HOUR_OF_DAY,
                    horarioSelecionado.get(
                        Calendar.HOUR_OF_DAY
                    )
                )

                set(
                    Calendar.MINUTE,
                    horarioSelecionado.get(
                        Calendar.MINUTE
                    )
                )

                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

        val appointment =
            Appointment(
                id = "",
                client = cliente,
                service = servico,
                startTime = startCalendar.time,
                status = AppointmentStatus.SCHEDULED,
                notes = null
            )

        lifecycleScope.launch {

            val result =
                appointmentRepository
                    .createAppointment(
                        appointment
                    )

            result.onSuccess {

                toast("Agendamento criado com sucesso")

                parentFragmentManager.popBackStack()
            }

            result.onFailure {

                toast(
                    it.message
                        ?: "Erro ao criar agendamento"
                )
            }

            agendarButton.isEnabled = true
        }
    }

    private fun toast(message: String) {

        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}