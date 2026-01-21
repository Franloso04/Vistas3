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
        val status: TextView = view.findViewById(R.id.txtStatus)
        val container: LinearLayout = view.findViewById(R.id.layoutStatus)
        val check: CheckBox = view.findViewById(R.id.checkDelete)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int): GastoVH {
        val view = LayoutInflater.from(p.context).inflate(R.layout.item_gasto, p, false)
        return GastoVH(view)
    }

    override fun onBindViewHolder(holder: GastoVH, position: Int) {
        val gasto = lista[position]

        holder.comercio.text = gasto.nombreComercio
        holder.info.text = "${gasto.fecha} • ${gasto.categoria}"
        holder.monto.text = "$${String.format("%.2f", gasto.monto)}"

        // Diseño de la pastilla de estado
        configurarEstado(holder, gasto)

        // LÓGICA DE SELECCIÓN
        if (isSelectionMode) {
            holder.check.visibility = View.VISIBLE
            holder.check.setOnCheckedChangeListener(null)
            holder.check.isChecked = gasto.isSelected

            // Click en toda la fila selecciona
            val clickListener = View.OnClickListener {
                gasto.isSelected = !gasto.isSelected
                holder.check.isChecked = gasto.isSelected
                onSelectionChanged()
            }
            holder.itemView.setOnClickListener(clickListener)
            holder.check.setOnClickListener(clickListener)
        } else {
            holder.check.visibility = View.GONE
            holder.itemView.setOnClickListener(null) // Reset click normal si hubiera detalle
        }
    }

    override fun getItemCount() = lista.size

    fun updateData(nuevaLista: List<Gasto>) {
        this.lista = nuevaLista
        notifyDataSetChanged()
    }

    // Funciones para el Fragmento
    fun activarModoSeleccion(activar: Boolean) {
        isSelectionMode = activar
        if (!activar) lista.forEach { it.isSelected = false }
        notifyDataSetChanged()
    }

    fun getSelectedCount() = lista.count { it.isSelected }
    fun getSelectedIds() = lista.filter { it.isSelected }.map { it.id }

    // Colores de estado
    private fun configurarEstado(holder: GastoVH, gasto: Gasto) {
        val ctx = holder.itemView.context
        val (bg, txt, label) = when (gasto.estado) {
            EstadoGasto.APROBADO -> Triple(R.color.status_approved_bg, R.color.status_approved_text, "APROBADO")
            EstadoGasto.RECHAZADO -> Triple(R.color.status_rejected_bg, R.color.status_rejected_text, "RECHAZADO")
            else -> Triple(R.color.status_pending_bg, R.color.status_pending_text, "PROCESANDO")
        }
        holder.container.backgroundTintList = ContextCompat.getColorStateList(ctx, bg)
        holder.status.setTextColor(ContextCompat.getColor(ctx, txt))
        holder.status.text = label
    }
}