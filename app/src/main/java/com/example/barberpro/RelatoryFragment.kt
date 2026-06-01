package com.example.barberpro

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.barberpro.adapter.ServicosAcumuladoAdapter
import com.example.barberpro.model.ServiceSummary
import com.example.barberpro.repository.ReportsRepository
import com.example.barberpro.repository.ProductSalesData
import com.example.barberpro.repository.ServiceSalesData
import com.google.android.material.card.MaterialCardView
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class RelatoryFragment : Fragment() {

    // Views
    private lateinit var moreOptionsButton: ImageView
    private lateinit var periodCard: MaterialCardView
    private lateinit var periodText: TextView
    private lateinit var downloadCard: MaterialCardView
    private lateinit var valorTotalText: TextView
    private lateinit var comparacaoText: TextView
    private lateinit var totalServicosText: TextView
    private lateinit var servicosRecyclerView: RecyclerView
    private lateinit var totalProdutosText: TextView
    private lateinit var produtosRecyclerView: RecyclerView
    private lateinit var loadingProgressBar: ProgressBar

    private lateinit var servicosAdapter: ServicosAcumuladoAdapter
    private lateinit var produtosAdapter: ServicosAcumuladoAdapter

    private val reportsRepository = ReportsRepository.getInstance()

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    private var startDate = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }
    private var endDate = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_relatory, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupRecyclerViews()
        setupClickListeners()

        initializeDefaultPeriod()

        loadReportData()
    }

    override fun onResume() {
        super.onResume()
        loadReportData()
    }

    private fun initializeViews(view: View) {
        moreOptionsButton = view.findViewById(R.id.moreOptionsButton)
        periodCard = view.findViewById(R.id.periodCard)
        periodText = view.findViewById(R.id.periodText)
        downloadCard = view.findViewById(R.id.downloadCard)
        valorTotalText = view.findViewById(R.id.valorTotalText)
        comparacaoText = view.findViewById(R.id.comparacaoText)
        totalServicosText = view.findViewById(R.id.totalServicosText)
        servicosRecyclerView = view.findViewById(R.id.servicosRecyclerView)
        totalProdutosText = view.findViewById(R.id.totalProdutosText)
        produtosRecyclerView = view.findViewById(R.id.produtosRecyclerView)
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar)
    }

    private fun setupRecyclerViews() {
        servicosAdapter = ServicosAcumuladoAdapter()
        servicosRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        servicosRecyclerView.adapter = servicosAdapter

        produtosAdapter = ServicosAcumuladoAdapter()
        produtosRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        produtosRecyclerView.adapter = produtosAdapter
    }

    private fun setupClickListeners() {
        periodCard.setOnClickListener { showDateRangePicker() }
        downloadCard.setOnClickListener { exportarRelatorio() }
    }

    private fun loadReportData() {
        updatePeriodText()
        showLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            Log.d("RELATORY", "Carregando relatório completo...")

            try {
                // 1. Buscar receita total
                val revenueResult = reportsRepository.getRevenueReport(startDate.time, endDate.time)

                // 2. Buscar serviços mais vendidos
                val servicesResult = reportsRepository.getTopServices(startDate.time, endDate.time)

                // 3. Buscar produtos mais vendidos
                val productsResult = reportsRepository.getTopProducts(startDate.time, endDate.time)

                // Processar receita
                revenueResult.onSuccess { report ->
                    val valorTotal = report.totalServices + report.totalProducts
                    valorTotalText.text = currencyFormat.format(valorTotal)
                    totalServicosText.text = "TOTAL: ${currencyFormat.format(report.totalServices)}"
                    totalProdutosText.text = "TOTAL: ${currencyFormat.format(report.totalProducts)}"
                    comparacaoText.text = "+12% VS MÊS ANTERIOR"
                }

                // Processar serviços
                servicesResult.onSuccess { services ->
                    Log.d("RELATORY", " ${services.size} serviços recebidos")

                    // Converter para ServiceSummary para exibir no adapter
                    val serviceSummaryList = services.map { service ->
                        ServiceSummary(
                            id = service.id,
                            nome = service.name,
                            quantidade = service.quantity,
                            valorTotal = service.totalRevenue,
                            iconRes = R.drawable.ic_scissors
                        )
                    }

                    servicosAdapter.submitList(serviceSummaryList)
                }

                servicesResult.onFailure { error ->
                    Log.e("RELATORY_ERROR", "Erro ao buscar serviços: ${error.message}")
                }

                // Processar produtos
                productsResult.onSuccess { products ->
                    Log.d("RELATORY", " ${products.size} produtos recebidos")

                    // Converter para ServiceSummary para exibir no adapter
                    val productSummaryList = products.map { product ->
                        ServiceSummary(
                            id = product.id,
                            nome = product.name,
                            quantidade = product.quantity,
                            valorTotal = product.totalRevenue,
                            iconRes = R.drawable.ic_stock
                        )
                    }

                    produtosAdapter.submitList(productSummaryList)
                }

                productsResult.onFailure { error ->
                    Log.e("RELATORY_ERROR", "Erro ao buscar produtos: ${error.message}")
                }

                showLoading(false)
                Log.d("RELATORY", " Relatório carregado com sucesso!")

            } catch (e: Exception) {
                Log.e("RELATORY_ERROR", "Erro geral: ${e.message}")
                showError("Erro ao carregar relatório: ${e.message}")
                showLoading(false)
            }
        }
    }
    private fun updatePeriodText() {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale("pt", "BR"))
        periodText.text =
            "${formatter.format(startDate.time)} - ${formatter.format(endDate.time)}"
    }

    private fun showDateRangePicker() {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Selecionar Período")
            .setTheme(R.style.ThemeOverlay_BarberPro_MaterialCalendar)
            .setSelection(
                androidx.core.util.Pair(
                    startDate.timeInMillis,
                    endDate.timeInMillis
                )
            )
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            // MaterialDatePicker retorna UTC
            val utc = TimeZone.getTimeZone("UTC")

            val startCal = Calendar.getInstance(utc)
            startCal.timeInMillis = selection.first

            val endCal = Calendar.getInstance(utc)
            endCal.timeInMillis = selection.second

            startDate.set(
                startCal.get(Calendar.YEAR),
                startCal.get(Calendar.MONTH),
                startCal.get(Calendar.DAY_OF_MONTH),
                0,
                0,
                0
            )

            endDate.set(
                endCal.get(Calendar.YEAR),
                endCal.get(Calendar.MONTH),
                endCal.get(Calendar.DAY_OF_MONTH),
                23,
                59,
                59
            )

            startDate.set(Calendar.MILLISECOND, 0)
            endDate.set(Calendar.MILLISECOND, 999)

            updatePeriodText()
            loadReportData()
        }

        picker.show(parentFragmentManager, "DATE_RANGE_PICKER")
    }

    private fun exportarRelatorio() {
        Toast.makeText(
            requireContext(),
            "Funcionalidade em desenvolvimento",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showLoading(isLoading: Boolean) {
        loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun initializeDefaultPeriod() {
        val now = Calendar.getInstance()

        startDate = Calendar.getInstance().apply {
            set(Calendar.YEAR, now.get(Calendar.YEAR))
            set(Calendar.MONTH, now.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        endDate = Calendar.getInstance().apply {
            set(Calendar.YEAR, now.get(Calendar.YEAR))
            set(Calendar.MONTH, now.get(Calendar.MONTH))
            set(
                Calendar.DAY_OF_MONTH,
                getActualMaximum(Calendar.DAY_OF_MONTH)
            )
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
    }
}