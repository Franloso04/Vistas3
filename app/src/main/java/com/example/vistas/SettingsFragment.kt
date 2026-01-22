package com.example.vistas

import android.app.AlertDialog
import android.content.Intent
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

        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        val btnAdmin = view.findViewById<Button>(R.id.btnAdminPanel)
        val btnSupport = view.findViewById<Button>(R.id.btnSupport) // NUEVO
        val txtUser = view.findViewById<TextView>(R.id.txtCurrentUser)
        val switchDark = view.findViewById<MaterialSwitch>(R.id.switchDarkMode)

        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email ?: ""
        txtUser.text = email

        // Admin
        if (email.lowercase().contains("admin")) {
            btnAdmin.visibility = View.VISIBLE
            btnAdmin.setOnClickListener { findNavController().navigate(R.id.adminFragment) }
        } else {
            btnAdmin.visibility = View.GONE
        }

        // Modo Oscuro
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        switchDark.isChecked = currentNightMode == Configuration.UI_MODE_NIGHT_YES
        switchDark.setOnCheckedChangeListener { _, isChecked ->
            val mode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(mode)
        }

        // --- NUEVO: NAVEGACIÓN A REPORTES ---
        btnSupport.setOnClickListener {
            // Asegúrate de tener esta acción o el ID del fragmento en tu nav_graph
            // Si no tienes acción creada, usa el ID del fragmento destino:
            try {
                findNavController().navigate(R.id.reportsFragment)
            } catch (e: Exception) {
                // Si falla por ID directo, verifica tu nav_graph.xml
            }
        }

        // Logout
        btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Cerrar Sesión")
                .setMessage("¿Seguro que quieres salir?")
                .setPositiveButton("Salir") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(requireContext(), IntroActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }
}