package com.example.barberpro.ui.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.barberpro.R
import com.example.barberpro.data.api.ProductRequest
import com.example.barberpro.model.ProductType
import com.example.barberpro.model.StockProducts
import com.example.barberpro.repository.ProductsRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class NewProductFragment : Fragment() {

    companion object {

        private const val ARG_PRODUCT_ID = "product_id"

        fun newInstance(product: StockProducts? = null): NewProductFragment {
            return NewProductFragment().apply {
                arguments = Bundle().apply {
                    product?.let {
                        putString(ARG_PRODUCT_ID, it.id)
                    }
                }
            }
        }
    }

    private lateinit var nomeInput: EditText
    private lateinit var estoqueInicialInput: EditText
    private lateinit var alertaEstoqueInput: EditText
    private lateinit var precoVendaInput: EditText
    private lateinit var precoCustoInput: EditText

    private lateinit var saveButton: MaterialButton
    private lateinit var deleteButton: MaterialButton

    private lateinit var tipoRevendaButton: MaterialButton
    private lateinit var tipoInsumoButton: MaterialButton

    private var selectedType: ProductType? = null

    private val repository = ProductsRepository.getInstance()

    private var productToEdit: StockProducts? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.getString(ARG_PRODUCT_ID)?.let { productId ->

            lifecycleScope.launch {

                repository.getProductById(productId)
                    .onSuccess { product ->

                        productToEdit = product

                        if (isAdded) {
                            fillFormWithProduct(product)
                        }
                    }
                    .onFailure {
                        Toast.makeText(
                            requireContext(),
                            it.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(
            R.layout.fragment_new_product,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupClickListeners()

        productToEdit?.let {
            fillFormWithProduct(it)
        }
    }

    private fun initializeViews(view: View) {

        nomeInput = view.findViewById(R.id.nomeInput)
        estoqueInicialInput = view.findViewById(R.id.estoqueInicialInput)
        alertaEstoqueInput = view.findViewById(R.id.alertaEstoqueInput)
        precoVendaInput = view.findViewById(R.id.precoVendaInput)
        precoCustoInput = view.findViewById(R.id.precoCustoInput)

        tipoRevendaButton = view.findViewById(R.id.tipoRevendaButton)
        tipoInsumoButton = view.findViewById(R.id.tipoInsumoButton)

        saveButton = view.findViewById(R.id.salvarButton)
        deleteButton = view.findViewById(R.id.excluirButton)
    }

    private fun setupClickListeners() {

        saveButton.setOnClickListener {
            saveProduct()
        }

        deleteButton.setOnClickListener {
            confirmDelete()
        }

        tipoRevendaButton.setOnClickListener {
            selectedType = ProductType.REVENDA
            updateTypeButtons()
        }

        tipoInsumoButton.setOnClickListener {
            selectedType = ProductType.INSUMO
            updateTypeButtons()
        }

        view?.findViewById<View>(R.id.backButton)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun updateTypeButtons() {

        // REVENDA
        if (selectedType == ProductType.REVENDA) {

            tipoRevendaButton.setBackgroundColor(
                requireContext().getColor(R.color.chip_background)
            )

            tipoRevendaButton.setTextColor(
                requireContext().getColor(android.R.color.white)
            )

            tipoRevendaButton.strokeWidth = 2

        } else {

            tipoRevendaButton.setBackgroundColor(
                requireContext().getColor(R.color.card_background)
            )

            tipoRevendaButton.setTextColor(
                requireContext().getColor(R.color.text_secondary)
            )

            tipoRevendaButton.strokeWidth = 0
        }

        // INSUMO
        if (selectedType == ProductType.INSUMO) {

            tipoInsumoButton.setBackgroundColor(
                requireContext().getColor(R.color.gold)
            )

            tipoInsumoButton.setTextColor(
                requireContext().getColor(android.R.color.white)
            )

            tipoInsumoButton.strokeWidth = 2

        } else {

            tipoInsumoButton.setBackgroundColor(
                requireContext().getColor(R.color.card_background)
            )

            tipoInsumoButton.setTextColor(
                requireContext().getColor(R.color.text_secondary)
            )

            tipoInsumoButton.strokeWidth = 0
        }
    }

    private fun fillFormWithProduct(product: StockProducts) {

        nomeInput.setText(product.name)

        estoqueInicialInput.setText(
            product.quantity.toString()
        )

        alertaEstoqueInput.setText(
            product.alertThreshold.toString()
        )

        precoVendaInput.setText(
            product.unitPrice.toString()
        )

        precoCustoInput.setText(
            product.costPrice.toString()
        )

        selectedType = product.type

        updateTypeButtons()

        deleteButton.visibility = View.VISIBLE
    }

    private fun saveProduct() {

        val name = nomeInput.text.toString().trim()

        val quantity =
            estoqueInicialInput.text.toString().toIntOrNull() ?: 0

        val alertThreshold =
            alertaEstoqueInput.text.toString().toIntOrNull() ?: 5

        val unitPrice =
            precoVendaInput.text.toString().toDoubleOrNull() ?: 0.0

        val costPrice =
            precoCustoInput.text.toString().toDoubleOrNull() ?: 0.0

        val type = selectedType

        // VALIDAÇÕES

        if (name.isEmpty()) {
            nomeInput.error = "Digite o nome do produto"
            return
        }

        if (quantity < 0) {
            estoqueInicialInput.error = "Quantidade inválida"
            return
        }

        if (unitPrice <= 0) {
            precoVendaInput.error = "Preço inválido"
            return
        }

        if (type == null) {
            Toast.makeText(
                requireContext(),
                "Selecione o tipo do produto",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        saveButton.isEnabled = false
        saveButton.text = "Salvando..."

        lifecycleScope.launch {

            val request = ProductRequest(
                name = name,
                quantity = quantity,
                alert_threshold = alertThreshold,
                unit_price = unitPrice,
                cost_price = costPrice,
                type = type.name
            )

            val result = if (productToEdit == null) {

                repository.addProduct(request)

            } else {

                repository.updateProduct(
                    productToEdit!!.id,
                    request
                )
            }

            result.onSuccess {

                Toast.makeText(
                    requireContext(),
                    "Produto salvo com sucesso!",
                    Toast.LENGTH_SHORT
                ).show()

                parentFragmentManager.popBackStack()

            }.onFailure {

                Toast.makeText(
                    requireContext(),
                    it.message,
                    Toast.LENGTH_LONG
                ).show()

                saveButton.isEnabled = true
                saveButton.text = "SALVAR"
            }
        }
    }

    private fun confirmDelete() {

        productToEdit?.let { product ->

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Excluir Produto")
                .setMessage("Deseja excluir ${product.name}?")

                .setPositiveButton("Excluir") { _, _ ->
                    deleteProduct(product)
                }

                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun deleteProduct(product: StockProducts) {

        lifecycleScope.launch {

            try {

                // PRIMEIRA CAMADA:
                // verifica no app se produto possui vendas
                val hasSales = repository.hasProductInSales(product.id)

                if (hasSales) {

                    Toast.makeText(
                        requireContext(),
                        "Este produto possui vendas vinculadas e não pode ser excluído",
                        Toast.LENGTH_LONG
                    ).show()

                    return@launch
                }

                // SEGUNDA CAMADA:
                // backend também irá validar
                repository.deleteProduct(product.id)
                    .onSuccess {

                        Toast.makeText(
                            requireContext(),
                            "Produto excluído com sucesso",
                            Toast.LENGTH_SHORT
                        ).show()

                        parentFragmentManager.popBackStack()
                    }
                    .onFailure {

                        Toast.makeText(
                            requireContext(),
                            it.message ?: "Erro ao excluir produto",
                            Toast.LENGTH_LONG
                        ).show()
                    }

            } catch (e: Exception) {

                Toast.makeText(
                    requireContext(),
                    e.message ?: "Erro inesperado",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}