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

    // Colores para la leyenda (Deben coincidir con los del DonutChart)
    private val coloresGrafico = listOf(
        "#2563EB", // Azul
        "#10B981", // Verde
        "#F59E0B", // Ambar
        "#EF4444", // Rojo
        "#8B5CF6", // Violeta
        "#EC4899", // Rosa
        "#6366F1"  // Indigo
    )

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

        // B) Gráfico y Desglose de Categorías
        layoutGrafico.visibility = View.VISIBLE
        sectionCategorias.visibility = View.VISIBLE

        viewModel.statsCategorias.observe(viewLifecycleOwner) { mapa ->
            donutChart.setData(mapa) // Actualiza el gráfico

            layoutListCategorias.removeAllViews()
            if (mapa.isNullOrEmpty()) {
                agregarFila(layoutListCategorias, "Sin gastos registrados", "")
            } else {
                // Ordenamos por mayor gasto y usamos un índice para el color
                var index = 0
                mapa.entries.sortedByDescending { it.value }.forEach { (cat, monto) ->
                    // Pasamos 'index' para que pinte el puntito de color
                    agregarFila(layoutListCategorias, cat, formatoMoneda(monto), index)
                    index++
                }
            }
        }

        // --- 3. DATOS EXCLUSIVOS (SOLO ADMIN) ---
        if (viewModel.isAdmin) {
            sectionEmpleados.visibility = View.VISIBLE

            viewModel.statsEmpleados.observe(viewLifecycleOwner) { mapa ->
                layoutListEmpleados.removeAllViews()
                if (mapa.isNullOrEmpty()) {
                    agregarFila(layoutListEmpleados, "Sin datos de empleados", "")
                } else {
                    mapa.entries.sortedByDescending { it.value }.forEach { (email, monto) ->
                        val nombre = (email ?: "Desconocido").substringBefore("@")
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                        // Aquí NO pasamos índice (-1 por defecto), así que no sale puntito
                        agregarFila(layoutListEmpleados, nombre, formatoMoneda(monto))
                    }
                }
            }
        } else {
            sectionEmpleados.visibility = View.GONE
        }
    }

    // Función auxiliar modificada para incluir el puntito de color
    private fun agregarFila(
        parent: LinearLayout,
        textoIzq: String,
        textoDerecha: String,
        indexColor: Int = -1 // -1 significa "sin punto"
    ) {
        val context = requireContext()
        val row = LinearLayout(context)
        row.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        row.orientation = LinearLayout.HORIZONTAL
        row.setPadding(0, 16, 0, 16)
        row.gravity = Gravity.CENTER_VERTICAL // Alinear verticalmente el punto y el texto

        // 1. PUNTITO DE COLOR (Solo si indexColor es válido)
        if (indexColor >= 0) {
            val dot = View(context)
            val size = (10 * context.resources.displayMetrics.density).toInt() // 10dp
            val params = LinearLayout.LayoutParams(size, size)
            params.marginEnd = (12 * context.resources.displayMetrics.density).toInt() // Margen derecho
            dot.layoutParams = params

            // Usamos un drawable circular base (dot_green) y lo tintamos
            dot.background = ContextCompat.getDrawable(context, R.drawable.dot_green)

            // Calculamos el color cíclico
            val colorHex = coloresGrafico[indexColor % coloresGrafico.size]
            dot.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(colorHex))

            row.addView(dot)
        }

        // 2. TEXTO IZQUIERDA (Categoría)
        val tvIzq = TextView(context)
        tvIzq.text = textoIzq
        tvIzq.textSize = 14f
        tvIzq.setTextColor(ContextCompat.getColor(context, R.color.text_main))
        tvIzq.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        // 3. TEXTO DERECHA (Monto)
        val tvDer = TextView(context)
        tvDer.text = textoDerecha
        tvDer.textSize = 14f
        tvDer.setTypeface(null, Typeface.BOLD)
        tvDer.setTextColor(ContextCompat.getColor(context, R.color.text_main))
        tvDer.gravity = Gravity.END

        row.addView(tvIzq)
        row.addView(tvDer)
        parent.addView(row)

        // 4. LÍNEA SEPARADORA
        val line = View(context)
        line.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
        line.setBackgroundColor(ContextCompat.getColor(context, R.color.text_secondary))
        line.alpha = 0.1f // Muy sutil
        parent.addView(line)
    }

    private fun formatoMoneda(valor: Double): String {
        return String.format(Locale.US, "$%.2f", valor)
    }
}