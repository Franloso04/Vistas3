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

    // Conexión a la "Nube" local de la app
    private val viewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtTotal = view.findViewById<TextView>(R.id.txtGastoMes)
        val txtPendiente = view.findViewById<TextView>(R.id.txtGastoPendiente)
        val  chart = view.findViewById<DonutChartView>(R.id.donutChart)
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerRecientes)

        // Adapter para la lista pequeña de recientes
        val adapter = GastoAdapter(emptyList()) {}
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = adapter

        // --- AQUÍ ESTÁ LA MAGIA: OBSERVAMOS LOS DATOS REALES ---

        // 1. Lista de Gastos: Actualiza la lista y la Gráfica
        viewModel.gastos.observe(viewLifecycleOwner) { lista ->
            // Mostrar solo los últimos 3 en recientes
            adapter.updateData(lista.take(3))

            // Pintar la gráfica con datos reales
            chart.setData(lista)
        }

        // 2. Total Mes: Actualiza el texto grande azul
        viewModel.totalMes.observe(viewLifecycleOwner) { total ->
            txtTotal.text = "$${String.format("%.2f", total)}"
        }

        // 3. Pendiente: Actualiza el texto de la derecha
        viewModel.totalPendiente.observe(viewLifecycleOwner) { pendiente ->
            txtPendiente.text = "$${String.format("%.2f", pendiente)}"
        }
    }
}