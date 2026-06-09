import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.example.barberpro.R
import com.example.barberpro.model.BarberConfig
import com.example.barberpro.model.DaySchedule
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

    private lateinit var segundaCard: MaterialCardView
    private lateinit var tercaCard: MaterialCardView
    private lateinit var quartaCard: MaterialCardView
    private lateinit var quintaCard: MaterialCardView
    private lateinit var sextaCard: MaterialCardView
    private lateinit var sabadoCard: MaterialCardView
    private lateinit var domingoCard: MaterialCardView

    private lateinit var segundaHorarioText: TextView
    private lateinit var tercaHorarioText: TextView
    private lateinit var quartaHorarioText: TextView
    private lateinit var quintaHorarioText: TextView
    private lateinit var sextaHorarioText: TextView
    private lateinit var sabadoHorarioText: TextView
    private lateinit var domingoHorarioText: TextView

    // Mapa local dos horários exibidos (atualizado junto com o BarberConfig)
    private val horarios = mutableMapOf(
        "Segunda-feira" to Pair("08:00", "19:00"),
        "Terça-feira"  to Pair("08:00", "19:00"),
        "Quarta-feira" to Pair("08:00", "19:00"),
        "Quinta-feira" to Pair("08:00", "19:00"),
        "Sexta-feira"  to Pair("08:00", "19:00"),
        "Sábado"       to Pair("08:00", "18:00"),
        "Domingo"      to Pair("Fechado", "Fechado")
    )

    // SharedPreferences para persistência
    private val PREFS = "barber_schedule_prefs"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews(view)
        carregarConfigSalva()   // ← carrega do SharedPreferences primeiro
        loadConfig()
        setupClickListeners()
    }

    private fun initializeViews(view: View) {
        backButton       = view.findViewById(R.id.backButton)
        almocoSwitch     = view.findViewById(R.id.almocoSwitch)
        almocoInicioCard = view.findViewById(R.id.almocoInicioCard)
        almocoInicioText = view.findViewById(R.id.almocoInicioText)
        almocoFimCard    = view.findViewById(R.id.almocoFimCard)
        almocoFimText    = view.findViewById(R.id.almocoFimText)

        segundaCard = view.findViewById(R.id.segundaCard)
        tercaCard   = view.findViewById(R.id.tercaCard)
        quartaCard  = view.findViewById(R.id.quartaCard)
        quintaCard  = view.findViewById(R.id.quintaCard)
        sextaCard   = view.findViewById(R.id.sextaCard)
        sabadoCard  = view.findViewById(R.id.sabadoCard)
        domingoCard = view.findViewById(R.id.domingoCard)

        segundaHorarioText = view.findViewById(R.id.segundaHorarioText)
        tercaHorarioText   = view.findViewById(R.id.tercaHorarioText)
        quartaHorarioText  = view.findViewById(R.id.quartaHorarioText)
        quintaHorarioText  = view.findViewById(R.id.quintaHorarioText)
        sextaHorarioText   = view.findViewById(R.id.sextaHorarioText)
        sabadoHorarioText  = view.findViewById(R.id.sabadoHorarioText)
        domingoHorarioText = view.findViewById(R.id.domingoHorarioText)
    }

    // ── Persistência ──────────────────────────────────────────────────────────

    /**
     * Carrega a configuração salva do SharedPreferences e restaura o BarberConfig.
     * Chamado antes de loadConfig() para garantir que os dados salvos sejam usados.
     */
    private fun carregarConfigSalva() {
        val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        val hasLunch       = prefs.getBoolean("lunch_enabled",      true)
        val lunchStartH    = prefs.getInt("lunch_start_hour",        12)
        val lunchStartM    = prefs.getInt("lunch_start_minute",      0)
        val lunchEndH      = prefs.getInt("lunch_end_hour",          13)
        val lunchEndM      = prefs.getInt("lunch_end_minute",        0)

        // Restaurar configuração de almoço
        val config = BarberConfig.getInstance()
        config.hasLunchBreak     = hasLunch
        config.lunchStartHour    = lunchStartH
        config.lunchStartMinute  = lunchStartM
        config.lunchEndHour      = lunchEndH
        config.lunchEndMinute    = lunchEndM

        // Restaurar cada dia da semana
        val dias = listOf(
            Calendar.MONDAY    to "segunda",
            Calendar.TUESDAY   to "terca",
            Calendar.WEDNESDAY to "quarta",
            Calendar.THURSDAY  to "quinta",
            Calendar.FRIDAY    to "sexta",
            Calendar.SATURDAY  to "sabado",
            Calendar.SUNDAY    to "domingo"
        )

        dias.forEach { (calDay, key) ->
            val enabled = prefs.getBoolean("${key}_enabled", calDay != Calendar.SUNDAY)
            if (enabled) {
                val startH = prefs.getInt("${key}_start_hour",   8)
                val startM = prefs.getInt("${key}_start_minute", 0)
                val endH   = prefs.getInt("${key}_end_hour",
                    if (calDay == Calendar.SATURDAY) 18 else 19)
                val endM   = prefs.getInt("${key}_end_minute", 0)

                config.workingDays[calDay] = DaySchedule(
                    enabled     = true,
                    startHour   = startH,
                    startMinute = startM,
                    endHour     = endH,
                    endMinute   = endM
                )
            } else {
                config.workingDays[calDay] = DaySchedule(enabled = false)
            }
        }

        BarberConfig.updateInstance(config)
    }

    /**
     * Persiste toda a configuração atual no SharedPreferences.
     */
    private fun salvarConfigNoPrefs() {
        val config = BarberConfig.getInstance()
        val prefs  = requireContext()
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()

        prefs.putBoolean("lunch_enabled",     config.hasLunchBreak)
        prefs.putInt("lunch_start_hour",      config.lunchStartHour)
        prefs.putInt("lunch_start_minute",    config.lunchStartMinute)
        prefs.putInt("lunch_end_hour",        config.lunchEndHour)
        prefs.putInt("lunch_end_minute",      config.lunchEndMinute)

        val diasMap = mapOf(
            Calendar.MONDAY    to "segunda",
            Calendar.TUESDAY   to "terca",
            Calendar.WEDNESDAY to "quarta",
            Calendar.THURSDAY  to "quinta",
            Calendar.FRIDAY    to "sexta",
            Calendar.SATURDAY  to "sabado",
            Calendar.SUNDAY    to "domingo"
        )

        diasMap.forEach { (calDay, key) ->
            val schedule = config.workingDays[calDay]
            prefs.putBoolean("${key}_enabled", schedule?.enabled == true)
            if (schedule != null && schedule.enabled) {
                prefs.putInt("${key}_start_hour",   schedule.startHour)
                prefs.putInt("${key}_start_minute", schedule.startMinute)
                prefs.putInt("${key}_end_hour",     schedule.endHour)
                prefs.putInt("${key}_end_minute",   schedule.endMinute)
            }
        }

        prefs.apply()
    }

    // ── UI ───────────────────────────────────────────────────────────────────

    private fun loadConfig() {
        val config = BarberConfig.getInstance()

        almocoSwitch.isChecked  = config.hasLunchBreak
        almocoInicioText.text   = String.format("%02d:%02d", config.lunchStartHour,  config.lunchStartMinute)
        almocoFimText.text      = String.format("%02d:%02d", config.lunchEndHour,    config.lunchEndMinute)

        atualizarHorariosNaTela(config)
        updateLunchCardsState(config.hasLunchBreak)
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener { parentFragmentManager.popBackStack() }

        almocoSwitch.setOnCheckedChangeListener { _, isChecked ->
            updateLunchCardsState(isChecked)
            salvarConfiguracao()
        }

        almocoInicioCard.setOnClickListener {
            val config = BarberConfig.getInstance()
            selecionarHorario(config.lunchStartHour, config.lunchStartMinute) { h, m ->
                almocoInicioText.text = String.format("%02d:%02d", h, m)
                salvarConfiguracao()
            }
        }

        almocoFimCard.setOnClickListener {
            val config = BarberConfig.getInstance()
            selecionarHorario(config.lunchEndHour, config.lunchEndMinute) { h, m ->
                almocoFimText.text = String.format("%02d:%02d", h, m)
                salvarConfiguracao()
            }
        }

        segundaCard.setOnClickListener { mostrarDialogHorario("Segunda-feira", segundaHorarioText) }
        tercaCard.setOnClickListener   { mostrarDialogHorario("Terça-feira",   tercaHorarioText)   }
        quartaCard.setOnClickListener  { mostrarDialogHorario("Quarta-feira",  quartaHorarioText)  }
        quintaCard.setOnClickListener  { mostrarDialogHorario("Quinta-feira",  quintaHorarioText)  }
        sextaCard.setOnClickListener   { mostrarDialogHorario("Sexta-feira",   sextaHorarioText)   }
        sabadoCard.setOnClickListener  { mostrarDialogHorario("Sábado",        sabadoHorarioText)  }
        domingoCard.setOnClickListener { mostrarDialogHorario("Domingo",       domingoHorarioText) }
    }

    private fun updateLunchCardsState(enabled: Boolean) {
        almocoInicioCard.isEnabled = enabled
        almocoFimCard.isEnabled    = enabled
        almocoInicioCard.alpha     = if (enabled) 1.0f else 0.5f
        almocoFimCard.alpha        = if (enabled) 1.0f else 0.5f
    }

    private fun salvarConfiguracao() {
        val inicioText = almocoInicioText.text.toString().split(":")
        val fimText    = almocoFimText.text.toString().split(":")

        val inicioH = inicioText[0].toInt()
        val inicioM = inicioText[1].toInt()
        val fimH    = fimText[0].toInt()
        val fimM    = fimText[1].toInt()

        if (fimH * 60 + fimM <= inicioH * 60 + inicioM) {
            Toast.makeText(
                requireContext(),
                "Horário final deve ser maior que o inicial!",
                Toast.LENGTH_LONG
            ).show()
            val config = BarberConfig.getInstance()
            almocoInicioText.text = String.format("%02d:%02d", config.lunchStartHour, config.lunchStartMinute)
            almocoFimText.text    = String.format("%02d:%02d", config.lunchEndHour,   config.lunchEndMinute)
            return
        }

        val config = BarberConfig(
            hasLunchBreak    = almocoSwitch.isChecked,
            lunchStartHour   = inicioH,
            lunchStartMinute = inicioM,
            lunchEndHour     = fimH,
            lunchEndMinute   = fimM
        )
        BarberConfig.updateInstance(config)
        salvarConfigNoPrefs()   // ← persiste

        Toast.makeText(requireContext(), "Configuração salva!", Toast.LENGTH_SHORT).show()
    }

    private fun mostrarDialogHorario(dia: String, textView: TextView) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_horario_funcionamento, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // ✅ FIX 1: Título dinâmico com o nome do dia
        val titleText   = dialogView.findViewById<TextView>(R.id.dialogTitleText)
        titleText.text  = dia

        val fechadoOption   = dialogView.findViewById<MaterialCardView>(R.id.fechadoOption)
        val fechadoCheckIcon = dialogView.findViewById<ImageView>(R.id.fechadoCheckIcon)
        val inicioTimeText  = dialogView.findViewById<TextView>(R.id.inicioTimeText)
        val fimTimeText     = dialogView.findViewById<TextView>(R.id.fimTimeText)
        val salvarButton    = dialogView.findViewById<MaterialButton>(R.id.salvarButton)

        // ✅ FIX 2: Inicializar com horários já configurados para o dia
        val calDay      = getCalendarDay(dia)
        val config      = BarberConfig.getInstance()
        val schedule    = config.workingDays[calDay]
        var isFechado   = schedule == null || !schedule.enabled

        if (!isFechado && schedule != null) {
            inicioTimeText.text = String.format("%02d:%02d", schedule.startHour, schedule.startMinute)
            fimTimeText.text    = String.format("%02d:%02d", schedule.endHour,   schedule.endMinute)
        }

        fechadoCheckIcon.visibility = if (isFechado) View.VISIBLE else View.GONE

        fechadoOption.setOnClickListener {
            isFechado = !isFechado
            fechadoCheckIcon.visibility = if (isFechado) View.VISIBLE else View.GONE
        }

        inicioTimeText.setOnClickListener {
            val parts = inicioTimeText.text.toString().split(":")
            selecionarHorario(parts[0].toInt(), parts[1].toInt()) { h, m ->
                inicioTimeText.text = String.format("%02d:%02d", h, m)
            }
        }

        fimTimeText.setOnClickListener {
            val parts = fimTimeText.text.toString().split(":")
            selecionarHorario(parts[0].toInt(), parts[1].toInt()) { h, m ->
                fimTimeText.text = String.format("%02d:%02d", h, m)
            }
        }

        salvarButton.setOnClickListener {
            val cfg = BarberConfig.getInstance()

            if (isFechado) {
                cfg.workingDays[calDay] = DaySchedule(enabled = false)
                BarberConfig.updateInstance(cfg)
                salvarConfigNoPrefs()   // ← persiste

                horarios[dia]  = Pair("Fechado", "Fechado")
                textView.text  = "Fechado"
                textView.setTextColor(Color.RED)

                dialog.dismiss()
                Toast.makeText(requireContext(), "$dia definido como fechado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val inicio = inicioTimeText.text.toString()
            val fim    = fimTimeText.text.toString()

            val inicioSplit = inicio.split(":")
            val fimSplit    = fim.split(":")
            val inicioMin   = inicioSplit[0].toInt() * 60 + inicioSplit[1].toInt()
            val fimMin      = fimSplit[0].toInt()    * 60 + fimSplit[1].toInt()

            if (fimMin <= inicioMin) {
                Toast.makeText(
                    requireContext(),
                    "O horário final deve ser maior que o inicial!",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            cfg.workingDays[calDay] = DaySchedule(
                enabled     = true,
                startHour   = inicioSplit[0].toInt(),
                startMinute = inicioSplit[1].toInt(),
                endHour     = fimSplit[0].toInt(),
                endMinute   = fimSplit[1].toInt()
            )
            BarberConfig.updateInstance(cfg)
            salvarConfigNoPrefs()   // ← persiste

            horarios[dia]  = Pair(inicio, fim)
            textView.text  = "$inicio - $fim"
            textView.setTextColor(Color.GRAY)

            dialog.dismiss()
            Toast.makeText(requireContext(), "$dia salvo: $inicio - $fim", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    private fun selecionarHorario(
        currentHour: Int,
        currentMinute: Int,
        callback: (Int, Int) -> Unit
    ) {
        TimePickerDialog(
            requireContext(),
            { _, h, m -> callback(h, m) },
            currentHour,
            currentMinute,
            true
        ).show()
    }

    private fun getCalendarDay(dia: String): Int = when (dia) {
        "Segunda-feira" -> Calendar.MONDAY
        "Terça-feira"   -> Calendar.TUESDAY
        "Quarta-feira"  -> Calendar.WEDNESDAY
        "Quinta-feira"  -> Calendar.THURSDAY
        "Sexta-feira"   -> Calendar.FRIDAY
        "Sábado"        -> Calendar.SATURDAY
        else            -> Calendar.SUNDAY
    }

    private fun atualizarHorariosNaTela(config: BarberConfig) {
        atualizarDia(config, Calendar.MONDAY,    segundaHorarioText)
        atualizarDia(config, Calendar.TUESDAY,   tercaHorarioText)
        atualizarDia(config, Calendar.WEDNESDAY, quartaHorarioText)
        atualizarDia(config, Calendar.THURSDAY,  quintaHorarioText)
        atualizarDia(config, Calendar.FRIDAY,    sextaHorarioText)
        atualizarDia(config, Calendar.SATURDAY,  sabadoHorarioText)
        atualizarDia(config, Calendar.SUNDAY,    domingoHorarioText)
    }

    private fun atualizarDia(config: BarberConfig, day: Int, textView: TextView) {
        val schedule = config.workingDays[day]
        if (schedule == null || !schedule.enabled) {
            textView.text = "Fechado"
            textView.setTextColor(Color.RED)
            return
        }
        textView.text = String.format(
            "%02d:%02d - %02d:%02d",
            schedule.startHour, schedule.startMinute,
            schedule.endHour,   schedule.endMinute
        )
        textView.setTextColor(Color.GRAY)
    }
}