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

        // 1. Vinculación de Vistas
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerHistorial)
        val btnEscanear = view.findViewById<View>(R.id.btnEscanear)
        val searchBar = view.findViewById<EditText>(R.id.searchBar)
        val txtSeleccionar = view.findViewById<TextView>(R.id.btnSeleccionar)
        val btnEliminar = view.findViewById<Button>(R.id.btnEliminarVarios)

        val chipFechas = view.findViewById<Chip>(R.id.chipFechas)
        val chipCategoria = view.findViewById<Chip>(R.id.chipCategoria)
        val chipEstado = view.findViewById<Chip>(R.id.chipEstado)

        // 2. Configuración del Adapter
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

        // 3. OBSERVAMOS LA LISTA FILTRADA (NO LA GLOBAL)
        // Esto soluciona que el Dashboard se modifique al filtrar aquí.
        viewModel.gastosFiltrados.observe(viewLifecycleOwner) { lista ->
            adapter.updateData(lista)
        }

        // 4. Configuración del Buscador
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { viewModel.filtrarPorTexto(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // --- 5. CONFIGURACIÓN DE FILTROS (CHIPS) ---

        // Filtro: Fechas
        chipFechas.setOnClickListener {
            val popup = PopupMenu(requireContext(), chipFechas)
            popup.menu.add("Más recientes")
            popup.menu.add("Más antiguos")

            popup.setOnMenuItemClickListener { item ->
                val esReciente = item.title == "Más recientes"
                viewModel.ordenarPorFecha(esReciente)
                chipFechas.text = item.title
                true
            }
            popup.show()
        }

        // Filtro: Categoría
        chipCategoria.setOnClickListener {
            val popup = PopupMenu(requireContext(), chipCategoria)
            // Añadimos las opciones que coinciden con el Spinner del OCR
            listOf("Todas", "Comida", "Transporte", "Alojamiento", "Suministros", "Equipamiento").forEach { popup.menu.add(it) }

            popup.setOnMenuItemClickListener { item ->
                val cat = item.title.toString()
                viewModel.filtrarPorCategoria(cat)
                chipCategoria.text = if (cat == "Todas") "Categoría" else cat
                true
            }
            popup.show()
        }

        // Filtro: Estado
        chipEstado.setOnClickListener {
            val popup = PopupMenu(requireContext(), chipEstado)
            listOf("Todos", "APROBADO", "PENDIENTE", "RECHAZADO", "PROCESANDO").forEach { popup.menu.add(it) }

            popup.setOnMenuItemClickListener { item ->
                val est = item.title.toString()
                viewModel.filtrarPorEstado(est)
                chipEstado.text = if (est == "Todos") "Estado" else est
                true
            }
            popup.show()
        }

        // 6. Navegación al OCR
        btnEscanear.setOnClickListener {
            findNavController().navigate(R.id.ocrFragment)
        }

        // 7. Lógica de Selección
        txtSeleccionar.setOnClickListener {
            val nuevoModo = !adapter.isSelectionMode
            adapter.activarModoSeleccion(nuevoModo)
            txtSeleccionar.text = if (nuevoModo) "Cancelar" else "Seleccionar"

            if (!nuevoModo) {
                btnEliminar.visibility = View.GONE
                btnEscanear.visibility = View.VISIBLE
            }
        }

        // 8. Lógica de Eliminación
        btnEliminar.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Eliminar gastos")
                .setMessage("¿Estás seguro de que quieres eliminar los tickets seleccionados?")
                .setPositiveButton("Eliminar") { _, _ ->
                    val ids = adapter.getSelectedIds()
                    viewModel.eliminarGastosSeleccionados(ids)

                    // Salir del modo selección tras borrar
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