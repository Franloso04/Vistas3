package com.example.vistas

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
import com.example.vistas.model.Gasto

class ReportsFragment : Fragment(R.layout.screen_report) {

    private val viewModel: MainViewModel by activityViewModels()

    // Referencias
    private lateinit var dropdownMenu: AutoCompleteTextView
    private var listaGastosActual: List<Gasto> = emptyList()
    private var gastoSeleccionado: Gasto? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Vinculamos las vistas con el XML nuevo
        // Fíjate que buscamos 'autoCompleteTickets', no 'spinnerTickets'
        dropdownMenu = view.findViewById(R.id.autoCompleteTickets)
        val etDescripcion = view.findViewById<EditText>(R.id.etDescripcion)
        val btnEnviar = view.findViewById<Button>(R.id.btnEnviarReporte)

        // 2. Cargamos los tickets en el menú desplegable
        viewModel.gastosFiltrados.observe(viewLifecycleOwner) { gastos ->
            listaGastosActual = gastos

            if (gastos.isEmpty()) {
                dropdownMenu.setText("No tienes tickets recientes", false)
                dropdownMenu.isEnabled = false // Deshabilitar si no hay tickets
            } else {
                dropdownMenu.isEnabled = true
                dropdownMenu.setText("Toca para seleccionar...", false)

                // Texto bonito para la lista
                val opciones = gastos.map {
                    "${it.nombreComercio} ($${it.importe}) - ${it.fecha}"
                }

                // Configuramos el adaptador
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, opciones)
                dropdownMenu.setAdapter(adapter)

                // Guardamos qué gasto se ha tocado
                dropdownMenu.setOnItemClickListener { _, _, position, _ ->
                    gastoSeleccionado = listaGastosActual[position]
                }
            }
        }

        // 3. BOTÓN ENVIAR: AHORA SÍ FUNCIONA
        btnEnviar.setOnClickListener {
            val descripcion = etDescripcion.text.toString().trim()

            if (gastoSeleccionado == null) {
                Toast.makeText(context, "Por favor, selecciona un ticket de la lista", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (descripcion.isEmpty()) {
                etDescripcion.error = "Describe el problema"
                return@setOnClickListener
            }

            // --- AQUÍ ESTÁ LA MAGIA ---
            // Llamamos a la función del ViewModel que creamos en el Paso 2
            viewModel.enviarReporteFirebase(gastoSeleccionado!!, descripcion)

            Toast.makeText(context, "Reporte enviado correctamente", Toast.LENGTH_LONG).show()

            // Volvemos atrás
            findNavController().navigateUp()
        }
    }
}