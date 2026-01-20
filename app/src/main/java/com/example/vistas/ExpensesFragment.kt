package com.example.vistas

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip

class ExpensesFragment : Fragment(R.layout.screen_hist_gast) {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: GastoAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Vinculación de Vistas
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerHistorial)
        val btnEscanear = view.findViewById<View>(R.id.btnEscanear)
        val searchBar = view.findViewById<EditText>(R.id.searchBar)
        val txtSeleccionar = view.findViewById<TextView>(R.id.btnSeleccionar)
        val btnEliminar = view.findViewById<Button>(R.id.btnEliminarVarios)
        val chipCategoria = view.findViewById<Chip>(R.id.chipCategoria)

        // Configuración del RecyclerView
        adapter = GastoAdapter(emptyList(), isSelectionMode = false) {
            val count = adapter.getSelectedCount()
            if (count > 0) {
                btnEliminar.visibility = View.VISIBLE
                btnEliminar.text = "Eliminar seleccionados ($count)"
                btnEscanear.visibility = View.GONE
            } else {
                btnEliminar.visibility = View.GONE
                btnEscanear.visibility = View.VISIBLE
            }
        }

        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = adapter

        // Observar datos del ViewModel
        viewModel.gastos.observe(viewLifecycleOwner) { lista ->
            adapter.updateData(lista)
        }

        // Buscador de Texto
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.filtrarPorTexto(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // --- FILTRO DE CATEGORÍA (Menú Desplegable) ---
        chipCategoria.setOnClickListener {
            val popup = PopupMenu(requireContext(), chipCategoria)
            popup.menu.add("Todas")
            popup.menu.add("Comida")
            popup.menu.add("Transporte")
            popup.menu.add("Alojamiento")
            popup.menu.add("Suministros")
            popup.menu.add("Equipamiento")

            popup.setOnMenuItemClickListener { item ->
                val cat = item.title.toString()
                chipCategoria.text = if (cat == "Todas") "Categoría" else cat
                viewModel.filtrarPorCategoria(cat) // Llamada al ViewModel
                true
            }
            popup.show()
        }

        // Navegación OCR
        btnEscanear.setOnClickListener {
            findNavController().navigate(R.id.ocrFragment)
        }

        // Botón Seleccionar
        txtSeleccionar.setOnClickListener {
            val nuevoModo = !adapter.isSelectionMode
            adapter.activarModoSeleccion(nuevoModo)
            txtSeleccionar.text = if (nuevoModo) "Cancelar" else "Seleccionar"

            if (!nuevoModo) {
                btnEliminar.visibility = View.GONE
                btnEscanear.visibility = View.VISIBLE
            }
        }

        // Botón Eliminar
        btnEliminar.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("¿Eliminar gastos?")
                .setMessage("Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar") { _, _ ->
                    val ids = adapter.getSelectedIds()
                    viewModel.eliminarGastosSeleccionados(ids)
                    adapter.activarModoSeleccion(false)
                    txtSeleccionar.text = "Seleccionar"
                    btnEliminar.visibility = View.GONE
                    btnEscanear.visibility = View.VISIBLE
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }
}