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

        // Referencias a Vistas
        val txtTotalMes = view.findViewById<TextView>(R.id.txtTotalMes)
        val txtTotalPendiente = view.findViewById<TextView>(R.id.txtTotalPendiente)

        // Secciones Admin
        val layoutGrafico = view.findViewById<LinearLayout>(R.id.layoutGrafico)
        val sectionCategorias = view.findViewById<LinearLayout>(R.id.sectionCategorias)
        val sectionEmpleados = view.findViewById<LinearLayout>(R.id.sectionEmpleados) // ¡Ahora sí existe!

        // Contenedores de listas y gráfico
        val donutChart = view.findViewById<DonutChartView>(R.id.donutChart)
        val layoutListCategorias = view.findViewById<LinearLayout>(R.id.layoutStatsCategorias)
        val layoutListEmpleados = view.findViewById<LinearLayout>(R.id.layoutStatsEmpleados)

        // 1. SIEMPRE: Mostrar Totales
        viewModel.totalMes.observe(viewLifecycleOwner) { txtTotalMes.text = formatoMoneda(it) }
        viewModel.totalPendiente.observe(viewLifecycleOwner) { txtTotalPendiente.text = formatoMoneda(it) }

        // 2. LÓGICA DE ROLES (Admin vs Empleado)
        if (viewModel.isAdmin) {
            // -- ES ADMIN: Ver todo (Gráfico, Categorías, Empleados) --
            layoutGrafico.visibility = View.VISIBLE
            sectionCategorias.visibility = View.VISIBLE
            sectionEmpleados.visibility = View.VISIBLE

            // A) Categorías y Gráfico
            viewModel.statsCategorias.observe(viewLifecycleOwner) { mapa ->
                donutChart.setData(mapa) // Pintar gráfico

                layoutListCategorias.removeAllViews()
                if (mapa.isEmpty()) {
                    agregarFila(layoutListCategorias, "Sin datos globales", "")
                } else {
                    mapa.entries.sortedByDescending { it.value }.forEach { (cat, monto) ->
                        agregarFila(layoutListCategorias, cat, formatoMoneda(monto))
                    }
                }
            }

            // B) Empleados
            viewModel.statsEmpleados.observe(viewLifecycleOwner) { mapa ->
                layoutListEmpleados.removeAllViews()
                if (mapa.isEmpty()) {
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
            // -- ES EMPLEADO: Solo ver Totales --
            layoutGrafico.visibility = View.GONE
            sectionCategorias.visibility = View.GONE
            sectionEmpleados.visibility = View.GONE
        }
    }

    private fun agregarFila(parent: LinearLayout, textoIzq: String, textoDer: String) {
        val context = requireContext()
        val row = LinearLayout(context)
        row.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        row.orientation = LinearLayout.HORIZONTAL
        row.setPadding(0, 12, 0, 12)

        val tvIzq = TextView(context)
        tvIzq.text = textoIzq
        tvIzq.setTextColor(ContextCompat.getColor(context, R.color.text_main))
        tvIzq.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        val tvDer = TextView(context)
        tvDer.text = textoDer
        tvDer.setTypeface(null, Typeface.BOLD)
        tvDer.setTextColor(ContextCompat.getColor(context, R.color.text_main))
        tvDer.gravity = Gravity.END

        row.addView(tvIzq)
        row.addView(tvDer)
        parent.addView(row)

        val line = View(context)
        line.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
        line.setBackgroundColor(ContextCompat.getColor(context, R.color.text_secondary))
        line.alpha = 0.1f
        parent.addView(line)
    }

    private fun formatoMoneda(valor: Double): String {
        return String.format(Locale.US, "$%.2f", valor)
    }
}