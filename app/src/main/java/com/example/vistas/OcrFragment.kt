package com.example.vistas

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
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
        val editMonto = view.findViewById<EditText>(R.id.editMonto)
        val btnConfirmar = view.findViewById<Button>(R.id.btnConfirmar)

        btnConfirmar.setOnClickListener {
            val nuevoGasto = Gasto(
                id = UUID.randomUUID().toString(),
                nombreComercio = editNombre.text.toString(),
                fecha = "Hoy, 10:00 AM",
                categoria = "Comidas y Entretenimiento",
                monto = 15.50, // En producción parsear editMonto
                estado = EstadoGasto.PROCESANDO
            )

            FakeRepository.addGasto(nuevoGasto)

            // Regresamos al historial
            findNavController().popBackStack()
        }
    }
}