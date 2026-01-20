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
    isSelectionMode: Boolean = false,
    private val onAction: () -> Unit
) : RecyclerView.Adapter<GastoAdapter.GastoVH>() {

    var isSelectionMode: Boolean = isSelectionMode
        set(value) {
            field = value
            if (!value) {
                lista.forEach { it.isSelected = false }
            }
            notifyDataSetChanged()
        }

    class GastoVH(view: View) : RecyclerView.ViewHolder(view) {
        val comercio: TextView = view.findViewById(R.id.txtComercio)
        val info: TextView = view.findViewById(R.id.txtInfo)
        val monto: TextView = view.findViewById(R.id.txtMonto)
        val status: TextView = view.findViewById(R.id.txtStatus)
        val container: LinearLayout = view.findViewById(R.id.layoutStatus)
        val dot: View = view.findViewById(R.id.dotStatus)
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

        configurarEstado(holder, gasto)

        // Lógica de selección
        holder.check.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
        holder.check.setOnCheckedChangeListener(null)
        holder.check.isChecked = gasto.isSelected

        val clickListener = View.OnClickListener {
            if (isSelectionMode) {
                gasto.isSelected = !gasto.isSelected
                holder.check.isChecked = gasto.isSelected
                onAction()
            }
        }

        holder.itemView.setOnClickListener(clickListener)
        holder.check.setOnClickListener(clickListener)
    }

    override fun getItemCount() = lista.size

    // --- FUNCIONES NECESARIAS ---

    fun updateData(nuevaLista: List<Gasto>) {
        this.lista = nuevaLista
        notifyDataSetChanged()
    }

    fun getSelectedCount(): Int {
        return lista.count { it.isSelected }
    }

    fun getSelectedIds(): List<String> {
        return lista.filter { it.isSelected }.map { it.id }
    }

    // --- LÓGICA DE DISEÑO (PILL) ---
    private data class StatusConfig(val bgColor: Int, val textColor: Int, val dotRes: Int, val label: String)

    private fun configurarEstado(holder: GastoVH, gasto: Gasto) {
        val context = holder.itemView.context
        val config = when (gasto.estado) {
            EstadoGasto.APROBADO -> StatusConfig(R.color.status_approved_bg, R.color.status_approved_text, R.drawable.dot_green, "APROBADO")
            EstadoGasto.PENDIENTE -> StatusConfig(R.color.status_pending_bg, R.color.status_pending_text, R.drawable.dot_amber, "PENDIENTE")
            EstadoGasto.RECHAZADO -> StatusConfig(R.color.status_rejected_bg, R.color.status_rejected_text, R.drawable.dot_red, "RECHAZADO")
            EstadoGasto.PROCESANDO -> StatusConfig(R.color.status_pending_bg, R.color.status_pending_text, 0, "Procesando")
        }

        holder.container.apply {
            setBackgroundResource(R.drawable.bg_status_pill)
            backgroundTintList = ContextCompat.getColorStateList(context, config.bgColor)
        }

        holder.status.apply {
            text = config.label
            setTextColor(ContextCompat.getColor(context, config.textColor))
        }

        if (config.dotRes != 0) {
            holder.dot.visibility = View.VISIBLE
            holder.dot.setBackgroundResource(config.dotRes)
        } else {
            holder.dot.visibility = View.GONE
        }
    }
}
