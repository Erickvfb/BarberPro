package com.example.barberpro

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.barberpro.adapter.ConsumptionAdapter
import com.example.barberpro.adapter.ServiceSelectionAdapter
import com.example.barberpro.model.Appointment
import com.example.barberpro.model.AttendanceRecord
import com.example.barberpro.model.AttendanceStatus
import com.example.barberpro.model.ConsumptionItem
import com.example.barberpro.model.ProductType
import com.example.barberpro.model.StockProducts
import com.example.barberpro.model.com.example.barberpro.adapter.ProductSelectionAdapter
import com.example.barberpro.repository.AppointmentRepository
import com.example.barberpro.repository.ProductsRepository
import com.example.barberpro.repository.ServicesRepository
import com.example.barberpro.util.CurrencyUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class ConfirmAttendanceFragment : Fragment() {

    companion object {

        private const val ARG_APPOINTMENT_ID = "appointment_id"

        fun newInstance(
            appointmentId: String
        ): ConfirmAttendanceFragment {

            return ConfirmAttendanceFragment().apply {

                arguments = Bundle().apply {
                    putString(ARG_APPOINTMENT_ID, appointmentId)
                }
            }
        }
    }

    // HEADER
    private lateinit var backButton: ImageView

    // INFO AGENDAMENTO
    private lateinit var clientInitialText: TextView
    private lateinit var clientNameText: TextView
    private lateinit var scheduledTimeText: TextView
    private lateinit var serviceNameText: TextView
    private lateinit var servicePriceText: TextView
    private lateinit var editServiceButton: ImageView

    // STATUS
    private lateinit var attendedCard: MaterialCardView
    private lateinit var noShowCard: MaterialCardView

    // CONSUMOS
    private lateinit var consumptionSection: LinearLayout
    private lateinit var addConsumptionButton: MaterialCardView
    private lateinit var consumptionRecyclerView: RecyclerView

    // TOTAL
    private lateinit var totalServiceNameText: TextView
    private lateinit var totalServicePriceText: TextView
    private lateinit var grandTotalText: TextView

    // BOTÕES
    private lateinit var finalizarButton: MaterialButton
    private lateinit var confirmNoShowButton: MaterialButton

    // ADAPTER
    private lateinit var consumptionAdapter: ConsumptionAdapter

    // REPOSITORIES
    private val appointmentRepository =
        AppointmentRepository.getInstance()

    private val productsRepository =
        ProductsRepository.getInstance()

    private val servicesRepository =
        ServicesRepository.getInstance()

    // DADOS
    private val currencyFormat =
        NumberFormat.getCurrencyInstance(
            Locale("pt", "BR")
        )

    private var appointmentId = ""

    private var appointment: Appointment? = null

    private var currentStatus =
        AttendanceStatus.PENDING

    private var serviceId = ""
    private var serviceName = ""
    private var servicePrice = 0.0

    private var clientId = ""
    private var clientName = ""

    private var productSelectionDialog: AlertDialog? = null
    private var serviceSelectionDialog: AlertDialog? = null

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        appointmentId =
            requireArguments().getString(
                ARG_APPOINTMENT_ID
            ) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(
            R.layout.fragment_confirm_attendance,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(view, savedInstanceState)

        initViews(view)

        setupRecyclerView()

        setupClicks()

        loadAppointment()
    }

    private fun initViews(view: View) {

        backButton =
            view.findViewById(R.id.backButton)

        clientInitialText =
            view.findViewById(R.id.clientInitialText)

        clientNameText =
            view.findViewById(R.id.clientNameText)

        scheduledTimeText =
            view.findViewById(R.id.scheduledTimeText)

        serviceNameText =
            view.findViewById(R.id.serviceNameText)

        servicePriceText =
            view.findViewById(R.id.servicePriceText)

        editServiceButton =
            view.findViewById(R.id.editServiceButton)

        attendedCard =
            view.findViewById(R.id.attendedCard)

        noShowCard =
            view.findViewById(R.id.noShowCard)

        consumptionSection =
            view.findViewById(R.id.consumptionSection)

        addConsumptionButton =
            view.findViewById(R.id.addConsumptionButton)

        consumptionRecyclerView =
            view.findViewById(R.id.consumptionRecyclerView)

        totalServiceNameText =
            view.findViewById(R.id.totalServiceNameText)

        totalServicePriceText =
            view.findViewById(R.id.totalServicePriceText)

        grandTotalText =
            view.findViewById(R.id.grandTotalText)

        finalizarButton =
            view.findViewById(R.id.finalizarButton)

        confirmNoShowButton =
            view.findViewById(R.id.confirmNoShowButton)
    }

    private fun setupRecyclerView() {

        consumptionAdapter =
            ConsumptionAdapter { item ->

                consumptionAdapter.removeItem(item)

                updateTotal()
            }

        consumptionRecyclerView.apply {

            layoutManager =
                LinearLayoutManager(requireContext())

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

            selectAttendanceStatus(
                AttendanceStatus.ATTENDED
            )
        }

        noShowCard.setOnClickListener {

            selectAttendanceStatus(
                AttendanceStatus.NO_SHOW
            )
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

    private fun loadAppointment() {

        lifecycleScope.launch {

            val result =
                appointmentRepository.getAllAppointments()

            result.onSuccess { appointments ->

                val foundAppointment =
                    appointments.find {
                        it.id == appointmentId
                    }

                if (foundAppointment == null) {

                    Toast.makeText(
                        requireContext(),
                        "Agendamento não encontrado",
                        Toast.LENGTH_SHORT
                    ).show()

                    parentFragmentManager.popBackStack()

                    return@onSuccess
                }

                appointment = foundAppointment

                clientId =
                    foundAppointment.client.id

                clientName =
                    foundAppointment.client.name

                serviceId =
                    foundAppointment.service.id

                serviceName =
                    foundAppointment.service.name

                servicePrice =
                    foundAppointment.service.price

                populateScreen(foundAppointment)
            }

            result.onFailure {

                Toast.makeText(
                    requireContext(),
                    it.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun populateScreen(
        appointment: Appointment
    ) {

        clientInitialText.text =
            appointment.client.getInitial()

        clientNameText.text =
            appointment.client.name

        val formatter =
            SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
            )

        scheduledTimeText.text =
            formatter.format(
                appointment.startTime
            )

        serviceNameText.text =
            appointment.service.name

        servicePriceText.text =
            currencyFormat.format(
                appointment.service.price
            )

        totalServiceNameText.text =
            appointment.service.name

        totalServicePriceText.text =
            currencyFormat.format(
                appointment.service.price
            )

        grandTotalText.text =
            currencyFormat.format(
                appointment.service.price
            )
    }

    private fun selectAttendanceStatus(
        status: AttendanceStatus
    ) {

        currentStatus = status

        when (status) {

            AttendanceStatus.ATTENDED -> {

                consumptionSection.visibility =
                    View.VISIBLE

                confirmNoShowButton.visibility =
                    View.GONE
            }

            AttendanceStatus.NO_SHOW -> {

                consumptionSection.visibility =
                    View.GONE

                confirmNoShowButton.visibility =
                    View.VISIBLE
            }

            else -> {}
        }
    }

    private fun showAddConsumptionDialog() {

        val dialogView =
            LayoutInflater.from(requireContext())
                .inflate(
                    R.layout.dialog_add_consumption,
                    null
                )

        val productSelectorCard =
            dialogView.findViewById<MaterialCardView>(
                R.id.productSelectorCard
            )

        val selectedProductText =
            dialogView.findViewById<TextView>(
                R.id.selectedProductText
            )

        val quantityInput =
            dialogView.findViewById<EditText>(
                R.id.quantityInput
            )

        val quantityLayout =
            dialogView.findViewById<TextInputLayout>(
                R.id.quantityLayout
            )

        val productInfoSection =
            dialogView.findViewById<LinearLayout>(
                R.id.productInfoSection
            )

        val unitPriceText =
            dialogView.findViewById<TextView>(
                R.id.unitPriceText
            )

        val stockQuantityText =
            dialogView.findViewById<TextView>(
                R.id.stockQuantityText
            )

        val totalPriceText =
            dialogView.findViewById<TextView>(
                R.id.totalPriceText
            )

        var selectedProduct:
                StockProducts? = null

        productSelectorCard.setOnClickListener {

            showProductSelectionDialog { product ->

                selectedProduct = product

                selectedProductText.text =
                    product.name

                productInfoSection.visibility =
                    View.VISIBLE

                unitPriceText.text =
                    currencyFormat.format(
                        product.unitPrice
                    )

                stockQuantityText.text =
                    "${product.quantity} un"

                updateConsumptionTotal(
                    quantityInput,
                    product,
                    totalPriceText
                )
            }
        }

        quantityInput.addTextChangedListener(
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
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {

                    selectedProduct?.let {

                        updateConsumptionTotal(
                            quantityInput,
                            it,
                            totalPriceText
                        )
                    }
                }
            }
        )

        val dialog =
            AlertDialog.Builder(requireContext())
                .setTitle("Adicionar Consumo")
                .setView(dialogView)
                .setPositiveButton(
                    "Adicionar",
                    null
                )
                .setNegativeButton(
                    "Cancelar",
                    null
                )
                .create()

        dialog.setOnShowListener {

            val addButton =
                dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
                )

            addButton.setOnClickListener {

                if (selectedProduct == null) {

                    Toast.makeText(
                        requireContext(),
                        "Selecione um produto",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setOnClickListener
                }

                val quantity =
                    quantityInput.text.toString()
                        .toIntOrNull()

                if (quantity == null || quantity <= 0) {

                    quantityLayout.error =
                        "Quantidade inválida"

                    return@setOnClickListener
                }

                val product =
                    selectedProduct!!

                val item =
                    ConsumptionItem(
                        id = product.id,
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

        val quantity =
            quantityInput.text.toString()
                .toIntOrNull() ?: 0

        val total =
            product.unitPrice * quantity

        totalPriceText.text =
            currencyFormat.format(total)
    }

    private fun showProductSelectionDialog(
        onProductSelected: (StockProducts) -> Unit
    ) {

        val dialogView =
            LayoutInflater.from(requireContext())
                .inflate(
                    R.layout.dialog_select_product,
                    null
                )

        val searchInput =
            dialogView.findViewById<EditText>(
                R.id.productSearchInput
            )

        val recyclerView =
            dialogView.findViewById<RecyclerView>(
                R.id.productsRecyclerView
            )

        val adapter =
            ProductSelectionAdapter { product ->

                onProductSelected(product)

                productSelectionDialog?.dismiss()
            }

        recyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        recyclerView.adapter = adapter

        lifecycleScope.launch {

            val result =
                productsRepository.getAllProducts()

            result.onSuccess { products ->

                adapter.submitList(
                    products.filter {
                        it.type == ProductType.REVENDA
                    }
                )
            }
        }

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
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {

                    val query =
                        s.toString()

                    lifecycleScope.launch {

                        val result =
                            productsRepository.searchProducts(
                                query
                            )

                        result.onSuccess {

                            adapter.submitList(it)
                        }
                    }
                }
            }
        )

        productSelectionDialog =
            AlertDialog.Builder(requireContext())
                .setTitle("Selecionar Produto")
                .setView(dialogView)
                .setNegativeButton(
                    "Cancelar",
                    null
                )
                .create()

        productSelectionDialog?.show()
    }

    private fun showServiceSelectionDialog() {

        val dialogView =
            LayoutInflater.from(requireContext())
                .inflate(
                    R.layout.dialog_select_service,
                    null
                )

        val searchInput =
            dialogView.findViewById<EditText>(
                R.id.serviceSearchInput
            )

        val recyclerView =
            dialogView.findViewById<RecyclerView>(
                R.id.servicesRecyclerView
            )

        val adapter =
            ServiceSelectionAdapter { service ->

                serviceId = service.id
                serviceName = service.name
                servicePrice = service.price

                serviceNameText.text =
                    service.name

                servicePriceText.text =
                    currencyFormat.format(
                        service.price
                    )

                totalServiceNameText.text =
                    service.name

                totalServicePriceText.text =
                    currencyFormat.format(
                        service.price
                    )

                updateTotal()

                serviceSelectionDialog?.dismiss()
            }

        recyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        recyclerView.adapter = adapter

        lifecycleScope.launch {

            val result =
                servicesRepository.getAllServices()

            result.onSuccess {

                adapter.submitList(it)
            }
        }

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
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {

                    lifecycleScope.launch {

                        val result =
                            servicesRepository.searchServices(
                                s.toString()
                            )

                        result.onSuccess {

                            adapter.submitList(it)
                        }
                    }
                }
            }
        )

        serviceSelectionDialog =
            AlertDialog.Builder(requireContext())
                .setTitle("Selecionar Serviço")
                .setView(dialogView)
                .setNegativeButton(
                    "Cancelar",
                    null
                )
                .create()

        serviceSelectionDialog?.show()
    }

    private fun updateTotal() {

        val total =
            servicePrice +
                    consumptionAdapter.getTotal()

        grandTotalText.text =
            currencyFormat.format(total)
    }

    private fun finalizarAtendimento() {

        finalizarButton.isEnabled = false

        lifecycleScope.launch {

            val result =
                appointmentRepository.completeAppointment(
                    appointmentId = appointmentId,
                    status = currentStatus.name,
                    consumptions =
                    consumptionAdapter.getItems()
                )

            result.onSuccess { attendance ->

                showSummaryDialog(attendance)
            }

            result.onFailure {

                Toast.makeText(
                    requireContext(),
                    it.message,
                    Toast.LENGTH_LONG
                ).show()
            }

            finalizarButton.isEnabled = true
        }
    }

    private fun confirmarFalta() {

        lifecycleScope.launch {

            val result = appointmentRepository.completeAppointment(
                appointmentId = appointmentId,
                status = AttendanceStatus.NO_SHOW.name,
                consumptions = emptyList()
            )

            result.onSuccess {

                Toast.makeText(
                    requireContext(),
                    "Falta confirmada",
                    Toast.LENGTH_SHORT
                ).show()

                parentFragmentManager.popBackStack()
            }

            result.onFailure { error ->

                Toast.makeText(
                    requireContext(),
                    error.message ?: "Erro ao confirmar falta",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showSummaryDialog(
        record: AttendanceRecord
    ) {

        val dialogView =
            LayoutInflater.from(requireContext())
                .inflate(
                    R.layout.dialog_attendance_summary,
                    null
                )

        val summaryMessageText =
            dialogView.findViewById<TextView>(
                R.id.summaryMessageText
            )

        val message =
            buildString {

                append("Cliente: ${record.clientName}\n")
                append("Serviço: ${record.serviceName}\n")
                append(
                    "Valor Serviço: ${
                        CurrencyUtils.format(
                            record.servicePrice
                        )
                    }\n\n"
                )

                if (record.consumptions.isNotEmpty()) {

                    append("Produtos:\n\n")

                    record.consumptions.forEach {

                        append(
                            "${it.name} x${it.quantity}\n"
                        )

                        append(
                            "${
                                CurrencyUtils.format(
                                    it.getTotal()
                                )
                            }\n\n"
                        )
                    }
                }

                record.consumptions.forEach {
                    Log.d(
                        "ATTENDANCE_DEBUG",
                        """
        nome=${it.name}
        qtd=${it.quantity}
        unit=${it.unitPrice}
        total=${it.getTotal()}
        """.trimIndent()
                    )
                }

                append(
                    "TOTAL: ${
                        CurrencyUtils.format(
                            record.getTotalValue()
                        )
                    }"
                )
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
}