package com.example.vistas

import android.app.AlertDialog
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : Fragment(R.layout.screen_settings) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Referencias
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        val btnAdmin = view.findViewById<Button>(R.id.btnAdminPanel) // El nuevo botón
        val txtUser = view.findViewById<TextView>(R.id.txtCurrentUser)
        val switchDark = view.findViewById<MaterialSwitch>(R.id.switchDarkMode)

        // Obtener usuario actual
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email ?: ""
        txtUser.text = email

        // --- LÓGICA DE ADMIN ---
        // Si el correo contiene "admin", mostramos el botón
        if (email.lowercase().contains("admin")) {
            btnAdmin.visibility = View.VISIBLE
            btnAdmin.setOnClickListener {
                // Navegamos al panel de admin
                findNavController().navigate(R.id.adminFragment)
            }
        } else {
            btnAdmin.visibility = View.GONE
        }

        // Lógica Modo Oscuro
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        switchDark.isChecked = currentNightMode == Configuration.UI_MODE_NIGHT_YES

        switchDark.setOnCheckedChangeListener { _, isChecked ->
            val mode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(mode)
        }

        // Lógica Cerrar Sesión
        btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Cerrar Sesión")
                .setMessage("¿Seguro que quieres salir?")
                .setPositiveButton("Salir") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    val navOptions = androidx.navigation.NavOptions.Builder()
                        .setPopUpTo(R.id.nav_graph, true).build()
                    findNavController().navigate(R.id.loginFragment, null, navOptions)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }
}