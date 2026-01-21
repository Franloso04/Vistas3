package com.example.vistas

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vistas.model.EstadoGasto

class AdminFragment : Fragment(R.layout.screen_admin) {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: AdminAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerAdmin)
        val txtEmpty = view.findViewById<TextView>(R.id.txtEmptyState)

        // AHORA ESTO FUNCIONARÁ PORQUE EL ADAPTER YA ESPERA 'onEliminar'
        adapter = AdminAdapter(
            lista = emptyList(),
            onAprobar = { gasto ->
                viewModel.aprobarGasto(gasto.id)
                Toast.makeText(context, "Ticket Aprobado", Toast.LENGTH_SHORT).show()
            },
            onRechazar = { gasto ->
                viewModel.rechazarGasto(gasto.id)
                Toast.makeText(context, "Ticket Rechazado", Toast.LENGTH_SHORT).show()
            },
            onEliminar = { gasto ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar Ticket")
                    .setMessage("¿Estás seguro de borrar este ticket permanentemente?")
                    .setPositiveButton("Borrar") { _, _ ->
                        viewModel.eliminarGastoIndividual(gasto.id)
                        Toast.makeText(context, "Eliminado", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )

        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = adapter

        // Observar datos del ViewModel
        viewModel.gastosGlobales.observe(viewLifecycleOwner) { lista ->
            // Filtramos solo pendientes/procesando para el admin
            val pendientes = lista.filter {
                it.estado == EstadoGasto.PENDIENTE || it.estado == EstadoGasto.PROCESANDO
            }
            adapter.updateData(pendientes)

            if (pendientes.isEmpty()) {
                recycler.visibility = View.GONE
                txtEmpty?.visibility = View.VISIBLE
                txtEmpty?.text = "No hay solicitudes pendientes."
            } else {
                recycler.visibility = View.VISIBLE
                txtEmpty?.visibility = View.GONE
            }
        }
    }
}