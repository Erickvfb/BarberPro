package com.example.barberpro

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.card.MaterialCardView
import androidx.fragment.app.Fragment
import com.example.barberpro.ui.products.ProductsFragment
import com.example.barberpro.ui.services.ServicesFragment

class StockFragment : Fragment() {

    private lateinit var servicosCard: MaterialCardView
    private lateinit var produtosCard: MaterialCardView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_stock, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupClickListeners()
    }

    private fun initializeViews(view: View) {
        servicosCard = view.findViewById(R.id.servicosCard)
        produtosCard = view.findViewById(R.id.produtosCard)
    }

    private fun setupClickListeners() {

        // Serviços
        servicosCard.setOnClickListener {
            navigateToServicos()
        }

        // Produtos
        produtosCard.setOnClickListener {
            navigateToProdutos()
        }
    }

    private fun navigateToServicos() {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ServicesFragment())
                .addToBackStack(null)
                .commit()
        }

    private fun navigateToProdutos() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, ProductsFragment())
            .addToBackStack(null)
            .commit()
    }
}