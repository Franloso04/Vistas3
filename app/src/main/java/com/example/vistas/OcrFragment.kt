package com.example.vistas

import android.app.DatePickerDialog
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
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class OcrFragment : Fragment(R.layout.fragment_ocr_validation) {

    private val viewModel: MainViewModel by activityViewModels()
    private val calendar = Calendar.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- 1. REFERENCIAS CON SEGURIDAD ---
        // Usamos 'findViewById' pero si devuelve null (porque no está en el XML), la app no crashea inmediatamente.
        val editNombre = view.findViewById<EditText>(R.id.editNombre)
        val editFecha = view.findViewById<TextInputEditText>(R.id.editFecha)
        val editMonto = view.findViewById<EditText>(R.id.editMonto)
        val spinner = view.findViewById<Spinner>(R.id.spinnerCategoria)
        val btnConfirmar = view.findViewById<Button>(R.id.btnConfirmar)
        val btnDescartar = view.findViewById<Button>(R.id.btnDescartar)

        // Verificación de seguridad: Si falta alguna vista, avisamos y salimos
        if (editNombre == null || editFecha == null || editMonto == null || spinner == null || btnConfirmar == null) {
            Toast.makeText(context, "Error: Faltan elementos en el diseño XML", Toast.LENGTH_LONG).show()
            return
        }

        // --- 2. LÓGICA DE FECHA ---
        actualizarCampoFecha(editFecha)

        editFecha.setOnClickListener {
            mostrarSelectorFecha(editFecha)
        }

        // --- 3. SPINNER ---
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item,
            listOf("Comida", "Transporte", "Alojamiento", "Suministros", "Equipamiento"))
        spinner.adapter = adapter

        // --- 4. CONFIRMAR ---
        btnConfirmar.setOnClickListener {
            val nombre = editNombre.text.toString()
            val montoStr = editMonto.text.toString().replace("$", "").replace(",", ".").trim()
            val monto = montoStr.toDoubleOrNull() ?: 0.0

            if (nombre.isNotBlank() && monto > 0.0) {
                val nuevoGasto = Gasto(
                    id = UUID.randomUUID().toString(),
                    nombreComercio = nombre,
                    fecha = editFecha.text.toString(),
                    categoria = spinner.selectedItem.toString(),
                    monto = monto,
                    estado = EstadoGasto.PROCESANDO,
                    timestamp = calendar.timeInMillis
                )

                viewModel.agregarGasto(nuevoGasto)
                Toast.makeText(context, "Ticket guardado", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(context, "Revisa los datos", Toast.LENGTH_SHORT).show()
            }
        }

        // --- 5. DESCARTAR ---
        btnDescartar?.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun mostrarSelectorFecha(editText: EditText) {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                actualizarCampoFecha(editText)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun actualizarCampoFecha(editText: EditText) {
        val formato = "dd MMM yyyy"
        val simpleDateFormat = SimpleDateFormat(formato, Locale.getDefault())
        editText.setText(simpleDateFormat.format(calendar.time))
    }
}