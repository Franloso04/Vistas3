package com.example.vistas

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.vistas.data.api.LoginRequest
import com.example.vistas.data.api.RetrofitClient
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.screen_log_empl) {

    // Usamos el ViewModel compartido
    private val viewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. REFERENCIAS (Usando findViewById, nada de binding)
        // Asegúrate de que estos IDs coinciden con tu XML 'screen_log_empl.xml'
        val editEmail = view.findViewById<TextInputEditText>(R.id.editEmail)
        val editPass = view.findViewById<TextInputEditText>(R.id.editPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnIniciarSesion)

        // Protección contra nulos
        if (editEmail == null || editPass == null || btnLogin == null) {
            return
        }

        // 2. LISTENER DEL BOTÓN
        btnLogin.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val pass = editPass.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(context, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. LLAMADA A LA API (Con Corutinas)
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    // A) Llamamos a tu servidor
                    val respuesta = RetrofitClient.instance.login(LoginRequest(email, pass))

                    // B) Si la respuesta es correcta y trae un empleado...
                    val empleado = respuesta.empleado
                    if (empleado != null) {
                        // Guardamos la sesión en la App
                        viewModel.setEmpleadoSesion(empleado)

                        Toast.makeText(context, "Bienvenido ${empleado.nombre}", Toast.LENGTH_SHORT).show()

                        // C) Navegamos al Dashboard
                        // NOTA: Revisa que esta flecha exista en tu navigation.xml
                        // Si te da error al navegar, prueba con R.id.action_loginFragment_to_mainActivity
                        findNavController().navigate(R.id.action_login_to_dashboard)
                    }

                } catch (e: Exception) {
                    // D) Control de errores (Servidor caído, contraseña mal, etc.)
                    e.printStackTrace()
                    Toast.makeText(context, "Error de acceso: Credenciales inválidas o fallo de red", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}