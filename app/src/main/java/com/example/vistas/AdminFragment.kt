package com.example.vistas

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText // Importante para el diálogo
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

        // 1. Configurar Recycler de Gastos Pendientes
        val recyclerGastos = view.findViewById<RecyclerView>(R.id.recyclerPendientes)
        recyclerGastos.layoutManager = LinearLayoutManager(context)

        adapterGastos = AdminAdapter(
            lista = emptyList(),
            onAprobar = { gasto -> intentarAprobar(gasto) },
            onRechazar = { gasto -> viewModel.rechazarGasto(gasto.id) },
            onEliminar = { gasto -> viewModel.eliminarGastoIndividual(gasto.id) }
        )
        recyclerGastos.adapter = adapterGastos

        // 2. Configurar Recycler de Reportes (Incidencias)
        val recyclerReportes = view.findViewById<RecyclerView>(R.id.recyclerReportes)
        recyclerReportes.layoutManager = LinearLayoutManager(context)

        // AQUÍ CONFIGURAMOS LAS ACCIONES DE LOS REPORTES
        adapterReportes = ReportAdapter(
            lista = emptyList(),
            onResponder = { reporte -> mostrarDialogoRespuesta(reporte) }, // Acción botón VERDE
            onEliminar = { reporte -> confirmarEliminarReporte(reporte) } // Acción botón ROJO/PAPELERA
        )
        recyclerReportes.adapter = adapterReportes

        // 3. Observar Datos
        viewModel.gastosGlobales.observe(viewLifecycleOwner) { lista ->
            // Filtramos solo los pendientes para el admin
            val pendientes = lista.filter { it.estado.name == "PENDIENTE" }
            adapterGastos.updateList(pendientes)
        }

        viewModel.reportes.observe(viewLifecycleOwner) { lista ->
            adapterReportes.updateList(lista)
        }
    }

    // --- FUNCIÓN PARA BORRAR REPORTE ---
    private fun confirmarEliminarReporte(reporte: Reporte) {
        AlertDialog.Builder(requireContext())
            .setTitle("¿Eliminar reporte?")
            .setMessage("Se borrará la incidencia de ${reporte.comercio}. ¿Estás seguro?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.eliminarReporte(reporte.id)
                Toast.makeText(context, "Reporte eliminado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // --- FUNCIÓN PARA RESPONDER REPORTE ---
    private fun mostrarDialogoRespuesta(reporte: Reporte) {
        val input = EditText(requireContext())
        input.hint = "Escribe tu respuesta aquí..."
        input.setPadding(50, 50, 50, 50)

        AlertDialog.Builder(requireContext())
            .setTitle("Responder a ${reporte.emailUsuario}")
            .setView(input)
            .setPositiveButton("Enviar") { _, _ ->
                val texto = input.text.toString()
                if (texto.isNotEmpty()) {
                    viewModel.responderReporte(reporte.id, texto)
                    Toast.makeText(context, "Respuesta enviada", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun intentarAprobar(gasto: Gasto) {
        val incidencia = viewModel.tieneIncidencia(gasto.id)

        if (incidencia != null) {
            AlertDialog.Builder(requireContext())
                .setTitle("⚠️ ¡Atención!")
                .setMessage("Este ticket tiene una incidencia:\n'${incidencia.descripcion}'\n\n¿Aprobar de todas formas?")
                .setPositiveButton("Aprobar") { _, _ ->
                    viewModel.aprobarGasto(gasto.id)
                    Toast.makeText(context, "Aprobado manualmente", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        } else {
            viewModel.aprobarGasto(gasto.id)
        }
    }
}