package com.example.vistas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vistas.model.Gasto

class AdminAdapter(
    private var lista: List<Gasto>,
    private val onAprobar: (Gasto) -> Unit,
    private val onRechazar: (Gasto) -> Unit,
    private val onEliminar: (Gasto) -> Unit // <--- ESTO ES LO QUE TE FALTA
) : RecyclerView.Adapter<AdminAdapter.AdminViewHolder>() {

    class AdminViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val usuario: TextView = view.findViewById(R.id.txtUsuarioAdmin)
        val fecha: TextView = view.findViewById(R.id.txtFechaAdmin)
        val comercio: TextView = view.findViewById(R.id.txtComercioAdmin)
        val categoria: TextView = view.findViewById(R.id.txtCategoriaAdmin)
        val monto: TextView = view.findViewById(R.id.txtMontoAdmin)

        // Botones
        val btnAprobar: Button = view.findViewById(R.id.btnAprobar)
        val btnRechazar: Button = view.findViewById(R.id.btnRechazar)
        //val btnEliminar: Button = view.findViewById(R.id.btnEliminarAdmin) // Asegúrate de que este ID existe en item_admin_gasto.xml
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_gasto, parent, false)
        return AdminViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminViewHolder, position: Int) {
        val gasto = lista[position]

        holder.usuario.text = gasto.emailUsuario
        holder.fecha.text = gasto.fecha
        holder.comercio.text = gasto.nombreComercio
        holder.categoria.text = gasto.categoria
        holder.monto.text = "$${String.format("%.2f", gasto.monto)}"

        // Conectamos los 3 botones
        holder.btnAprobar.setOnClickListener { onAprobar(gasto) }
        holder.btnRechazar.setOnClickListener { onRechazar(gasto) }
        //holder.btnEliminar.setOnClickListener { onEliminar(gasto) }
    }

    override fun getItemCount() = lista.size

    fun updateData(nuevaLista: List<Gasto>) {
        this.lista = nuevaLista
        notifyDataSetChanged()
    }
}