package com.example.vistas

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vistas.ui.theme.DonutChartView

class DashboardFragment : Fragment(R.layout.screen_dash_gast) {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtTotal = view.findViewById<TextView>(R.id.txtGastoMes)
        val txtPendiente = view.findViewById<TextView>(R.id.txtGastoPendiente)
        val chart = view.findViewById<DonutChartView>(R.id.donutChart)
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerRecientes)

        val adapter = GastoAdapter(emptyList()) {}
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = adapter

        // CAMBIO IMPORTANTE: Observamos gastosGlobales
        viewModel.gastosGlobales.observe(viewLifecycleOwner) { lista ->
            // El dashboard siempre muestra los 3 más recientes reales, sin filtros
            adapter.updateData(lista.take(3))
            chart.setData(lista)
        }

        viewModel.totalMes.observe(viewLifecycleOwner) { total ->
            txtTotal.text = "$${String.format("%.2f", total)}"
        }

        viewModel.totalPendiente.observe(viewLifecycleOwner) { pendiente ->
            txtPendiente.text = "$${String.format("%.2f", pendiente)}"
        }
    }
}