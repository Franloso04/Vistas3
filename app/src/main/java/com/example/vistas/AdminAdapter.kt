package com.example.vistas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vistas.model.Gasto

// Asegúrate de NO tener 'import android.R' aquí arriba

class AdminAdapter(
    private var lista: List<Gasto>,
    private val onAprobar: (Gasto) -> Unit,
    private val onRechazar: (Gasto) -> Unit,
    private val onEliminar: (Gasto) -> Unit
) : RecyclerView.Adapter<AdminAdapter.AdminVH>() {

    class AdminVH(view: View) : RecyclerView.ViewHolder(view) {
        // Enlazamos con los IDs EXACTOS de tu item_admin_gasto.xml
        val txtComercio: TextView = view.findViewById(R.id.txtAdminComercio)
        val txtMonto: TextView = view.findViewById(R.id.txtAdminMonto)
        val txtUsuario: TextView = view.findViewById(R.id.txtAdminUser)

        val btnApprove: ImageButton = view.findViewById(R.id.btnApprove)
        val btnReject: ImageButton = view.findViewById(R.id.btnReject)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminVH {
        // Cargamos tu archivo item_admin_gasto.xml
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_gasto, parent, false)
        return AdminVH(view)
    }

    override fun onBindViewHolder(holder: AdminVH, position: Int) {
        val gasto = lista[position]

        holder.txtComercio.text = gasto.nombreComercio
        holder.txtMonto.text = "$${gasto.monto}"
        holder.txtUsuario.text = gasto.emailUsuario

        // Configuramos los clics
        holder.btnApprove.setOnClickListener { onAprobar(gasto) }
        holder.btnReject.setOnClickListener { onRechazar(gasto) }
        holder.btnDelete.setOnClickListener { onEliminar(gasto) }
    }

    override fun getItemCount() = lista.size

    fun updateList(nuevaLista: List<Gasto>) {
        this.lista = nuevaLista
        notifyDataSetChanged()
    }
}