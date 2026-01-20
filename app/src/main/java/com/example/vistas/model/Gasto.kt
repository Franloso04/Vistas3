package com.example.vistas.model

enum class EstadoGasto { APROBADO, PENDIENTE, RECHAZADO, PROCESANDO }

data class Gasto(
    val id: String = "",
    val userId: String = "",
    val emailUsuario: String = "",
    val nombreComercio: String = "",
    val fecha: String = "",
    val categoria: String = "",
    val monto: Double = 0.0,
    val estado: EstadoGasto = EstadoGasto.PROCESANDO,
    val timestamp: Long = System.currentTimeMillis(), // Para ordenar
    var isSelected: Boolean = false // Ignorado por Firebase normalmente
)