package com.example.vistas.data

import com.example.vistas.model.Gasto
import com.example.vistas.model.EstadoGasto

object FakeRepository {
    // Usamos una lista mutable para poder añadir gastos desde el OCR
    private val listaGastos = mutableListOf(
        Gasto("1", "Starbucks Coffee", "15 Oct 2023", "Comida", 12.50, EstadoGasto.APROBADO),
        Gasto("2", "Uber Trip", "14 Oct 2023", "Transporte", 24.00, EstadoGasto.PENDIENTE),
        Gasto("3", "Apple Store", "12 Oct 2023", "Equipamiento", 99.00, EstadoGasto.RECHAZADO)
    )

    fun getAllGastos(): List<Gasto> = listaGastos

    fun getRecentActivity(): List<Gasto> = listaGastos.take(5)
    fun addGasto(nuevoGasto: Gasto) {
        listaGastos.add(0, nuevoGasto) // Lo añade al principio de la lista
    }
}



