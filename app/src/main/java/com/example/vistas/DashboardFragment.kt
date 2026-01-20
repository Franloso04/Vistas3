package com.example.vistas

import com.example.vistas.data.FakeRepository


import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vistas.GastoAdapter

class DashboardFragment : Fragment(R.layout.screen_dash_gast) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuración de tarjetas (Simulado)
        view.findViewById<TextView>(R.id.txtGastoMes).text = "$1.245,50"
        view.findViewById<TextView>(R.id.txtGastoPendiente).text = "$320,00"

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerRecientes)

        // Mostramos solo los 2 primeros para "Actividad Reciente"
        val adapter = GastoAdapter(FakeRepository.getAllGastos().take(2)) {
            // Acción opcional al pulsar
        }

        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = adapter
    }
}