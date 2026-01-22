package com.example.vistas

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.vistas.model.EstadoGasto // Asegúrate de importar esto
import com.example.vistas.model.Gasto
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth // <--- IMPORTANTE
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class OcrFragment : Fragment(R.layout.fragment_ocr_validation) {

    private val viewModel: MainViewModel by activityViewModels()
    private val calendar = Calendar.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. REFERENCIAS
        val editNombre = view.findViewById<EditText>(R.id.editNombre)
        val editFecha = view.findViewById<TextInputEditText>(R.id.editFecha)
        val editMonto = view.findViewById<EditText>(R.id.editMonto)
        val menuCategoria = view.findViewById<AutoCompleteTextView>(R.id.autoCompleteCategoria)
        val btnConfirmar = view.findViewById<Button>(R.id.btnConfirmar)
        val btnDescartar = view.findViewById<Button>(R.id.btnDescartar)

        if (editNombre == null || menuCategoria == null || editFecha == null || editMonto == null) return

        // 2. LÓGICA FECHA
        actualizarCampoFecha(editFecha)
        editFecha.setOnClickListener { mostrarSelectorFecha(editFecha) }

        // 3. CONFIGURAR MENÚ
        val categorias = listOf("Comida", "Transporte", "Alojamiento", "Suministros", "Equipamiento")
        val adapter = ArrayAdapter(requireContext(), R.layout.item_dropdown_category, categorias)
        menuCategoria.setAdapter(adapter)
        menuCategoria.keyListener = null

        // 4. CONFIRMAR
        btnConfirmar.setOnClickListener {
            val nombre = editNombre.text.toString()
            val montoStr = editMonto.text.toString().replace("$", "").replace(",", ".").trim()
            val monto = montoStr.toDoubleOrNull() ?: 0.0
            val categoriaSeleccionada = menuCategoria.text.toString()

            // --- AÑADIDO: OBTENER USUARIO ACTUAL ---
            val currentUser = FirebaseAuth.getInstance().currentUser
            val myUid = currentUser?.uid ?: ""
            val myEmail = currentUser?.email ?: ""

            if (nombre.isNotBlank() && monto > 0.0) {
                val nuevoGasto = Gasto(
                    id = UUID.randomUUID().toString(),
                    nombreComercio = nombre,
                    fecha = editFecha.text.toString(),
                    categoria = categoriaSeleccionada,
                    monto = monto,
                    timestamp = calendar.timeInMillis,
                    imagenUrl = "",
                    estado = EstadoGasto.PENDIENTE, // Asegura el estado inicial

                    // --- AÑADIDO: GUARDAR QUIÉN LO CREÓ ---
                    userId = myUid,
                    emailUsuario = myEmail
                )

                viewModel.agregarGasto(nuevoGasto)
                Toast.makeText(context, "Ticket guardado", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(context, "Revisa los datos", Toast.LENGTH_SHORT).show()
            }
        }

        btnDescartar?.setOnClickListener { findNavController().popBackStack() }
    }

    // ... (resto de funciones de fecha igual) ...
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