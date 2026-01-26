package com.example.vistas.model

import com.google.gson.annotations.SerializedName

enum class EstadoGasto {
    PENDIENTE, APROBADO, RECHAZADO
}

data class Gasto(
    @SerializedName("id") val id: String,

    // Tu UI busca 'emailUsuario' en AdminAdapter.kt
    // Usamos @SerializedName para leer el campo 'email' del JSON y meterlo en 'emailUsuario'
    @SerializedName("email") val emailUsuario: String = "",

    @SerializedName("id_empleado") val userId: String,
    @SerializedName("nombreComercio") val nombreComercio: String,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("categoria") val categoria: String,

    // CRÍTICO: La API manda "importe", pero tu UI (AdminAdapter/Dashboard) usa "monto"
    @SerializedName("importe") val monto: Double,

    @SerializedName("url_ticket") val imagenUrl: String = "",
    @SerializedName("estado") val estado: EstadoGasto = EstadoGasto.PENDIENTE,

    // Para ordenar en MainViewModel
    val timestamp: Long = 0L
)