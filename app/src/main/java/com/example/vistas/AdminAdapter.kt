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
        // Asegúrate de que estos IDs existen en item_admin_gasto.xml
        val txtComercio: TextView = v.findViewById(R.id.txtComercioAdmin)
        val txtMonto: TextView = v.findViewById(R.id.txtMontoAdmin)
        val txtUsuario: TextView = v.findViewById(R.id.txtUsuarioAdmin)
        val btnAprobar: ImageButton = v.findViewById(R.id.btnAprobar)
        val btnRechazar: ImageButton = v.findViewById(R.id.btnRechazar)
        val btnEliminar: ImageButton = v.findViewById(R.id.btnEliminarAdmin)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_gasto, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val gasto = lista[position]
        holder.txtComercio.text = gasto.nombreComercio
        // Ahora gasto.monto funciona porque lo definimos en el Modelo
        holder.txtMonto.text = "${gasto.monto}€"
        holder.txtUsuario.text = gasto.emailUsuario

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