package com.example.vistas.model

import java.math.BigDecimal
import java.time.LocalDate

enum class EstadoGasto { APROBADO, PENDIENTE, RECHAZADO }

data class Gasto(
    val id: Long,
    val idEmpleado: Long,
    val idSeccion: Long,
    val categoria: String,
    val referenciaProveedor: String?,
    val estado: Enum<EstadoGasto>,          // pendiente, aprobado, rechazado
    val fecha: LocalDate,
    val importe: BigDecimal,
    val urlTicket: String?,
    val nombreComercio: String
)