package com.example.vistas.data

import com.example.vistas.model.EstadoGasto
import com.example.vistas.model.Gasto

object FakeRepository {
    fun getAllGastos() = listOf(
        Gasto("1", "Starbucks Coffee", "15 Oct 2023", "Comida", 12.50, EstadoGasto.APROBADO),
        Gasto("2", "Uber Trip", "14 Oct 2023", "Transporte", 24.00, EstadoGasto.PENDIENTE),
        Gasto("3", "Apple Store", "12 Oct 2023", "Equipamiento", 99.00, EstadoGasto.RECHAZADO),
        Gasto("4", "Procesando ticket...", "Hoy", "", 0.00, EstadoGasto.PROCESANDO)
    )
}