package com.example.vistas

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
// Asegúrate de importar tu Adaptador si está en otro paquete, ej: com.example.vistas.adapter.GastoAdapter

class ExpensesFragment : Fragment(R.layout.screen_hist_gast) {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: GastoAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Referencias a las vistas (Coinciden con el XML de arriba)
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerHistorial)
        val btnEscanear = view.findViewById<View>(R.id.btnEscanear)
        val searchBar = view.findViewById<EditText>(R.id.searchBar)
        val txtSeleccionar = view.findViewById<TextView>(R.id.btnSeleccionar)
        val btnEliminar = view.findViewById<Button>(R.id.btnEliminarVarios)

        // Configurar Adapter
        adapter = GastoAdapter(emptyList(), isSelectionMode = false) {
            // Callback: Se ejecuta cada vez que tocas un checkbox
            val count = adapter.getSelectedCount() // Esta función ya existe en GastoAdapter
            if (count > 0) {
                btnEliminar.visibility = View.VISIBLE
                btnEliminar.text = "Eliminar seleccionados ($count)"
                btnEscanear.visibility = View.GONE // Ocultamos escanear para no tapar
            } else {
                btnEliminar.visibility = View.GONE
                btnEscanear.visibility = View.VISIBLE
            }
        }

        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = adapter

        // Observar datos del ViewModel (Fuente de verdad)
        viewModel.gastos.observe(viewLifecycleOwner) { lista ->
            adapter.updateData(lista)
        }

        // Navegación
        btnEscanear.setOnClickListener {
            findNavController().navigate(R.id.ocrFragment)
        }

        // Buscador
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val texto = s.toString()
                val listaFiltrada = viewModel.filtrarGastos(texto)
                adapter.updateData(listaFiltrada)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Botón Seleccionar / Cancelar
        txtSeleccionar.setOnClickListener {
            // Accedemos a la propiedad pública isSelectionMode
            val nuevoModo = !adapter.isSelectionMode
            adapter.setSelectionMode(nuevoModo)

            txtSeleccionar.text = if (nuevoModo) "Cancelar" else "Seleccionar"

            if (!nuevoModo) {
                btnEliminar.visibility = View.GONE
                btnEscanear.visibility = View.VISIBLE
            }
        }

        // Acción de Eliminar
        btnEliminar.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("¿Eliminar gastos?")
                .setMessage("Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar") { _, _ ->
                    val ids = adapter.getSelectedIds() // Esta función ya existe en GastoAdapter
                    viewModel.eliminarGastosSeleccionados(ids)

                    // Salir modo selección
                    adapter.setSelectionMode(false)
                    txtSeleccionar.text = "Seleccionar"
                    btnEliminar.visibility = View.GONE
                    btnEscanear.visibility = View.VISIBLE
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }
}