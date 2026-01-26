package com.example.vistas

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vistas.model.Gasto
import com.example.vistas.model.Reporte

class AdminFragment : Fragment(R.layout.screen_admin) {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapterGastos: AdminAdapter
    private lateinit var adapterReportes: ReportAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup Recycler Gastos
        val recyclerGastos = view.findViewById<RecyclerView>(R.id.recyclerPendientes)
        recyclerGastos.layoutManager = LinearLayoutManager(context)

        adapterGastos = AdminAdapter(
            lista = emptyList(),
            onAprobar = { g -> viewModel.aprobarGasto(g.id) },
            onRechazar = { g -> viewModel.rechazarGasto(g.id) },
            onEliminar = { g -> viewModel.eliminarGastoIndividual(g.id) }
        )
        recyclerGastos.adapter = adapterGastos

        // Setup Recycler Reportes
        val recyclerReportes = view.findViewById<RecyclerView>(R.id.recyclerReportes)
        recyclerReportes.layoutManager = LinearLayoutManager(context)

        adapterReportes = ReportAdapter(
            lista = emptyList(),
            onEliminar = { r -> viewModel.eliminarReporte(r.id) }
        )
        recyclerReportes.adapter = adapterReportes

        // Observers
        viewModel.gastosGlobales.observe(viewLifecycleOwner) { lista ->
            // Filtramos solo pendientes para validar
            val pendientes = lista.filter { it.estado.name == "PENDIENTE" }
            adapterGastos.updateList(pendientes)
        }

        viewModel.reportes.observe(viewLifecycleOwner) { lista ->
            adapterReportes.updateList(lista)
        }
    }
}