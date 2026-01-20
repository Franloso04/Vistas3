package com.example.vistas.data

import com.example.vistas.model.Gasto
import com.example.vistas.model.EstadoGasto

object FakeRepository {
    private val listaGastos = mutableListOf(
        Gasto("1", "Starbucks Coffee", "15 Oct 2023", "Comida", 12.50, EstadoGasto.APROBADO, 1697366400000),
        Gasto("2", "Uber Trip", "14 Oct 2023", "Transporte", 24.00, EstadoGasto.PENDIENTE, 1697280000000),
        Gasto("3", "Apple Store", "12 Oct 2023", "Equipamiento", 99.00, EstadoGasto.RECHAZADO, 1697107200000)
    )

    fun getAllGastos(): List<Gasto> = listaGastos.toList() // Devuelve copia para seguridad

    fun addGasto(nuevoGasto: Gasto) {
        listaGastos.add(0, nuevoGasto)
    }
}


