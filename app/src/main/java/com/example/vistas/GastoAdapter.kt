package com.example.vistas

import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.vistas.model.EstadoGasto
import com.example.vistas.model.Gasto

class GastoAdapter(
    private var lista: List<Gasto>,
    var isSelectionMode: Boolean = false,
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<GastoAdapter.GastoVH>() {

    class GastoVH(view: View) : RecyclerView.ViewHolder(view) {
        val card: CardView = view.findViewById(R.id.cardRoot) // Referencia a la tarjeta completa
        val comercio: TextView = view.findViewById(R.id.txtComercio)
        val info: TextView = view.findViewById(R.id.txtInfo)
        val monto: TextView = view.findViewById(R.id.txtMonto)

        // Elementos de la Pastilla
        val container: LinearLayout = view.findViewById(R.id.layoutStatus)
        val status: TextView = view.findViewById(R.id.txtStatus)
        val dot: View = view.findViewById(R.id.dotStatus)

        // Checkbox eliminado del ViewHolder porque ya no existe en el XML
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GastoVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gasto, parent, false)
        return GastoVH(view)
    }

    override fun onBindViewHolder(holder: GastoVH, position: Int) {
        val gasto = lista[position]
        val ctx = holder.itemView.context

        // Datos básicos
        holder.comercio.text = gasto.nombreComercio
        holder.info.text = "${gasto.fecha} • ${gasto.categoria}"
        holder.monto.text = "$${String.format("%.2f", gasto.monto)}"

        // Diseño de la pastilla de estado
        configurarEstado(holder, gasto)

        // --- LÓGICA DE SELECCIÓN "PREMIUM" ---

        // 1. Definir colores (Normal vs Seleccionado)
        val colorNormal = ContextCompat.getColor(ctx, R.color.surface_card)
        // Usamos un azul muy suave para seleccionado (puedes ajustar el hex o usar un recurso)
        // En modo noche convendría un gris más claro que el fondo.
        // Aquí uso un tint del primary_blue con mucha transparencia para que sirva en ambos modos.
        val colorSeleccionado = adjustAlpha(ContextCompat.getColor(ctx, R.color.primary_blue), 0.2f)

        if (isSelectionMode) {
            // CASO A: Gasto APROBADO (Bloqueado)
            if (gasto.estado == EstadoGasto.APROBADO) {
                // Se ve normal, un poco más apagado quizás (opcional)
                holder.card.setCardBackgroundColor(colorNormal)
                holder.itemView.alpha = 0.5f // Le bajamos la opacidad para indicar que no es editable
                holder.itemView.scaleX = 1f
                holder.itemView.scaleY = 1f

                holder.itemView.setOnClickListener {
                    Toast.makeText(ctx, "No puedes borrar gastos aprobados", Toast.LENGTH_SHORT).show()
                }
            }
            // CASO B: SELECCIONABLE (Pendiente/Rechazado)
            else {
                holder.itemView.alpha = 1f // Opacidad total

                // Si está seleccionado, cambiamos color y tamaño
                if (gasto.isSelected) {
                    holder.card.setCardBackgroundColor(colorSeleccionado)
                    // Efecto "apretado"
                    holder.itemView.scaleX = 0.95f
                    holder.itemView.scaleY = 0.95f
                } else {
                    holder.card.setCardBackgroundColor(colorNormal)
                    holder.itemView.scaleX = 1f
                    holder.itemView.scaleY = 1f
                }

                // Click Listener con animación
                holder.itemView.setOnClickListener {
                    gasto.isSelected = !gasto.isSelected
                    notifyItemChanged(position) // Refrescamos solo este item para que haga la animación
                    onSelectionChanged()
                }
            }
        } else {
            // MODO NORMAL (Fuera de selección)
            holder.card.setCardBackgroundColor(colorNormal)
            holder.itemView.alpha = 1f
            holder.itemView.scaleX = 1f
            holder.itemView.scaleY = 1f
            holder.itemView.setOnClickListener(null) // O abrir detalle si tuvieras
        }
    }

    override fun getItemCount() = lista.size

    fun updateData(nuevaLista: List<Gasto>) {
        this.lista = nuevaLista
        notifyDataSetChanged()
    }

    fun activarModoSeleccion(activar: Boolean) {
        isSelectionMode = activar
        if (activar) {
            lista.forEach { if (it.estado == EstadoGasto.APROBADO) it.isSelected = false }
        } else {
            lista.forEach { it.isSelected = false }
        }
        notifyDataSetChanged()
    }

    fun getSelectedCount() = lista.count { it.isSelected }
    fun getSelectedIds() = lista.filter { it.isSelected }.map { it.id }

    // Función auxiliar para crear un color transparente basado en otro
    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    private fun configurarEstado(holder: GastoVH, gasto: Gasto) {
        val ctx = holder.itemView.context
        val (bg, txt, dotDraw, label) = when (gasto.estado) {
            EstadoGasto.APROBADO -> Quad(R.color.status_approved_bg, R.color.status_approved_text, R.drawable.dot_green, "APROBADO")
            EstadoGasto.RECHAZADO -> Quad(R.color.status_rejected_bg, R.color.status_rejected_text, R.drawable.dot_red, "RECHAZADO")
            else -> Quad(R.color.status_pending_bg, R.color.status_pending_text, R.drawable.dot_amber, if(gasto.estado==EstadoGasto.PROCESANDO) "PROCESANDO" else "PENDIENTE")
        }

        holder.container.backgroundTintList = ContextCompat.getColorStateList(ctx, bg)
        holder.status.setTextColor(ContextCompat.getColor(ctx, txt))
        holder.status.text = label
        holder.dot.setBackgroundResource(dotDraw)
        holder.dot.visibility = View.VISIBLE
    }

    // Clase simple para devolver 4 valores
    data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}