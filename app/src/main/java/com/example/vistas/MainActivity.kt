package com.example.vistas

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- CAMBIO 1: Configuración predeterminada de Modo Oscuro ---
        // Si el usuario nunca ha elegido, forzamos el modo oscuro por defecto.
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_UNSPECIFIED) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        setContentView(R.layout.activity_main)

        // 1. Inicializar componentes
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        // 2. Vincular BottomNav con NavController
        bottomNav.setupWithNavController(navController)

        // 3. Lógica para ocultar el menú
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                // --- CAMBIO 2: Añadido reportsFragment para ocultar el menú ahí también ---
                R.id.loginFragment, R.id.ocrFragment, R.id.reportsFragment -> {
                    bottomNav.visibility = View.GONE
                }
                else -> {
                    bottomNav.visibility = View.VISIBLE
                }
            }
        }
    }
}