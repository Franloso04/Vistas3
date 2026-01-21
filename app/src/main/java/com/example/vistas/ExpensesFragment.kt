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

        // Referencias
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerHistorial)
        val btnEscanear = view.findViewById<Button>(R.id.btnEscanear)
        val btnEliminar = view.findViewById<Button>(R.id.btnEliminarVarios)
        val searchBar = view.findViewById<EditText>(R.id.searchBar)
        val txtSeleccionar = view.findViewById<TextView>(R.id.btnSeleccionar)

        // Chips
        val chipFechas = view.findViewById<Chip>(R.id.chipFechas)
        val chipCategoria = view.findViewById<Chip>(R.id.chipCategoria)
        val chipEstado = view.findViewById<Chip>(R.id.chipEstado)

        // Configurar Adapter
        adapter = GastoAdapter(emptyList(), isSelectionMode = false) {
            // Callback selección
            val count = adapter.getSelectedCount()
            if (count > 0) {
                btnEliminar.visibility = View.VISIBLE
                btnEliminar.text = "Eliminar ($count)"
                btnEscanear.visibility = View.GONE
            } else {
                btnEliminar.text = "Eliminar"
                // Mantenemos btnEliminar visible si estamos en modo selección, aunque count sea 0,
                // o podemos ocultarlo. La lógica original ocultaba btnEliminar al salir del modo selección.
                // Aquí, si estamos en modo selección y count es 0, podríamos querer que se vea el botón de eliminar deshabilitado o similar.
                // Para simplificar y seguir la lógica de "toggle", dejemos que el botón "Seleccionar" controle la visibilidad principal.
            }
        }

        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = adapter

        // Observar datos
        viewModel.gastosFiltrados.observe(viewLifecycleOwner) { lista ->
            adapter.updateData(lista)
        }

        // --- LÓGICA DE FILTROS ---
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { viewModel.filtrarPorTexto(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        chipCategoria.setOnClickListener {
            val popup = PopupMenu(requireContext(), chipCategoria)
            listOf("Todas", "Comida", "Transporte", "Alojamiento", "Suministros").forEach { popup.menu.add(it) }
            popup.setOnMenuItemClickListener {
                viewModel.filtrarPorCategoria(it.title.toString())
                chipCategoria.text = if (it.title == "Todas") "Categoría" else it.title
                true
            }
            popup.show()
        }

        chipEstado.setOnClickListener {
            val popup = PopupMenu(requireContext(), chipEstado)
            listOf("Todos", "APROBADO", "PENDIENTE", "RECHAZADO", "PROCESANDO").forEach { popup.menu.add(it) }
            popup.setOnMenuItemClickListener {
                viewModel.filtrarPorEstado(it.title.toString())
                chipEstado.text = if (it.title == "Todos") "Estado" else it.title
                true
            }
            popup.show()
        }

        chipFechas.setOnClickListener {
            val popup = PopupMenu(requireContext(), chipFechas)
            popup.menu.add("Más recientes")
            popup.menu.add("Más antiguos")
            popup.setOnMenuItemClickListener {
                viewModel.ordenarPorFecha(it.title == "Más recientes")
                true
            }
            popup.show()
        }

        // --- LÓGICA DE SELECCIÓN Y BORRADO ---

        // Botón "Seleccionar" (Arriba derecha)
        txtSeleccionar.setOnClickListener {
            val nuevoModo = !adapter.isSelectionMode
            adapter.activarModoSeleccion(nuevoModo)

            txtSeleccionar.text = if (nuevoModo) "Cancelar" else "Seleccionar"

            if (nuevoModo) {
                // Entrando a modo selección
                btnEscanear.visibility = View.GONE
                btnEliminar.visibility = View.VISIBLE
                btnEliminar.text = "Eliminar"
            } else {
                // Saliendo de modo selección
                btnEscanear.visibility = View.VISIBLE
                btnEliminar.visibility = View.GONE
            }
        }

        // Botón "Eliminar" (Abajo Rojo)
        btnEliminar.setOnClickListener {
            val count = adapter.getSelectedCount()
            if (count == 0) return@setOnClickListener

            AlertDialog.Builder(requireContext())
                .setTitle("¿Eliminar tickets?")
                .setMessage("Vas a borrar $count gastos permanentemente.")
                .setPositiveButton("Eliminar") { _, _ ->
                    val ids = adapter.getSelectedIds()
                    viewModel.eliminarGastosSeleccionados(ids)

                    // Resetear interfaz
                    adapter.activarModoSeleccion(false)
                    txtSeleccionar.text = "Seleccionar"
                    btnEliminar.visibility = View.GONE
                    btnEscanear.visibility = View.VISIBLE
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        btnEscanear.setOnClickListener { findNavController().navigate(R.id.ocrFragment) }
    }
}