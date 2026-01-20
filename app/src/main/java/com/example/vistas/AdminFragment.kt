package com.example.vistas

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vistas.model.EstadoGasto

// Asegúrate de tener un layout 'screen_admin.xml' con un RecyclerView id: recyclerAdmin
class AdminFragment : Fragment(R.layout.screen_admin) {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: AdminAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerAdmin)
        val txtEmpty = view.findViewById<TextView>(R.id.txtEmptyState) // Opcional, si lo pones en el XML

        // Configurar el adaptador con las acciones
        adapter = AdminAdapter(
            lista = emptyList(),
            onAprobar = { gasto ->
                viewModel.aprobarGasto(gasto.id)
                Toast.makeText(context, "Ticket Aprobado", Toast.LENGTH_SHORT).show()
            },
            onRechazar = { gasto ->
                viewModel.rechazarGasto(gasto.id)
                Toast.makeText(context, "Ticket Rechazado", Toast.LENGTH_SHORT).show()
            }
        )

        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = adapter

        // --- CLAVE: El Admin observa TODOS los gastos globales ---
        viewModel.gastosGlobales.observe(viewLifecycleOwner) { lista ->

            // Filtramos: Solo mostramos lo que no está ni aprobado ni rechazado
            val pendientes = lista.filter {
                it.estado == EstadoGasto.PENDIENTE || it.estado == EstadoGasto.PROCESANDO
            }

            adapter.updateData(pendientes)

            // Manejo de estado vacío (si txtEmpty existe en tu XML)
            if (pendientes.isEmpty()) {
                recycler.visibility = View.GONE
                txtEmpty?.visibility = View.VISIBLE
                txtEmpty?.text = "¡Todo al día! No hay tickets pendientes."
            } else {
                recycler.visibility = View.VISIBLE
                txtEmpty?.visibility = View.GONE
            }
        }
    }
}