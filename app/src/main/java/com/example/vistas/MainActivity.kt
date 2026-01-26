package com.example.vistas

import android.content.Context
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

        // --- GESTIÓN DEL TEMA (MODO OSCURO/CLARO) ---
        // 1. Cargamos la preferencia guardada por el usuario
        val sharedPrefs = getSharedPreferences("AppConfig", Context.MODE_PRIVATE)
        val esOscuro = sharedPrefs.getBoolean("MODO_OSCURO", true) // Por defecto TRUE (Oscuro)

        // 2. Aplicamos el tema antes de cargar la vista
        if (esOscuro) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        setContentView(R.layout.activity_main)

        // --- CONFIGURACIÓN DE NAVEGACIÓN ---
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // El ID en activity_main.xml es 'bottom_nav'
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        if (bottomNav != null) {
            // Vincular BottomNav con NavController
            bottomNav.setupWithNavController(navController)

            // Lógica para ocultar el menú en pantallas específicas
            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.loginFragment,
                    R.id.ocrFragment,
                    R.id.reportsFragment -> {
                        bottomNav.visibility = View.GONE
                    }
                    else -> {
                        bottomNav.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}
