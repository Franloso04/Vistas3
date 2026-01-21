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

        // Referencias a la UI
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerHistorial)
        val btnEscanear = view.findViewById<View>(R.id.btnEscanear)
        val searchBar = view.findViewById<EditText>(R.id.searchBar)

        // Botones de Acción (Eliminar / Seleccionar)
        val txtSeleccionar = view.findViewById<TextView>(R.id.btnSeleccionar)
        val btnEliminar = view.findViewById<Button>(R.id.btnEliminarVarios)

        // Chips de Filtros
        val chipFechas = view.findViewById<Chip>(R.id.chipFechas)
        val chipCategoria = view.findViewById<Chip>(R.id.chipCategoria)
        val chipEstado = view.findViewById<Chip>(R.id.chipEstado)

        // Configurar Adapter
        adapter = GastoAdapter(emptyList(), isSelectionMode = false) {
            // Callback: Se ejecuta cada vez que tocas un checkbox
            val count = adapter.getSelectedCount()
            if (count > 0) {
                btnEliminar.visibility = View.VISIBLE
                btnEliminar.text = "Eliminar ($count)"
                btnEscanear.visibility = View.GONE
            } else {
                btnEliminar.visibility = View.GONE
                // Mantenemos oculto escanear si estamos en modo selección
            }
        }

        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = adapter

        // --- IMPORTANTE: Observar datos FILTRADOS (Para no romper filtros) ---
        viewModel.gastosFiltrados.observe(viewLifecycleOwner) { lista ->
            adapter.updateData(lista)
        }

        // --- LÓGICA DE FILTROS (INTACTA) ---

        // 1. Buscador de Texto
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { viewModel.filtrarPorTexto(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 2. Filtro Categoría
        chipCategoria.setOnClickListener {
            val popup = PopupMenu(requireContext(), chipCategoria)
            listOf("Todas", "Comida", "Transporte", "Alojamiento", "Suministros").forEach { popup.menu.add(it) }
            popup.setOnMenuItemClickListener { item ->
                val cat = item.title.toString()
                viewModel.filtrarPorCategoria(cat)
                chipCategoria.text = if (cat == "Todas") "Categoría" else cat
                true
            }
            popup.show()
        }

        // 3. Filtro Estado
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

        // 4. Filtro Fechas
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

        // --- LÓGICA DE ELIMINAR (MODO SELECCIÓN) ---

        // Botón "Seleccionar" (Arriba a la derecha)
        txtSeleccionar.setOnClickListener {
            val nuevoModo = !adapter.isSelectionMode
            adapter.activarModoSeleccion(nuevoModo) // Activa los checkboxes

            txtSeleccionar.text = if (nuevoModo) "Cancelar" else "Seleccionar"

            if (nuevoModo) {
                btnEscanear.visibility = View.GONE // Ocultar cámara
            } else {
                btnEscanear.visibility = View.VISIBLE // Mostrar cámara
                btnEliminar.visibility = View.GONE
            }
        }

        // Botón Rojo "Eliminar" (Abajo)
        btnEliminar.setOnClickListener {
            val count = adapter.getSelectedCount()
            AlertDialog.Builder(requireContext())
                .setTitle("¿Eliminar tickets?")
                .setMessage("Vas a borrar $count gastos permanentemente.")
                .setPositiveButton("Eliminar") { _, _ ->
                    val ids = adapter.getSelectedIds()
                    viewModel.eliminarGastosSeleccionados(ids) // Borra de Firebase

                    // Resetear UI
                    adapter.activarModoSeleccion(false)
                    txtSeleccionar.text = "Seleccionar"
                    btnEliminar.visibility = View.GONE
                    btnEscanear.visibility = View.VISIBLE
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        // Navegación OCR
        btnEscanear.setOnClickListener { findNavController().navigate(R.id.ocrFragment) }
    }
}