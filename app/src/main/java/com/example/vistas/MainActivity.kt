package com.example.vistas

import android.content.Context
import android.content.Intent
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

        val prefs = getSharedPreferences("AppConfig", Context.MODE_PRIVATE)

        // COMPROBACIÓN DE CADUCIDAD DE SESIÓN 5 DIAS
        val lastLogin = prefs.getLong("LAST_LOGIN_TIMESTAMP", 0L)

        val cincoDiasEnMillis = 5 * 24 * 60 * 60 * 1000L


        if (lastLogin > 0 && (System.currentTimeMillis() - lastLogin > cincoDiasEnMillis)) {

            prefs.edit().remove("LAST_LOGIN_TIMESTAMP").apply()


            val intent = Intent(this, IntroActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }


        val esOscuro = prefs.getBoolean("MODO_OSCURO", true)
        if (esOscuro) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        setContentView(R.layout.activity_main)


        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment ?: return
        val navController = navHostFragment.navController
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        if (bottomNav != null) {
            bottomNav.setupWithNavController(navController)


            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.loginFragment,
                    R.id.ocrFragment,
                    R.id.reportsFragment -> bottomNav.visibility = View.GONE
                    else -> bottomNav.visibility = View.VISIBLE
                }
            }
        }
    }
}