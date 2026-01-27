package com.example.vistas

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vistas.model.EstadoGasto

class AdminFragment : Fragment(R.layout.screen_admin) {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapterGastos: AdminAdapter
    private lateinit var adapterReportes: ReportAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView Gastos Pendientes
        val recyclerGastos = view.findViewById<RecyclerView>(R.id.recyclerPendientes)
        recyclerGastos.layoutManager = LinearLayoutManager(context)
        adapterGastos = AdminAdapter(
            lista = emptyList(),
            onAprobar = { viewModel.aprobarGasto(it.id) },
            onRechazar = { viewModel.rechazarGasto(it.id) },
            onEliminar = { viewModel.eliminarGastoIndividual(it.id) }
        )
        recyclerGastos.adapter = adapterGastos


        val recyclerReportes = view.findViewById<RecyclerView>(R.id.recyclerReportes)
        if (recyclerReportes != null) {
            recyclerReportes.layoutManager = LinearLayoutManager(context)
            adapterReportes = ReportAdapter(emptyList()) { viewModel.eliminarReporte(it.id) }
            recyclerReportes.adapter = adapterReportes

            viewModel.reportes.observe(viewLifecycleOwner) { adapterReportes.updateList(it) }
        }

        viewModel.gastosGlobales.observe(viewLifecycleOwner) { lista ->
            val pendientes = lista.filter { it.estado == EstadoGasto.PENDIENTE }
            adapterGastos.updateList(pendientes)
        }
    }
}