package com.example.barberpro.ui.products

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.barberpro.adapter.ProdutosAdapter
import com.example.barberpro.model.StockProducts
import com.example.barberpro.repository.ProductsRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class ProductsFragment : Fragment() {

    private lateinit var backButton: ImageView
    private lateinit var searchEditText: EditText
    private lateinit var alertCountText: TextView
    private lateinit var produtosRecyclerView: RecyclerView
    private lateinit var fabAddProduto: FloatingActionButton

    private lateinit var produtosAdapter: ProdutosAdapter
    private val repository = ProductsRepository.getInstance()

    private var allProducts = listOf<StockProducts>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_products, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupRecyclerView()
        setupSearch()
        setupClickListeners()
        loadProducts()
    }

    private fun initializeViews(view: View) {
        backButton = view.findViewById(R.id.backButton)
        searchEditText = view.findViewById(R.id.searchEditText)
        alertCountText = view.findViewById(R.id.alertCountText)
        produtosRecyclerView = view.findViewById(R.id.produtosRecyclerView)
        fabAddProduto = view.findViewById(R.id.fabAddProduto)
    }

    private fun setupRecyclerView() {
        produtosAdapter = ProdutosAdapter(
            onProductClick = { product ->
                editarProduto(product)
            },
            onProductLongClick = { product ->
                mostrarOpcoesProduto(product)
            }
        )

        produtosRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = produtosAdapter
        }
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterProducts(s.toString())
            }
        })
    }

    private fun setupClickListeners() {
        fabAddProduto.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, NewProductFragment())
                .addToBackStack(null)
                .commit()
        }

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun loadProducts() {
        lifecycleScope.launch {
            val result = repository.getAllProducts()

            result.onSuccess { products ->
                allProducts = products
                filterProducts(searchEditText.text.toString())
                updateLowStockAlert()
            }

            result.onFailure { error ->
                Toast.makeText(
                    requireContext(),
                    "Erro ao carregar produtos: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun filterProducts(query: String) {
        val filtered = if (query.isEmpty()) {
            allProducts
        } else {
            allProducts.filter { product ->
                product.name.contains(query, ignoreCase = true)
            }
        }

        produtosAdapter.submitList(filtered)
    }

    private fun updateLowStockAlert() {
        val lowStock = allProducts.filter { it.isLowStock() }
        alertCountText.text = "${lowStock.size} itens precisam de reposição imediata"
    }

    private fun editarProduto(product: StockProducts) {
        parentFragmentManager.beginTransaction()
            .replace(
                R.id.fragmentContainer,
                NewProductFragment.newInstance(product)
            )
            .addToBackStack(null)
            .commit()
    }

    private fun mostrarOpcoesProduto(product: StockProducts) {
        val opcoes = arrayOf("Editar", "Adicionar estoque", "Excluir")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(product.name)
            .setItems(opcoes) { _, which ->
                when (which) {
                    0 -> editarProduto(product)
                    1 -> adicionarEstoque(product)
                    2 -> confirmarExclusao(product)
                }
            }
            .show()
    }

    private fun adicionarEstoque(product: StockProducts) {
        // TODO: Implementar dialog de adicionar estoque
        Toast.makeText(
            requireContext(),
            "Adicionar estoque: ${product.name}",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun confirmarExclusao(product: StockProducts) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Excluir Produto")
            .setMessage("Tem certeza que deseja excluir ${product.name}?")
            .setPositiveButton("Excluir") { _, _ ->
                excluirProduto(product)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun excluirProduto(product: StockProducts) {
        lifecycleScope.launch {
            val result = repository.deleteProduct(product.id)

            result.onSuccess {
                Toast.makeText(
                    requireContext(),
                    "Produto excluído",
                    Toast.LENGTH_SHORT
                ).show()
                loadProducts()
            }

            result.onFailure { error ->
                Toast.makeText(
                    requireContext(),
                    "Erro ao excluir: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadProducts()
    }
}