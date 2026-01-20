package com.example.vistas
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.vistas.R
import com.example.vistas.model.EstadoGasto
import com.example.vistas.model.Gasto

class GastoAdapter(
    private var lista: List<Gasto>,
    private var isSelectionMode: Boolean = false,
    private val onAction: () -> Unit
) : RecyclerView.Adapter<GastoAdapter.GastoVH>() {

    class GastoVH(view: View) : RecyclerView.ViewHolder(view) {
        val comercio: TextView = view.findViewById(R.id.txtComercio)
        val info: TextView = view.findViewById(R.id.txtInfo)
        val monto: TextView = view.findViewById(R.id.txtMonto)
        val status: TextView = view.findViewById(R.id.txtStatus)
        val container: LinearLayout = view.findViewById(R.id.layoutStatus)
        val dot: View = view.findViewById(R.id.dotStatus)
        val check: CheckBox = view.findViewById(R.id.checkDelete)
        val icono: ImageView = view.findViewById(R.id.imgIconCategory)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int): GastoVH {
        val view = LayoutInflater.from(p.context).inflate(R.layout.item_gasto, p, false)
        return GastoVH(view)
    }

    override fun onBindViewHolder(holder: GastoVH, position: Int) {
        val gasto = lista[position]

        // Datos básicos
        holder.comercio.text = gasto.nombreComercio
        holder.info.text = "${gasto.fecha} • ${gasto.categoria}"
        holder.monto.text = "$${String.format("%.2f", gasto.monto)}"

        // Aplicar lógica de estados (Refactorizada)
        configurarEstado(holder, gasto)

        // Lógica de selección
        holder.check.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
        holder.check.isChecked = gasto.isSelected

        holder.itemView.setOnClickListener {
            if (isSelectionMode) {
                gasto.isSelected = !gasto.isSelected
                notifyItemChanged(position)
                onAction()
            }
        }
    }

    override fun getItemCount() = lista.size

    fun setSelectionMode(enabled: Boolean) {
        isSelectionMode = enabled
        notifyDataSetChanged()
    }

    fun updateData(nuevaLista: List<Gasto>) {
        this.lista = nuevaLista
        notifyDataSetChanged()
    }

    // Dentro de GastoAdapter.kt, reemplaza la función configurarEstado por esta:

    // 1. Crea esta clase auxiliar al final del archivo (fuera de la clase GastoAdapter)
    private data class StatusConfig(
        val bgColor: Int,
        val textColor: Int,
        val dotRes: Int,
        val label: String
    )

    // 2. Modifica la función dentro del Adapter
    private fun configurarEstado(holder: GastoVH, gasto: Gasto) {
        val context = holder.itemView.context

        // Solución al error de Triple: usamos nuestra propia clase StatusConfig
        val config = when (gasto.estado) {
            EstadoGasto.APROBADO -> StatusConfig(R.color.status_approved_bg, R.color.status_approved_text, R.drawable.dot_green, "APROBADO")
            EstadoGasto.PENDIENTE -> StatusConfig(R.color.status_pending_bg, R.color.status_pending_text, R.drawable.dot_amber, "PENDIENTE")
            EstadoGasto.RECHAZADO -> StatusConfig(R.color.status_rejected_bg, R.color.status_rejected_text, R.drawable.dot_red, "RECHAZADO")
            EstadoGasto.PROCESANDO -> StatusConfig(R.color.status_pending_bg, R.color.status_pending_text, 0, "Procesando")
        }

        holder.container.apply {
            setBackgroundResource(R.drawable.bg_status_pill) // Asegúrate de tener este XML
            backgroundTintList = androidx.core.content.ContextCompat.getColorStateList(context, config.bgColor)
        }

        holder.status.apply {
            text = config.label
            setTextColor(androidx.core.content.ContextCompat.getColor(context, config.textColor))
        }

        if (config.dotRes != 0) {
            holder.dot.visibility = android.view.View.VISIBLE
            holder.dot.setBackgroundResource(config.dotRes)
        } else {
            holder.dot.visibility = android.view.View.GONE
        }
    }
}