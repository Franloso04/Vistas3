package com.example.vistas

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.vistas.model.EstadoGasto
import com.example.vistas.model.Gasto
import java.util.UUID

class OcrFragment : Fragment(R.layout.screen_val_tick) {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editNombre = view.findViewById<EditText>(R.id.editNombre)
        val editMonto = view.findViewById<EditText>(R.id.editMonto)
        val spinnerCategoria = view.findViewById<Spinner>(R.id.spinnerCategoria) // Asegúrate de tener este ID en el XML
        val btnConfirmar = view.findViewById<Button>(R.id.btnConfirmar) // Ajusta el ID según tu XML (btnConfirmar o btnConfirmarGasto)

        // 1. Rellenar el Spinner de Categorías
        val categorias = listOf("Comida", "Transporte", "Alojamiento", "Suministros", "Equipamiento")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categorias)
        spinnerCategoria.adapter = adapter

        // 2. Guardar Gasto
        btnConfirmar.setOnClickListener {
            val nombre = editNombre.text.toString()
            // Limpiamos el símbolo $ si el usuario lo pone
            val montoString = editMonto.text.toString().replace("$", "").trim()
            val monto = montoString.toDoubleOrNull() ?: 0.0
            val categoriaSeleccionada = spinnerCategoria.selectedItem.toString()

            if (nombre.isNotBlank() && monto > 0) {
                val nuevoGasto = Gasto(
                    id = UUID.randomUUID().toString(),
                    nombreComercio = nombre,
                    fecha = "Hoy", // Podrías usar DateFormat aquí
                    categoria = categoriaSeleccionada, // ¡AQUÍ GUARDAMOS LA CATEGORÍA REAL!
                    monto = monto,
                    estado = EstadoGasto.PROCESANDO
                )

                viewModel.agregarGasto(nuevoGasto)
                Toast.makeText(context, "Ticket guardado", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(context, "Revisa el nombre y el monto", Toast.LENGTH_SHORT).show()
            }
        }
    }
}