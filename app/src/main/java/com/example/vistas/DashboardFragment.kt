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
import com.example.vistas.ui.theme.DonutChartView // Importar tu gráfico
import java.util.Locale

class DashboardFragment : Fragment(R.layout.screen_dash_gast) {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Referencias
        val donutChart = view.findViewById<DonutChartView>(R.id.donutChart) // Referencia al gráfico
        val txtTotalMes = view.findViewById<TextView>(R.id.txtTotalMes)
        val txtTotalPendiente = view.findViewById<TextView>(R.id.txtTotalPendiente)
        val layoutCategorias = view.findViewById<LinearLayout>(R.id.layoutStatsCategorias)
        val layoutEmpleados = view.findViewById<LinearLayout>(R.id.layoutStatsEmpleados)
        val sectionEmpleados = view.findViewById<LinearLayout>(R.id.sectionEmpleados)

        // 1. Totales
        viewModel.totalMes.observe(viewLifecycleOwner) { total ->
            txtTotalMes.text = formatoMoneda(total)
        }
        viewModel.totalPendiente.observe(viewLifecycleOwner) { total ->
            txtTotalPendiente.text = formatoMoneda(total)
        }

        // 2. Categorías Y GRÁFICO
        viewModel.statsCategorias.observe(viewLifecycleOwner) { mapa ->
            // Actualizar Gráfico
            donutChart.setData(mapa)

            // Actualizar Lista
            layoutCategorias.removeAllViews()
            if (mapa.isEmpty()) {
                agregarFila(layoutCategorias, "Sin datos", "")
            } else {
                mapa.entries.sortedByDescending { it.value }.forEach { (cat, monto) ->
                    agregarFila(layoutCategorias, cat, formatoMoneda(monto))
                }
            }
        }

        // 3. Empleados (Solo Admin)
        if (viewModel.isAdmin) {
            sectionEmpleados.visibility = View.VISIBLE
            viewModel.statsEmpleados.observe(viewLifecycleOwner) { mapa ->
                layoutEmpleados.removeAllViews()
                if (mapa.isEmpty()) {
                    agregarFila(layoutEmpleados, "Sin datos", "")
                } else {
                    mapa.entries.sortedByDescending { it.value }.forEach { (email, monto) ->
                        val nombre = email.substringBefore("@")
                        agregarFila(layoutEmpleados, nombre, formatoMoneda(monto))
                    }
                }
            }
        } else {
            sectionEmpleados.visibility = View.GONE
        }
    }

    private fun agregarFila(parent: LinearLayout, textoIzq: String, textoDer: String) {
        val row = LinearLayout(context)
        row.orientation = LinearLayout.HORIZONTAL
        row.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        row.setPadding(0, 12, 0, 12)

        val tvIzq = TextView(context)
        tvIzq.text = textoIzq
        tvIzq.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_main))
        tvIzq.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        val tvDer = TextView(context)
        tvDer.text = textoDer
        tvDer.setTypeface(null, Typeface.BOLD)
        tvDer.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_main))
        tvDer.gravity = Gravity.END

        row.addView(tvIzq)
        row.addView(tvDer)
        parent.addView(row)

        // Línea separadora
        val line = View(context)
        line.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
        line.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
        line.alpha = 0.1f
        parent.addView(line)
    }

    private fun formatoMoneda(valor: Double): String {
        return String.format(Locale.US, "$%.2f", valor)
    }
}