package com.example.barberpro

import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.util.*

class SettingsCalendarFragment : Fragment() {

    private lateinit var backButton: ImageView
    private lateinit var almocoSwitch: SwitchCompat
    private lateinit var almocoInicioCard: MaterialCardView
    private lateinit var almocoInicioText: TextView
    private lateinit var almocoFimCard: MaterialCardView
    private lateinit var almocoFimText: TextView

    // Cards dos dias
    private lateinit var segundaCard: MaterialCardView
    private lateinit var tercaCard: MaterialCardView
    private lateinit var quartaCard: MaterialCardView
    private lateinit var quintaCard: MaterialCardView
    private lateinit var sextaCard: MaterialCardView
    private lateinit var sabadoCard: MaterialCardView
    private lateinit var domingoCard: MaterialCardView

    // TextViews dos horários
    private lateinit var segundaHorarioText: TextView
    private lateinit var tercaHorarioText: TextView
    private lateinit var quartaHorarioText: TextView
    private lateinit var quintaHorarioText: TextView
    private lateinit var sextaHorarioText: TextView
    private lateinit var sabadoHorarioText: TextView
    private lateinit var domingoHorarioText: TextView

    private val horarios = mutableMapOf(
        "Segunda-feira" to Pair("08:00", "19:00"),
        "Terça-feira" to Pair("08:00", "19:00"),
        "Quarta-feira" to Pair("08:00", "19:00"),
        "Quinta-feira" to Pair("08:00", "19:00"),
        "Sexta-feira" to Pair("08:00", "19:00"),
        "Sábado" to Pair("08:00", "18:00"),
        "Domingo" to Pair("Fechado", "Fechado")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initializeViews(view)
        setupClickListeners()
    }

    private fun initializeViews(view: View) {
        backButton = view.findViewById(R.id.backButton)
        almocoSwitch = view.findViewById(R.id.almocoSwitch)
        almocoInicioCard = view.findViewById(R.id.almocoInicioCard)
        almocoInicioText = view.findViewById(R.id.almocoInicioText)
        almocoFimCard = view.findViewById(R.id.almocoFimCard)
        almocoFimText = view.findViewById(R.id.almocoFimText)

        segundaCard = view.findViewById(R.id.segundaCard)
        tercaCard = view.findViewById(R.id.tercaCard)
        quartaCard = view.findViewById(R.id.quartaCard)
        quintaCard = view.findViewById(R.id.quintaCard)
        sextaCard = view.findViewById(R.id.sextaCard)
        sabadoCard = view.findViewById(R.id.sabadoCard)
        domingoCard = view.findViewById(R.id.domingoCard)

        segundaHorarioText = view.findViewById(R.id.segundaHorarioText)
        tercaHorarioText = view.findViewById(R.id.tercaHorarioText)
        quartaHorarioText = view.findViewById(R.id.quartaHorarioText)
        quintaHorarioText = view.findViewById(R.id.quintaHorarioText)
        sextaHorarioText = view.findViewById(R.id.SextaHorarioText)
        sabadoHorarioText = view.findViewById(R.id.sabadoHorarioText)
        domingoHorarioText = view.findViewById(R.id.domingoHorarioText)
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        almocoInicioCard.setOnClickListener {
            selecionarHorario { hora, minuto ->
                almocoInicioText.text = String.format("%02d:%02d", hora, minuto)
            }
        }

        almocoFimCard.setOnClickListener {
            selecionarHorario { hora, minuto ->
                almocoFimText.text = String.format("%02d:%02d", hora, minuto)
            }
        }

        segundaCard.setOnClickListener { mostrarDialogHorario("Segunda-feira", segundaHorarioText) }
        tercaCard.setOnClickListener { mostrarDialogHorario("Terça-feira", tercaHorarioText) }
        quartaCard.setOnClickListener { mostrarDialogHorario("Quarta-feira", quartaHorarioText) }
        quintaCard.setOnClickListener { mostrarDialogHorario("Quinta-feira", quintaHorarioText) }
        sextaCard.setOnClickListener { mostrarDialogHorario("Sexta-feira", sextaHorarioText) }
        sabadoCard.setOnClickListener { mostrarDialogHorario("Sábado", sabadoHorarioText) }
        domingoCard.setOnClickListener { mostrarDialogHorario("Domingo", domingoHorarioText) }
    }

    private fun mostrarDialogHorario(dia: String, textView: TextView) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_horario_funcionamento, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val fechadoOption = dialogView.findViewById<MaterialCardView>(R.id.fechadoOption)
        val fechadoCheckIcon = dialogView.findViewById<ImageView>(R.id.fechadoCheckIcon)
        val inicioTimeText = dialogView.findViewById<TextView>(R.id.inicioTimeText)
        val fimTimeText = dialogView.findViewById<TextView>(R.id.fimTimeText)
        val salvarButton = dialogView.findViewById<MaterialButton>(R.id.salvarButton)

        var isFechado = horarios[dia]?.first == "Fechado"

        fechadoCheckIcon.visibility = if (isFechado) View.VISIBLE else View.GONE

        fechadoOption.setOnClickListener {
            isFechado = !isFechado
            fechadoCheckIcon.visibility = if (isFechado) View.VISIBLE else View.GONE
        }

        salvarButton.setOnClickListener {
            if (isFechado) {
                horarios[dia] = Pair("Fechado", "Fechado")
                textView.text = "Fechado"
                textView.setTextColor(Color.RED)
            } else {
                val inicio = inicioTimeText.text.toString()
                val fim = fimTimeText.text.toString()
                horarios[dia] = Pair(inicio, fim)
                textView.text = "$inicio - $fim"
                textView.setTextColor(Color.GRAY)
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun selecionarHorario(callback: (Int, Int) -> Unit) {
        val cal = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, h, m -> callback(h, m) },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }
}