package com.example.vistas

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vistas.data.FakeRepository

class ExpensesFragment : Fragment(R.layout.screen_hist_gast) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerHistorial)
        val adapter = GastoAdapter(FakeRepository.getAllGastos()) {
            // Acción al pulsar
        }

        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = adapter
    }
}
