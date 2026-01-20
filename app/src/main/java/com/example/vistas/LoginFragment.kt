package com.example.vistas

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class LoginFragment : Fragment(R.layout.screen_log_empl) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnLogin = view.findViewById<Button>(R.id.btnIniciarSesion)

        btnLogin.setOnClickListener {
            // Simulación de Login: Por ahora no validamos, solo navegamos.
            // Más adelante aquí conectaremos Firebase Auth.
            findNavController().navigate(R.id.action_login_to_dashboard)

            Toast.makeText(requireContext(), "Bienvenido", Toast.LENGTH_SHORT).show()
        }
    }
}