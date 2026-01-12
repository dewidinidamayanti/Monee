package com.example.monee

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.monee.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Hubungkan BottomNavigationView
        binding.bottomNav.setupWithNavController(navController)

        binding.bottomNav.setOnItemSelectedListener { item ->
            val currentDestination = navController.currentDestination?.id

            // Jika user klik menu yang sama → tidak lakukan apa-apa
            if (currentDestination == item.itemId) {
                return@setOnItemSelectedListener true
            }

            // Lakukan navigasi normal
            val handled = NavigationUI.onNavDestinationSelected(item, navController)

            // Jika klik Home dari halaman manapun → bersihkan backstack ke Home
            if (item.itemId == navController.graph.startDestinationId) {
                navController.popBackStack(navController.graph.startDestinationId, false)
            }

            handled
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
