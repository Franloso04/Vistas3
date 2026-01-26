package com.example.vistas.model

import java.math.BigDecimal

data class Reporte(
    val totalMes: BigDecimal,
    val totalPendiente: BigDecimal,
    val totalAprobado: BigDecimal,
    val totalRechazado: BigDecimal
)
