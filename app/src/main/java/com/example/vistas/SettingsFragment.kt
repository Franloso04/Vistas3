package com.example.vistas

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.vistas.MainViewModel
import com.google.android.material.materialswitch.MaterialSwitch

class SettingsFragment : Fragment(R.layout.screen_settings) {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        val tvUsuario = view.findViewById<TextView>(R.id.txtCurrentUser)
        val switchModo = view.findViewById<MaterialSwitch>(R.id.switchDarkMode)
        val btnAdmin = view.findViewById<Button>(R.id.btnAdminPanel) // Se queda en el XML pero lo ocultamos
        val btnSupport = view.findViewById<Button>(R.id.btnSupport)

        // Ocultar boton Admin
        btnAdmin.visibility = View.GONE

        //  Datos usuario
        viewModel.empleadoSesion.observe(viewLifecycleOwner) { emp ->
            if (emp != null) {
                tvUsuario.text = "${emp.nombre}\n${emp.email}"
            }
        }

        // Modo Claro
        val prefs = requireActivity().getSharedPreferences("AppConfig", Context.MODE_PRIVATE)
        val esOscuro = prefs.getBoolean("MODO_OSCURO", true)
        switchModo.isChecked = !esOscuro

        val parentLayout = switchModo.parent as? ViewGroup
        parentLayout?.children?.forEach { child ->
            if (child is TextView && child.text.toString().contains("Modo", true)) {
                child.text = "Modo Claro"
            }
        }

        switchModo.setOnCheckedChangeListener { _, isChecked ->
            val activarModoOscuro = !isChecked
            prefs.edit().putBoolean("MODO_OSCURO", activarModoOscuro).apply()
            if (activarModoOscuro) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        btnSupport.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf("soporte@carsmarobe.es"))
                putExtra(Intent.EXTRA_SUBJECT, "Incidencia App Gastos")
            }
            try {
                startActivity(Intent.createChooser(intent, "Enviar correo..."))
            } catch (e: Exception) {
                Toast.makeText(context, "No hay app de correo instalada", Toast.LENGTH_SHORT).show()
            }
        }

        //  Cerrar sesión
        btnLogout.setOnClickListener {
            viewModel.cerrarSesion()
            val intent = Intent(requireContext(), IntroActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}