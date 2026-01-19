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

    private fun configurarEstado(holder: GastoVH, gasto: Gasto) {
        val context = holder.itemView.context
        holder.dot.visibility = View.VISIBLE

        when (gasto.estado) {
            EstadoGasto.APROBADO -> {
                holder.container.setBackgroundResource(R.drawable.bg_status_approved)
                holder.status.text = "APROBADO"
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.status_approved_text))
                holder.dot.setBackgroundResource(R.drawable.dot_green)
            }
            EstadoGasto.PENDIENTE -> {
                holder.container.setBackgroundResource(R.drawable.bg_status_pending)
                holder.status.text = "PENDIENTE"
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.status_pending_text))
                holder.dot.setBackgroundResource(R.drawable.dot_amber)
            }
            EstadoGasto.RECHAZADO -> {
                holder.container.setBackgroundResource(R.drawable.bg_status_rejected)
                holder.status.text = "RECHAZADO"
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.status_rejected_text))
                holder.dot.setBackgroundResource(R.drawable.dot_red)
            }
            EstadoGasto.PROCESANDO -> {
                holder.container.setBackgroundResource(R.drawable.bg_status_processing)
                holder.status.text = "Procesando"
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.status_pending_text))
                holder.dot.visibility = View.GONE
            }
        }
    }
}