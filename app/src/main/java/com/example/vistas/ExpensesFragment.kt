package com.example.vistas

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ExpensesFragment : Fragment(R.layout.screen_hist_gast) {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: GastoAdapter

    // Referencias a los botones nuevos
    private lateinit var btnMode: FloatingActionButton // El pequeño
    private lateinit var btnAction: ExtendedFloatingActionButton // El grande

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerHistorial)
        val searchBar = view.findViewById<EditText>(R.id.searchBar)
        val chipFechas = view.findViewById<Chip>(R.id.chipFechas)
        val chipCategoria = view.findViewById<Chip>(R.id.chipCategoria)
        val chipEstado = view.findViewById<Chip>(R.id.chipEstado)

        // Enlazar botones
        btnMode = view.findViewById(R.id.btnModeSwitch)
        btnAction = view.findViewById(R.id.btnMainAction)

        // Configurar Adapter
        adapter = GastoAdapter(emptyList(), isSelectionMode = false) {
            actualizarBotonAccion() // Actualizar texto del botón grande al seleccionar
        }

        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = adapter

        viewModel.gastosFiltrados.observe(viewLifecycleOwner) { lista ->
            adapter.updateData(lista)
        }

        // --- LISTENERS DE LOS BOTONES ---

        // 1. Botón Pequeño (Cambia de modo)
        btnMode.setOnClickListener {
            val nuevoModo = !adapter.isSelectionMode
            adapter.activarModoSeleccion(nuevoModo)
            actualizarInterfazModo(nuevoModo)
        }

        // 2. Botón Grande (Acción variable)
        btnAction.setOnClickListener {
            if (adapter.isSelectionMode) {
                // MODO SELECCIÓN -> Acción: ELIMINAR
                ejecutarEliminacion()
            } else {
                // MODO NORMAL -> Acción: ESCANEAR
                findNavController().navigate(R.id.ocrFragment)
            }
        }

        // Filtros (Igual que antes)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { viewModel.filtrarPorTexto(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        chipCategoria.setOnClickListener { setupChipPopup(chipCategoria, listOf("Todas", "Comida", "Transporte", "Alojamiento", "Suministros")) { viewModel.filtrarPorCategoria(it) } }
        chipEstado.setOnClickListener { setupChipPopup(chipEstado, listOf("Todos", "APROBADO", "PENDIENTE", "RECHAZADO", "PROCESANDO")) { viewModel.filtrarPorEstado(it) } }
        chipFechas.setOnClickListener { val popup = PopupMenu(requireContext(), chipFechas); popup.menu.add("Más recientes"); popup.menu.add("Más antiguos"); popup.setOnMenuItemClickListener { viewModel.ordenarPorFecha(it.title == "Más recientes"); true }; popup.show() }

        // Estado inicial
        actualizarInterfazModo(false)
    }

    private fun actualizarInterfazModo(esModoSeleccion: Boolean) {
        val context = requireContext()

        if (esModoSeleccion) {
            // --- ESTADO: SELECCIONANDO ---

            // Botón Pequeño: Se convierte en "Cancelar" (X)
            btnMode.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)

            // Botón Grande: Se convierte en "Eliminar" (Rojo)
            btnAction.text = "Eliminar"
            btnAction.icon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_delete)
            btnAction.backgroundTintList = ContextCompat.getColorStateList(context, R.color.status_rejected_text) // Rojo
            btnAction.shrink() // Empezamos encogido hasta que seleccione algo (opcional, o extendido "Eliminar (0)")
            btnAction.extend()
            actualizarBotonAccion()

        } else {
            // --- ESTADO: NORMAL ---

            // Botón Pequeño: Se convierte en "Editar/Seleccionar" (Lápiz)
            btnMode.setImageResource(android.R.drawable.ic_menu_edit)

            // Botón Grande: Se convierte en "Escanear" (Azul)
            btnAction.text = "Escanear"
            btnAction.icon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_camera)
            btnAction.backgroundTintList = ContextCompat.getColorStateList(context, R.color.primary_blue)
            btnAction.extend()
        }
    }

    private fun actualizarBotonAccion() {
        if (!adapter.isSelectionMode) return

        val count = adapter.getSelectedCount()
        if (count > 0) {
            btnAction.text = "Eliminar ($count)"
            btnAction.show()
        } else {
            btnAction.text = "Eliminar"
            // Opcional: btnAction.hide() si quieres que desaparezca cuando no hay nada seleccionado
        }
    }

    private fun ejecutarEliminacion() {
        val count = adapter.getSelectedCount()
        if (count == 0) return

        AlertDialog.Builder(requireContext())
            .setTitle("¿Eliminar tickets?")
            .setMessage("Vas a borrar $count gastos permanentemente.")
            .setPositiveButton("Eliminar") { _, _ ->
                val ids = adapter.getSelectedIds()
                viewModel.eliminarGastosSeleccionados(ids)

                adapter.activarModoSeleccion(false)
                actualizarInterfazModo(false)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setupChipPopup(chip: Chip, items: List<String>, onSelect: (String) -> Unit) {
        val popup = PopupMenu(requireContext(), chip)
        items.forEach { popup.menu.add(it) }
        popup.setOnMenuItemClickListener {
            val selected = it.title.toString()
            onSelect(selected)
            chip.text = if (selected == items[0]) chip.tag.toString() else selected
            true
        }
        popup.show()
    }
}