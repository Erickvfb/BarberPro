package com.example.barberpro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.barberpro.adapter.ServicosAcumuladoAdapter
import com.example.barberpro.model.ServiceSummary
import com.google.android.material.card.MaterialCardView
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class RelatoryFragment : Fragment() {

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

    private lateinit var servicosAdapter: ServicosAcumuladoAdapter
    private lateinit var produtosAdapter: ServicosAcumuladoAdapter

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
        //moreOptionsButton.setOnClickListener { showOptionsMenu() }
    }

    private fun loadReportData() {
        updatePeriodText()

        val servicos = listOf(
            ServiceSummary("1", "Corte Social", 42, 2100.0, R.drawable.ic_scissors),
            ServiceSummary("2", "Barba Completa", 28, 1120.0, R.drawable.ic_barber),
            ServiceSummary("3", "Combo Master", 11, 990.0, R.drawable.ic_star)
        )

        val produtos = listOf(
            ServiceSummary("1", "Pomada Modeladora", 15, 675.0, R.drawable.ic_box),
            ServiceSummary("2", "Óleo para Barba", 8, 545.50, R.drawable.ic_box)
        )

        val totalServicos = servicos.sumOf { it.valorTotal }
        val totalProdutos = produtos.sumOf { it.valorTotal }
        val valorTotal = totalServicos + totalProdutos

        valorTotalText.text = currencyFormat.format(valorTotal)
        totalServicosText.text = "TOTAL: ${currencyFormat.format(totalServicos)}"
        totalProdutosText.text = "TOTAL: ${currencyFormat.format(totalProdutos)}"

        comparacaoText.text = "+12% VS MÊS ANTERIOR"

        servicosAdapter.submitList(servicos)
        produtosAdapter.submitList(produtos)
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
            startDate.timeInMillis = selection.first
            endDate.timeInMillis = selection.second

            updatePeriodText()
            loadReportData()
        }

        picker.show(parentFragmentManager, "DATE_RANGE_PICKER")
    }

    private fun exportarRelatorio() {
        Toast.makeText(requireContext(), "Em desenvolvimento", Toast.LENGTH_SHORT).show()
    }

   /* private fun showOptionsMenu() {
        val options = arrayOf(
            "Exportar PDF",
            "Exportar Excel",
            "Compartilhar",
            "Imprimir"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Opções")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> toast("Exportando PDF...")
                    1 -> toast("Exportando Excel...")
                    2 -> toast("Compartilhando...")
                    3 -> toast("Imprimindo...")
                }
            }
            .show()
    }*/

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}