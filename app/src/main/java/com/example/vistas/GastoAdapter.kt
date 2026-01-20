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
    var isSelectionMode: Boolean = false, // Propiedad pública
    private val onAction: () -> Unit
) : RecyclerView.Adapter<GastoAdapter.GastoViewHolder>() {

    class GastoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val comercio: TextView = view.findViewById(R.id.txtComercio)
        val info: TextView = view.findViewById(R.id.txtInfo)
        val monto: TextView = view.findViewById(R.id.txtMonto)
        val status: TextView = view.findViewById(R.id.txtStatus)
        val container: LinearLayout = view.findViewById(R.id.layoutStatus)
        val dot: View = view.findViewById(R.id.dotStatus)
        val check: CheckBox = view.findViewById(R.id.checkDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GastoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gasto, parent, false)
        return GastoViewHolder(view)
    }

    override fun onBindViewHolder(holder: GastoViewHolder, position: Int) {
        val gasto = lista[position]

        holder.comercio.text = gasto.nombreComercio
        holder.info.text = "${gasto.fecha} • ${gasto.categoria}"
        holder.monto.text = "$${String.format("%.2f", gasto.monto)}"

        configurarEstado(holder, gasto)

        // Lógica de visualización del Checkbox
        holder.check.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
        holder.check.setOnCheckedChangeListener(null) // Evitar bugs al hacer scroll
        holder.check.isChecked = gasto.isSelected

        // Click listener unificado
        val listener = View.OnClickListener {
            if (isSelectionMode) {
                gasto.isSelected = !gasto.isSelected
                holder.check.isChecked = gasto.isSelected
                onAction() // Notificar cambios
            }
        }

        holder.itemView.setOnClickListener(listener)
        holder.check.setOnClickListener(listener)
    }

    override fun getItemCount(): Int = lista.size

    // --- MÉTODOS DE AYUDA ---

    fun updateData(nuevaLista: List<Gasto>) {
        this.lista = nuevaLista
        notifyDataSetChanged()
    }

    // RENOMBRADO PARA EVITAR EL ERROR "PLATFORM DECLARATION CLASH"
    fun activarModoSeleccion(activar: Boolean) {
        this.isSelectionMode = activar
        if (!activar) {
            // Si desactivamos, limpiamos las selecciones
            lista.forEach { it.isSelected = false }
        }
        notifyDataSetChanged()
    }

    fun getSelectedCount(): Int = lista.count { it.isSelected }

    fun getSelectedIds(): List<String> = lista.filter { it.isSelected }.map { it.id }

    // --- DISEÑO DE ESTADOS ---
    private data class StatusConfig(val bgColor: Int, val textColor: Int, val dotRes: Int, val label: String)

    private fun configurarEstado(holder: GastoViewHolder, gasto: Gasto) {
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