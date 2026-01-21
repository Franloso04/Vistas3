package com.example.vistas

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.vistas.ui.theme.DonutChartView
import java.util.Locale

class DashboardFragment : Fragment(R.layout.screen_dash_gast) {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- 1. REFERENCIAS UI ---
        val txtTotalMes = view.findViewById<TextView>(R.id.txtTotalMes)
        val txtTotalPendiente = view.findViewById<TextView>(R.id.txtTotalPendiente)

        val layoutGrafico = view.findViewById<LinearLayout>(R.id.layoutGrafico)
        val sectionCategorias = view.findViewById<LinearLayout>(R.id.sectionCategorias)
        val sectionEmpleados = view.findViewById<LinearLayout>(R.id.sectionEmpleados)

        val donutChart = view.findViewById<DonutChartView>(R.id.donutChart)
        val layoutListCategorias = view.findViewById<LinearLayout>(R.id.layoutStatsCategorias)
        val layoutListEmpleados = view.findViewById<LinearLayout>(R.id.layoutStatsEmpleados)

        // --- 2. DATOS COMUNES (PARA TODOS: Admin y Empleado) ---

        // A) Totales Numéricos
        viewModel.totalMes.observe(viewLifecycleOwner) {
            txtTotalMes.text = formatoMoneda(it ?: 0.0)
        }
        viewModel.totalPendiente.observe(viewLifecycleOwner) {
            txtTotalPendiente.text = formatoMoneda(it ?: 0.0)
        }

        // B) Gráfico y Desglose de Categorías (AHORA VISIBLE PARA TODOS)
        // Hacemos visibles las secciones
        layoutGrafico.visibility = View.VISIBLE
        sectionCategorias.visibility = View.VISIBLE

        // Observamos los datos (Si es admin llegan globales, si es empleado llegan los suyos)
        viewModel.statsCategorias.observe(viewLifecycleOwner) { mapa ->
            donutChart.setData(mapa) // Actualiza el gráfico

            layoutListCategorias.removeAllViews()
            if (mapa.isNullOrEmpty()) {
                agregarFila(layoutListCategorias, "Sin gastos registrados", "")
            } else {
                // Ordenamos por mayor gasto
                mapa.entries.sortedByDescending { it.value }.forEach { (cat, monto) ->
                    agregarFila(layoutListCategorias, cat, formatoMoneda(monto))
                }
            }
        }

        // --- 3. DATOS EXCLUSIVOS (SOLO ADMIN) ---
        if (viewModel.isAdmin) {
            // Mostrar sección de empleados
            sectionEmpleados.visibility = View.VISIBLE

            // Llenar lista de empleados
            viewModel.statsEmpleados.observe(viewLifecycleOwner) { mapa ->
                layoutListEmpleados.removeAllViews()
                if (mapa.isNullOrEmpty()) {
                    agregarFila(layoutListEmpleados, "Sin datos de empleados", "")
                } else {
                    mapa.entries.sortedByDescending { it.value }.forEach { (email, monto) ->
                        val nombre = email.substringBefore("@")
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                        agregarFila(layoutListEmpleados, nombre, formatoMoneda(monto))
                    }
                }
            }
        } else {
            // Si eres empleado, ocultamos la sección de "Gasto por Empleado"
            sectionEmpleados.visibility = View.GONE
        }
    }

    // Función auxiliar para pintar las filas de las listas
    private fun agregarFila(parent: LinearLayout, textoIzq: String, textoDerecha: String) {
        val context = requireContext()
        val row = LinearLayout(context)
        row.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        row.orientation = LinearLayout.HORIZONTAL
        row.setPadding(0, 16, 0, 16)

        val tvIzq = TextView(context)
        tvIzq.text = textoIzq
        tvIzq.textSize = 14f
        tvIzq.setTextColor(ContextCompat.getColor(context, R.color.text_main))
        tvIzq.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        val tvDer = TextView(context)
        tvDer.text = textoDerecha
        tvDer.textSize = 14f
        tvDer.setTypeface(null, Typeface.BOLD)
        tvDer.setTextColor(ContextCompat.getColor(context, R.color.text_main))
        tvDer.gravity = Gravity.END

        row.addView(tvIzq)
        row.addView(tvDer)
        parent.addView(row)

        // Línea divisoria sutil
        val line = View(context)
        line.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
        line.setBackgroundColor(ContextCompat.getColor(context, R.color.text_secondary))
        line.alpha = 0.2f
        parent.addView(line)
    }

    private fun formatoMoneda(valor: Double): String {
        return String.format(Locale.US, "$%.2f", valor)
    }
}