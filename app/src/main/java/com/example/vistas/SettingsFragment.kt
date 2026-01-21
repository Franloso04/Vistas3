package com.example.vistas

import android.app.AlertDialog
import android.content.Intent // IMPORTANTE: Necesario para cambiar de Activity
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
        val btnAdmin = view.findViewById<Button>(R.id.btnAdminPanel)
        val txtUser = view.findViewById<TextView>(R.id.txtCurrentUser)
        val switchDark = view.findViewById<MaterialSwitch>(R.id.switchDarkMode)

        // Obtener usuario actual
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email ?: ""
        txtUser.text = email

        // --- LÓGICA DE ADMIN ---
        if (email.lowercase().contains("admin")) {
            btnAdmin.visibility = View.VISIBLE
            btnAdmin.setOnClickListener {
                findNavController().navigate(R.id.adminFragment)
            }
        } else {
            btnAdmin.visibility = View.GONE
        }

        // --- LÓGICA MODO OSCURO ---
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        switchDark.isChecked = currentNightMode == Configuration.UI_MODE_NIGHT_YES

        switchDark.setOnCheckedChangeListener { _, isChecked ->
            val mode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(mode)
        }

        // --- LÓGICA CERRAR SESIÓN (MODIFICADA) ---
        btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Cerrar Sesión")
                .setMessage("¿Seguro que quieres salir?")
                .setPositiveButton("Salir") { _, _ ->
                    // 1. Cerrar sesión en Firebase
                    FirebaseAuth.getInstance().signOut()

                    // 2. Redirigir a la IntroActivity (Pantalla del Edificio)
                    val intent = Intent(requireContext(), IntroActivity::class.java)

                    // 3. Limpiar la pila de actividades (Borra el historial para no poder volver atrás)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                    // 4. Iniciar y cerrar la actual
                    startActivity(intent)
                    requireActivity().finish()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }
}