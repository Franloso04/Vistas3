package com.example.vistas.model



data class Reporte(
    val id: String = "",
    val gastoId: String = "",
    val descripcion: String = "",
    val comercio: String = "",
    val emailUsuario: String = "",
    val estado: String = "PENDIENTE"
)