package com.example.vistas

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment(R.layout.screen_log_empl) {

    private val auth = FirebaseAuth.getInstance()
    // Conectamos con el mismo ViewModel que usa el resto de la app
    private val viewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Si ya estaba logueado de antes, avisamos al VM y entramos
        if (auth.currentUser != null) {
            viewModel.recargarSesion()
            navegarAlDestino()
            return
        }

        val editEmail = view.findViewById<TextInputEditText>(R.id.editEmail)
        val editPass = view.findViewById<TextInputEditText>(R.id.editPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnIniciarSesion)

        btnLogin.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val pass = editPass.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, pass)
                    .addOnSuccessListener {
                        // ¡IMPORTANTE! Aquí despertamos al ViewModel
                        viewModel.recargarSesion()
                        navegarAlDestino()
                    }
                    .addOnFailureListener {
                        // Comprobamos si el fragmento sigue activo antes de mostrar el Toast
                        if (isAdded) {
                            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(context, "Rellena los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navegarAlDestino() {
        // --- PROTECCIÓN CONTRA CRASHES ---
        // Si por lo que sea el fragmento ya no es parte de la pantalla
        // (porque el usuario se salió o la app se pausó), no hacemos nada.
        if (!isAdded || activity == null) return
        // ---------------------------------

        // Navega siempre al dashboard, el ViewModel ya sabe si eres admin o no
        // y cargará los datos correspondientes.
        findNavController().navigate(R.id.action_login_to_dashboard)
    }
}