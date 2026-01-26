package com.example.vistas

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController

class LoginFragment : Fragment(R.layout.screen_log_empl) {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etEmail = view.findViewById<EditText>(R.id.editEmail)
        val etPass = view.findViewById<EditText>(R.id.editPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnIniciarSesion)

        // Observar si el login fue exitoso para navegar
        viewModel.empleadoSesion.observe(viewLifecycleOwner) { empleado ->
            if (empleado != null) {
                findNavController().navigate(R.id.action_login_to_dashboard) // Asegúrate que este ID existe en tu nav_graph
            }
        }

        // Observar mensajes de error/éxito
        viewModel.mensajeOp.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                viewModel.limpiarMensaje()
            }
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPass.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                viewModel.realizarLogin(email, pass)
            } else {
                Toast.makeText(context, "Rellena los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}