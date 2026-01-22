package com.example.vistas

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView // Importante
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.vistas.model.EstadoGasto
import com.example.vistas.model.Gasto
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

// Asegúrate de que el layout sea el correcto (screen_val_tick o fragment_ocr_validation)
class OcrFragment : Fragment(R.layout.screen_val_tick) {

    private val viewModel: MainViewModel by activityViewModels()
    private val calendar = Calendar.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. REFERENCIAS
        val editNombre = view.findViewById<EditText>(R.id.editNombre)
        val editFecha = view.findViewById<TextInputEditText>(R.id.editFecha)
        val editMonto = view.findViewById<EditText>(R.id.editMonto)

        // CAMBIO: Usamos AutoCompleteTextView en lugar de Spinner
        val menuCategoria = view.findViewById<AutoCompleteTextView>(R.id.autoCompleteCategoria)

        val btnConfirmar = view.findViewById<Button>(R.id.btnConfirmar)
        val btnDescartar = view.findViewById<Button>(R.id.btnDescartar)

        // Seguridad
        if (editNombre == null || menuCategoria == null) {
            return // Evita crash si la vista no cargó bien
        }

        // 2. LÓGICA FECHA
        actualizarCampoFecha(editFecha)
        editFecha.setOnClickListener { mostrarSelectorFecha(editFecha) }

        // 3. CONFIGURAR MENÚ DE CATEGORÍAS (MODO OSCURO FIX)
        val categorias = listOf("Comida", "Transporte", "Alojamiento", "Suministros", "Equipamiento")

        // Usamos el layout personalizado 'item_dropdown_category' para que el texto se vea bien
        val adapter = ArrayAdapter(requireContext(), R.layout.item_dropdown_category, categorias)
        menuCategoria.setAdapter(adapter)

        // Evita que el teclado salga al pulsar la categoría
        menuCategoria.keyListener = null

        // 4. CONFIRMAR
        btnConfirmar.setOnClickListener {
            val nombre = editNombre.text.toString()
            val montoStr = editMonto.text.toString().replace("$", "").replace(",", ".").trim()
            val monto = montoStr.toDoubleOrNull() ?: 0.0

            // Obtenemos el texto directamente del menú (ya no es selectedItem)
            val categoriaSeleccionada = menuCategoria.text.toString()

            if (nombre.isNotBlank() && monto > 0.0) {
                val nuevoGasto = Gasto(
                    id = UUID.randomUUID().toString(),
                    nombreComercio = nombre,
                    fecha = editFecha.text.toString(),
                    categoria = categoriaSeleccionada, // Usamos la variable nueva
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

        // 5. DESCARTAR
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