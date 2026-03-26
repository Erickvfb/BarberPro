package com.example.barberpro

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainContainerActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    private val homeFragment by lazy { HomeFragment() }
    private val clientsFragment by lazy { ClientsFragment() }
    private val statsFragment by lazy { RelatoryFragment() }
    private val stockFragment by lazy { StockFragment() }
    private val profileFragment by lazy { ProfileFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_container)

        bottomNavigation = findViewById(R.id.bottomNavigation)
        setupBottomNavigation()

        if (savedInstanceState == null) {
            loadFragment(homeFragment)
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(homeFragment)
                    true
                }
                R.id.nav_clients -> {
                    loadFragment(clientsFragment)
                    true
                }
                R.id.nav_stats -> {
                    loadFragment(statsFragment)
                    true
                }
                R.id.nav_stock -> {
                    loadFragment(stockFragment)
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(profileFragment)
                    true
                }
                else -> false
            }
        }
    }


    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun setSelectedMenuItem(itemId: Int) {
        bottomNavigation.selectedItemId = itemId
    }
}
