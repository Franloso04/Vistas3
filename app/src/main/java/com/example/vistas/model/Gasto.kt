package com.example.vistas.model

enum class EstadoGasto { APROBADO, PENDIENTE, RECHAZADO, PROCESANDO }

data class Gasto(
    val id: String,
    val nombreComercio: String,
    val fecha: String,
    val categoria: String,
    val monto: Double,
    val estado: EstadoGasto,
    val timestamp: Long = System.currentTimeMillis(), // NUEVO: Para ordenar fechas
    var isSelected: Boolean = false
)