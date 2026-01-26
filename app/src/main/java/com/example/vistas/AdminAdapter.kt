package com.example.vistas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vistas.model.Gasto

class AdminAdapter(
    private var lista: List<Gasto>,
    private val onAprobar: (Gasto) -> Unit,
    private val onRechazar: (Gasto) -> Unit,
    private val onEliminar: (Gasto) -> Unit
) : RecyclerView.Adapter<AdminAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val txtComercio: TextView = v.findViewById(R.id.txtAdminComercio)
        val txtMonto: TextView = v.findViewById(R.id.txtAdminMonto)
        val txtUsuario: TextView = v.findViewById(R.id.txtAdminUser)
        val btnAprobar: ImageButton = v.findViewById(R.id.btnApprove)
        val btnRechazar: ImageButton = v.findViewById(R.id.btnReject)
        val btnEliminar: ImageButton = v.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_gasto, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val gasto = lista[position]
        holder.txtComercio.text = gasto.nombreComercio
        holder.txtMonto.text = "${gasto.monto}€"
        holder.txtUsuario.text = gasto.emailUsuario.ifEmpty { "ID: ${gasto.userId}" }

        holder.btnAprobar.setOnClickListener { onAprobar(gasto) }
        holder.btnRechazar.setOnClickListener { onRechazar(gasto) }
        holder.btnEliminar.setOnClickListener { onEliminar(gasto) }
    }

    override fun getItemCount() = lista.size

    fun updateList(nuevaLista: List<Gasto>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}
