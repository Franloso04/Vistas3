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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OcrFragment : Fragment(R.layout.screen_val_tick) {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editNombre = view.findViewById<EditText>(R.id.editNombre)
        val editMonto = view.findViewById<EditText>(R.id.editMonto)
        val spinner = view.findViewById<Spinner>(R.id.spinnerCategoria) // Asegúrate que el ID en XML sea este
        val btnConfirmar = view.findViewById<Button>(R.id.btnConfirmar) // O btnConfirmarGasto

        // Configurar Spinner
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item,
            listOf("Comida", "Transporte", "Alojamiento", "Suministros", "Equipamiento"))
        spinner.adapter = adapter

        btnConfirmar.setOnClickListener {
            val nombre = editNombre.text.toString()
            val montoStr = editMonto.text.toString().replace("$", "").trim()
            val monto = montoStr.toDoubleOrNull() ?: 0.0

            if (nombre.isNotBlank() && monto > 0) {
                // Crear fecha legible
                val fechaHoy = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

                val nuevoGasto = Gasto(
                    id = UUID.randomUUID().toString(),
                    nombreComercio = nombre,
                    fecha = fechaHoy,
                    categoria = spinner.selectedItem.toString(),
                    monto = monto,
                    estado = EstadoGasto.PROCESANDO,
                    timestamp = System.currentTimeMillis()
                )

                // Guardar
                viewModel.agregarGasto(nuevoGasto)

                Toast.makeText(context, "Guardando en la nube...", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(context, "Revisa los datos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}