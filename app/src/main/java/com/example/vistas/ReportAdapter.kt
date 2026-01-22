package com.example.vistas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton // Importante
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vistas.model.Reporte

class ReportAdapter(
    private var lista: List<Reporte>,
    // CALLBACKS: Funciones que se ejecutarán al pulsar los botones
    private val onResponder: (Reporte) -> Unit,
    private val onEliminar: (Reporte) -> Unit
) : RecyclerView.Adapter<ReportAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        // Textos
        val txtComercio: TextView = v.findViewById(R.id.txtReporteComercio)
        val txtDesc: TextView = v.findViewById(R.id.txtReporteDesc)
        val txtUser: TextView = v.findViewById(R.id.txtReporteUser)

        // Botones de acción (Deben existir en tu item_reporte.xml)
        val btnResponder: ImageButton = v.findViewById(R.id.btnResponderReporte)
        val btnEliminar: ImageButton = v.findViewById(R.id.btnEliminarReporte)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_reporte, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val rep = lista[position]

        holder.txtComercio.text = rep.comercio
        holder.txtDesc.text = rep.descripcion
        holder.txtUser.text = rep.emailUsuario

        // Configurar clic en el botón RESPONDER (Verde)
        holder.btnResponder.setOnClickListener {
            onResponder(rep)
        }

        // Configurar clic en el botón ELIMINAR (Papelera)
        holder.btnEliminar.setOnClickListener {
            onEliminar(rep)
        }
    }

    override fun getItemCount() = lista.size

    fun updateList(nueva: List<Reporte>) {
        lista = nueva
        notifyDataSetChanged()
    }
}