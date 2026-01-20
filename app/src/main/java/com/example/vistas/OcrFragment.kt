package com.example.vistas

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vistas.data.FakeRepository
import com.example.vistas.model.Gasto
import com.example.vistas.model.EstadoGasto
import java.util.UUID

class OcrFragment : Fragment(R.layout.screen_val_tick) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editNombre = view.findViewById<EditText>(R.id.editNombre)
        val btnConfirmar = view.findViewById<Button>(R.id.btnConfirmar)

        btnConfirmar.setOnClickListener {
            val nombre = editNombre.text.toString()

            if (nombre.isNotEmpty()) {
                // Creamos el objeto real
                val nuevoGasto = Gasto(
                    id = UUID.randomUUID().toString(),
                    nombreComercio = nombre,
                    fecha = "Hoy, 12:00 PM",
                    categoria = "Comida",
                    monto = 15.50,
                    estado = EstadoGasto.PROCESANDO
                )

                // Lo guardamos en el repositorio
                FakeRepository.addGasto(nuevoGasto)

                Toast.makeText(requireContext(), "Ticket guardado", Toast.LENGTH_SHORT).show()

                // Volvemos al historial de gastos
                findNavController().popBackStack()
            }
        }
    }
}