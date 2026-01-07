package com.example.monee

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Hubungkan BottomNavigationView dengan NavController seperti biasa
        bottomNav.setupWithNavController(navController)

        // --- INI BAGIAN PENTINGNYA ---
        // Tambahkan listener untuk menangani klik ulang pada item yang sama
        bottomNav.setOnItemSelectedListener { item ->
            // Gunakan NavigationUI untuk menangani navigasi standar
            NavigationUI.onNavDestinationSelected(item, navController)

            // Cek apakah item yang diklik adalah item yang sedang aktif
            if (navController.currentDestination?.id == navController.graph.startDestinationId && item.itemId == navController.graph.startDestinationId) {
                // Ini untuk kasus khusus jika kita sudah di home dan klik home lagi (opsional)
                // Tidak melakukan apa-apa atau bisa scroll ke atas
            } else if (item.itemId == navController.graph.startDestinationId) {
                // Jika item yang diklik adalah tujuan awal (home), tapi kita tidak di sana,
                // maka pop up tumpukan navigasi kembali ke home.
                navController.popBackStack(navController.graph.startDestinationId, false)
            }
            true
        }
    }
}