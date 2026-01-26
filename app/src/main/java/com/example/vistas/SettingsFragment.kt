package com.example.vistas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.vistas.MainViewModel

class SettingsFragment : Fragment(R.layout.screen_settings) {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Referencias a UI (según screen_settings.xml)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        val tvUsuario = view.findViewById<TextView>(R.id.txtCurrentUser) 

        // Mostrar datos del empleado
        viewModel.empleadoSesion.observe(viewLifecycleOwner) { emp ->
            if (emp != null) {
                tvUsuario.text = emp.nombre
            }
        }

        btnLogout.setOnClickListener {
            // 1. Limpiar sesión en ViewModel
            viewModel.cerrarSesion()

            // 2. Volver al Login (IntroActivity)
            val intent = Intent(requireContext(), IntroActivity::class.java)
            // Limpiar pila de actividades para que no pueda volver atrás
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
