package com.example.vistas.model

import com.google.gson.annotations.SerializedName

enum class EstadoGasto {
    @SerializedName("pendiente") PENDIENTE,
    @SerializedName("aprobado") APROBADO,
    @SerializedName("rechazado") RECHAZADO,
    @SerializedName("procesando") PROCESANDO
}

data class Gasto(
    @SerializedName("id") val id: String,
    @SerializedName("id_empleado") val userId: String,
    @SerializedName("email") val emailUsuario: String? = "",
    @SerializedName("nombreComercio") val nombreComercio: String?,
    @SerializedName("fecha") val fecha: String?,
    @SerializedName("categoria") val categoria: String?,
    @SerializedName("importe") val monto: Double?,
    @SerializedName("url_ticket") val imagenUrl: String? = "",
    @SerializedName("estado") val estado: EstadoGasto? = EstadoGasto.PENDIENTE,
    val timestamp: Long = 0L,
    var isSelected: Boolean = false
)