package com.example.vistas.ui.theme

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class DonutChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val rect = RectF()
    private var slices: List<Pair<Float, Int>> = emptyList()


    private val colors = listOf(
        Color.parseColor("#2563EB"), // Azul
        Color.parseColor("#10B981"), // Verde
        Color.parseColor("#F59E0B"), // Ambar
        Color.parseColor("#EF4444"), // Rojo
        Color.parseColor("#8B5CF6"), // Violeta
        Color.parseColor("#EC4899"), // Rosa
        Color.parseColor("#6366F1")  // Indigo
    )

    fun setData(data: Map<String, Double>?) {
        if (data == null || data.isEmpty()) {
            slices = emptyList()
            invalidate()
            return
        }

        val total = data.values.sum().toFloat()
        if (total == 0f) {
            slices = emptyList()
            invalidate()
            return
        }

        val newSlices = mutableListOf<Pair<Float, Int>>()
        var colorIndex = 0


        data.entries.sortedByDescending { it.value }.forEach { (_, monto) ->
            val sweepAngle = (monto.toFloat() / total) * 360f

            // Asignamos el color y avanzamos el índice
            val color = colors[colorIndex % colors.size]
            newSlices.add(sweepAngle to color)

            colorIndex++
        }

        slices = newSlices
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val strokeWidth = 40f
        paint.strokeWidth = strokeWidth
        // Usamos StrokeCap.BUTT o ROUND. ROUND queda bonito pero puede engañar en tamaños pequeños.
        paint.strokeCap = Paint.Cap.BUTT

        val padding = strokeWidth / 2
        rect.set(padding, padding, width - padding, height - padding)


        var currentAngle = -90f

        slices.forEach { (sweep, color) ->
            paint.color = color


            canvas.drawArc(rect, currentAngle, sweep, false, paint)

            currentAngle += sweep
        }
    }
}