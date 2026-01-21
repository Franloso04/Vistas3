package com.example.vistas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
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
        val comercio: TextView = view.findViewById(R.id.txtComercio)
        val info: TextView = view.findViewById(R.id.txtInfo)
        val monto: TextView = view.findViewById(R.id.txtMonto)

        // Diseño Pastilla
        val container: LinearLayout = view.findViewById(R.id.layoutStatus)
        val status: TextView = view.findViewById(R.id.txtStatus)
        val dot: View = view.findViewById(R.id.dotStatus)

        val check: CheckBox = view.findViewById(R.id.checkDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GastoVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gasto, parent, false)
        return GastoVH(view)
    }

    override fun onBindViewHolder(holder: GastoVH, position: Int) {
        val gasto = lista[position]

        holder.comercio.text = gasto.nombreComercio
        holder.info.text = "${gasto.fecha} • ${gasto.categoria}"
        holder.monto.text = "$${String.format("%.2f", gasto.monto)}"

        // Diseño
        configurarEstado(holder, gasto)

        // Selección
        if (isSelectionMode) {
            holder.check.visibility = View.VISIBLE
            holder.check.setOnCheckedChangeListener(null)
            holder.check.isChecked = gasto.isSelected
            val clickListener = View.OnClickListener {
                gasto.isSelected = !gasto.isSelected
                holder.check.isChecked = gasto.isSelected
                onSelectionChanged()
            }
            holder.itemView.setOnClickListener(clickListener)
            holder.check.setOnClickListener(clickListener)
        } else {
            holder.check.visibility = View.GONE
            holder.itemView.setOnClickListener(null)
        }
    }

    override fun getItemCount() = lista.size

    fun updateData(nuevaLista: List<Gasto>) {
        this.lista = nuevaLista
        notifyDataSetChanged()
    }

    fun activatingModoSeleccion(activar: Boolean) {
        isSelectionMode = activar
        if (!activar) lista.forEach { it.isSelected = false }
        notifyDataSetChanged()
    }

    fun activarModoSeleccion(activar: Boolean) { activatingModoSeleccion(activar) }
    fun getSelectedCount() = lista.count { it.isSelected }
    fun getSelectedIds() = lista.filter { it.isSelected }.map { it.id }

    private fun configurarEstado(holder: GastoVH, gasto: Gasto) {
        val ctx = holder.itemView.context
        val bgColor: Int
        val txtColor: Int
        val dotDrawable: Int
        val text: String

        when (gasto.estado) {
            EstadoGasto.APROBADO -> {
                bgColor = R.color.status_approved_bg
                txtColor = R.color.status_approved_text
                dotDrawable = R.drawable.dot_green
                text = "APROBADO"
            }
            EstadoGasto.RECHAZADO -> {
                bgColor = R.color.status_rejected_bg
                txtColor = R.color.status_rejected_text
                dotDrawable = R.drawable.dot_red
                text = "RECHAZADO"
            }
            else -> {
                bgColor = R.color.status_pending_bg
                txtColor = R.color.status_pending_text
                dotDrawable = R.drawable.dot_amber
                text = if (gasto.estado == EstadoGasto.PROCESANDO) "PROCESANDO" else "PENDIENTE"
            }
        }

        holder.container.backgroundTintList = ContextCompat.getColorStateList(ctx, bgColor)
        holder.status.setTextColor(ContextCompat.getColor(ctx, txtColor))
        holder.status.text = text
        holder.dot.setBackgroundResource(dotDrawable)
        holder.dot.visibility = View.VISIBLE
    }
}