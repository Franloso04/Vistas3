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

        // Colores hardcodeados según tu diseño (Azul, Verde, Naranja)
        val colors = listOf(
            Color.parseColor("#2563EB"), // Azul
            Color.parseColor("#10B981"), // Verde
            Color.parseColor("#F59E0B"), // Naranja
            Color.parseColor("#64748B")  // Gris (Otros)
        )

        var colorIndex = 0
        data.forEach { (_, monto) ->
            val sweepAngle = (monto.toFloat() / total) * 360f
            newSlices.add(sweepAngle to colors[colorIndex % colors.size])
            colorIndex++
        }
        slices = newSlices
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val strokeWidth = 40f
        paint.strokeWidth = strokeWidth
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