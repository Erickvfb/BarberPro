package com.example.barberpro

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.barberpro.adapter.ConsumptionAdapter
import com.example.barberpro.adapter.ServiceSelectionAdapter
import com.example.barberpro.model.*
import com.example.barberpro.model.com.example.barberpro.adapter.ProductSelectionAdapter
import com.example.barberpro.repository.ProductsRepository
import com.example.barberpro.repository.ServicesRepository
import com.example.barberpro.util.CurrencyUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class ConfirmAttendanceFragment : Fragment() {

    companion object {
        private const val ARG_APPOINTMENT_ID = "appointment_id"

        fun newInstance(appointmentId: String): ConfirmAttendanceFragment {
            return ConfirmAttendanceFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_APPOINTMENT_ID, appointmentId)
                }
            }
        }
    }

    // Header
    private lateinit var backButton: ImageView

    // Info do agendamento
    private lateinit var clientInitialText: TextView
    private lateinit var clientNameText: TextView
    private lateinit var scheduledTimeText: TextView
    private lateinit var serviceNameText: TextView
    private lateinit var servicePriceText: TextView
    private lateinit var editServiceButton: ImageView

    // Presença
    private lateinit var attendedCard: MaterialCardView
    private lateinit var noShowCard: MaterialCardView

    // Consumos
    private lateinit var consumptionSection: LinearLayout
    private lateinit var addConsumptionButton: MaterialCardView
    private lateinit var consumptionRecyclerView: RecyclerView

    // Total
    private lateinit var totalServiceNameText: TextView
    private lateinit var totalServicePriceText: TextView
    private lateinit var grandTotalText: TextView

    // Botões
    private lateinit var finalizarButton: MaterialButton
    private lateinit var confirmNoShowButton: MaterialButton

    // Adapter
    private lateinit var consumptionAdapter: ConsumptionAdapter

    // Dados
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    private var currentStatus = AttendanceStatus.PENDING
    private var servicePrice = 0.0
    private var serviceName = ""
    private var clientName = ""
    private var clientId = ""
    private var appointmentId = ""
    private var serviceId = ""

    // Repository
    private val productsRepository = ProductsRepository.getInstance()
    private val servicesRepository = ServicesRepository.getInstance()
    private var productSelectionDialog: AlertDialog? = null
    private var serviceSelectionDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Buscar appointment pelo ID
        appointmentId = requireArguments().getString(ARG_APPOINTMENT_ID)!!
        val appointment = AppointmentsManager.appointments.find { it.id == appointmentId }
            ?: throw IllegalStateException("Appointment not found: $appointmentId")

        // Carregar dados
        clientId = appointment.client.id
        clientName = appointment.client.name
        serviceName = appointment.service.name
        servicePrice = appointment.service.price
        serviceId = appointment.service.id
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_confirm_attendance, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        setupClicks()
        loadAppointmentData()
    }

    private fun initViews(view: View) {
        backButton = view.findViewById(R.id.backButton)
        clientInitialText = view.findViewById(R.id.clientInitialText)
        clientNameText = view.findViewById(R.id.clientNameText)
        scheduledTimeText = view.findViewById(R.id.scheduledTimeText)
        serviceNameText = view.findViewById(R.id.serviceNameText)
        servicePriceText = view.findViewById(R.id.servicePriceText)
        editServiceButton = view.findViewById(R.id.editServiceButton)
        attendedCard = view.findViewById(R.id.attendedCard)
        noShowCard = view.findViewById(R.id.noShowCard)
        consumptionSection = view.findViewById(R.id.consumptionSection)
        addConsumptionButton = view.findViewById(R.id.addConsumptionButton)
        consumptionRecyclerView = view.findViewById(R.id.consumptionRecyclerView)
        totalServiceNameText = view.findViewById(R.id.totalServiceNameText)
        totalServicePriceText = view.findViewById(R.id.totalServicePriceText)
        grandTotalText = view.findViewById(R.id.grandTotalText)
        finalizarButton = view.findViewById(R.id.finalizarButton)
        confirmNoShowButton = view.findViewById(R.id.confirmNoShowButton)
    }

    private fun setupRecyclerView() {
        consumptionAdapter = ConsumptionAdapter { item ->
            consumptionAdapter.removeItem(item)
            updateTotal()
        }

        consumptionRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = consumptionAdapter
        }
    }

    private fun setupClicks() {
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        editServiceButton.setOnClickListener {
            showServiceSelectionDialog()
        }

        attendedCard.setOnClickListener {
            selectAttendanceStatus(AttendanceStatus.ATTENDED)
        }

        noShowCard.setOnClickListener {
            selectAttendanceStatus(AttendanceStatus.NO_SHOW)
        }

        addConsumptionButton.setOnClickListener {
            showAddConsumptionDialog()
        }

        finalizarButton.setOnClickListener {
            finalizarAtendimento()
        }

        confirmNoShowButton.setOnClickListener {
            confirmarFalta()
        }
    }

    private fun loadAppointmentData() {
        val appointment = AppointmentsManager.appointments.find { it.id == appointmentId }

        clientInitialText.text = clientName.take(2).uppercase()
        clientNameText.text = clientName

        // Formatar horário
        val timeFormat = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())
        scheduledTimeText.text = appointment?.startTime?.let {
            timeFormat.format(it)
        } ?: "Horário agendado"

        serviceNameText.text = serviceName
        servicePriceText.text = currencyFormat.format(servicePrice)

        totalServiceNameText.text = serviceName
        totalServicePriceText.text = currencyFormat.format(servicePrice)
        grandTotalText.text = currencyFormat.format(servicePrice)
    }

    private fun selectAttendanceStatus(status: AttendanceStatus) {
        currentStatus = status

        when (status) {
            AttendanceStatus.ATTENDED -> {
                attendedCard.setCardBackgroundColor(android.graphics.Color.parseColor("#1A2E1A"))
                attendedCard.strokeColor = android.graphics.Color.parseColor("#22C55E")
                attendedCard.strokeWidth = 3

                noShowCard.setCardBackgroundColor(android.graphics.Color.parseColor("#1F1F1F"))
                noShowCard.strokeColor = android.graphics.Color.parseColor("#4B5563")
                noShowCard.strokeWidth = 1

                consumptionSection.visibility = View.VISIBLE
                confirmNoShowButton.visibility = View.GONE

                updateTotal()
            }

            AttendanceStatus.NO_SHOW -> {
                noShowCard.setCardBackgroundColor(android.graphics.Color.parseColor("#2E1A1A"))
                noShowCard.strokeColor = android.graphics.Color.parseColor("#EF4444")
                noShowCard.strokeWidth = 3

                attendedCard.setCardBackgroundColor(android.graphics.Color.parseColor("#1F1F1F"))
                attendedCard.strokeColor = android.graphics.Color.parseColor("#4B5563")
                attendedCard.strokeWidth = 1

                consumptionSection.visibility = View.GONE
                confirmNoShowButton.visibility = View.VISIBLE
            }

            else -> {}
        }
    }

    private fun showAddConsumptionDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_consumption, null)

        val productSelectorCard = dialogView.findViewById<MaterialCardView>(R.id.productSelectorCard)
        val selectedProductText = dialogView.findViewById<TextView>(R.id.selectedProductText)
        val quantityInput = dialogView.findViewById<EditText>(R.id.quantityInput)
        val quantityLayout = dialogView.findViewById<TextInputLayout>(R.id.quantityLayout)
        val productInfoSection = dialogView.findViewById<LinearLayout>(R.id.productInfoSection)
        val unitPriceText = dialogView.findViewById<TextView>(R.id.unitPriceText)
        val stockQuantityText = dialogView.findViewById<TextView>(R.id.stockQuantityText)
        val totalPriceText = dialogView.findViewById<TextView>(R.id.totalPriceText)

        var selectedProduct: StockProducts? = null

        productSelectorCard.setOnClickListener {
            showProductSelectionDialog { product ->
                selectedProduct = product
                selectedProductText.text = product.name
                selectedProductText.setTextColor(android.graphics.Color.parseColor("#111827"))

                productInfoSection.visibility = View.VISIBLE
                unitPriceText.text = currencyFormat.format(product.unitPrice)
                stockQuantityText.text = "${product.quantity} un"

                updateConsumptionTotal(quantityInput, product, totalPriceText)
            }
        }

        quantityInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                selectedProduct?.let { product ->
                    updateConsumptionTotal(quantityInput, product, totalPriceText)
                }
            }
        })

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Adicionar Consumo")
            .setView(dialogView)
            .setPositiveButton("Adicionar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            val addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            addButton.setOnClickListener {
                if (selectedProduct == null) {
                    Toast.makeText(requireContext(), "Selecione um produto", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val quantityStr = quantityInput.text.toString().trim()
                if (quantityStr.isEmpty()) {
                    quantityLayout.error = "Digite a quantidade"
                    return@setOnClickListener
                }

                val quantity = quantityStr.toIntOrNull()
                if (quantity == null || quantity <= 0) {
                    quantityLayout.error = "Quantidade deve ser maior que zero"
                    return@setOnClickListener
                }

                val product = selectedProduct!!

                if (quantity > product.quantity) {
                    quantityLayout.error = "Estoque insuficiente (${product.quantity} disponíveis)"
                    return@setOnClickListener
                }

                val item = ConsumptionItem(
                    id = UUID.randomUUID().toString(),
                    productId = product.id,
                    name = product.name,
                    unitPrice = product.unitPrice,
                    quantity = quantity
                )

                consumptionAdapter.addItem(item)
                updateTotal()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun updateConsumptionTotal(
        quantityInput: EditText,
        product: StockProducts,
        totalPriceText: TextView
    ) {
        val quantityStr = quantityInput.text.toString().trim()
        val quantity = quantityStr.toIntOrNull() ?: 0
        val total = product.unitPrice * quantity
        totalPriceText.text = currencyFormat.format(total)
    }


    private fun showProductSelectionDialog(onProductSelected: (StockProducts) -> Unit) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_select_product, null)

        val searchInput = dialogView.findViewById<EditText>(R.id.productSearchInput)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.productsRecyclerView)

        val adapter = ProductSelectionAdapter { product ->
            onProductSelected(product)
            productSelectionDialog?.dismiss()
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            val result = productsRepository.getAllProducts()
            result.onSuccess { products ->
                val sellableProducts = products.filter { it.type == ProductType.REVENDA }
                adapter.submitList(sellableProducts)
            }
        }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                lifecycleScope.launch {
                    val result = if (query.isNotEmpty()) {
                        productsRepository.searchProducts(query)
                    } else {
                        productsRepository.getAllProducts()
                    }
                    result.onSuccess { products ->
                        val sellableProducts = products.filter { it.type == ProductType.REVENDA }
                        adapter.submitList(sellableProducts)
                    }
                }
            }
        })

        productSelectionDialog = AlertDialog.Builder(requireContext())
            .setTitle("Selecionar Produto")
            .setView(dialogView)
            .setNegativeButton("Cancelar", null)
            .create()

        productSelectionDialog?.show()
    }

    private fun updateTotal() {
        val serviceValue = servicePrice
        val consumptionTotal = consumptionAdapter.getTotal()
        val finalTotal = serviceValue + consumptionTotal
        grandTotalText.text = currencyFormat.format(finalTotal)
    }

    private fun finalizarAtendimento() {
        val record = AttendanceRecord(
            id = UUID.randomUUID().toString(),
            appointmentId = appointmentId,
            clientId = clientId,
            clientName = clientName,
            serviceId = serviceId,
            serviceName = serviceName,
            servicePrice = servicePrice,
            scheduledTime = System.currentTimeMillis(),
            status = AttendanceStatus.ATTENDED,
            consumptions = consumptionAdapter.getItems()
        )

        finalizarButton.isEnabled = false
        finalizarButton.text = "Finalizando..."

        lifecycleScope.launch {
            delay(500)

            AppointmentsManager.removeAppointment(appointmentId)
            showSummaryDialog(record)

            finalizarButton.isEnabled = true
            finalizarButton.text = "FINALIZAR ATENDIMENTO"
        }
    }

    private fun confirmarFalta() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirmar Falta")
            .setMessage("Confirmar que ${clientName} não compareceu?\n\nNenhum valor será cobrado.")
            .setPositiveButton("Confirmar") { _, _ ->
                val record = AttendanceRecord(
                    id = UUID.randomUUID().toString(),
                    appointmentId = appointmentId,
                    clientId = clientId,
                    clientName = clientName,
                    serviceId = serviceId,
                    serviceName = serviceName,
                    servicePrice = servicePrice,
                    scheduledTime = System.currentTimeMillis(),
                    status = AttendanceStatus.NO_SHOW
                )

                lifecycleScope.launch {
                    delay(300)

                    AppointmentsManager.removeAppointment(appointmentId)

                    Toast.makeText(
                        requireContext(),
                        "Falta registrada para ${clientName}",
                        Toast.LENGTH_SHORT
                    ).show()

                    parentFragmentManager.popBackStack()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showSummaryDialog(record: AttendanceRecord) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_attendance_summary, null)

        val summaryMessageText = dialogView.findViewById<TextView>(R.id.summaryMessageText)

        val consumptions = record.consumptions
        val hasConsumptions = consumptions.isNotEmpty()

        val message = buildString {
            append("Atendimento registrado!\n\n")
            append("Cliente: ${record.clientName}\n")
            append("Serviço: ${record.serviceName}\n")
            append("Valor: ${CurrencyUtils.format(record.servicePrice)}\n")  // ✅

            if (hasConsumptions) {
                append("\n━━━━━━━━━━━━━━━━━━\n")
                append("PRODUTOS ADICIONAIS:\n")
                append("━━━━━━━━━━━━━━━━━━\n\n")
                consumptions.forEach { item ->
                    append("• ${item.name}\n")
                    append("  ${CurrencyUtils.format(item.unitPrice * item.quantity)}\n\n")  // ✅
                }
            }

            append("━━━━━━━━━━━━━━━━━━\n")
            append("TOTAL: ${CurrencyUtils.format(record.getTotalValue())}\n")  // ✅
            append("━━━━━━━━━━━━━━━━━━")
        }

        summaryMessageText.text = message

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                parentFragmentManager.popBackStack()
            }
            .setCancelable(false)
            .show()
    }

    private fun showServiceSelectionDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_select_service, null)

        val searchInput = dialogView.findViewById<EditText>(R.id.serviceSearchInput)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.servicesRecyclerView)

        val adapter = ServiceSelectionAdapter { service ->
            serviceId = service.id
            serviceName = service.name
            servicePrice = service.price

            serviceNameText.text = service.name
            servicePriceText.text = currencyFormat.format(service.price)

            updateTotalTexts()
            updateTotal()

            serviceSelectionDialog?.dismiss()

            Toast.makeText(requireContext(), "Serviço atualizado!", Toast.LENGTH_SHORT).show()
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            val result = servicesRepository.getAllServices()
            result.onSuccess { services ->
                adapter.submitList(services)
            }
        }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                lifecycleScope.launch {
                    val result = if (query.isNotEmpty()) {
                        servicesRepository.searchServices(query)
                    } else {
                        servicesRepository.getAllServices()
                    }
                    result.onSuccess { services ->
                        adapter.submitList(services)
                    }
                }
            }
        })

        serviceSelectionDialog = AlertDialog.Builder(requireContext())
            .setTitle("Trocar Serviço")
            .setView(dialogView)
            .setNegativeButton("Cancelar", null)
            .create()

        serviceSelectionDialog?.show()
    }

    private fun updateTotalTexts() {
        totalServiceNameText.text = serviceName
        totalServicePriceText.text = currencyFormat.format(servicePrice)
    }
}