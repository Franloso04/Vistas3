package com.example.vistas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vistas.data.FakeRepository
import com.example.vistas.model.Gasto
import java.util.Locale

class MainViewModel : ViewModel() {

    // Lista maestra de gastos (Fuente de verdad)
    private val _gastos = MutableLiveData<List<Gasto>>()
    val gastos: LiveData<List<Gasto>> = _gastos

    // Estadísticas para el Dashboard
    private val _totalMes = MutableLiveData<Double>()
    val totalMes: LiveData<Double> = _totalMes

    private val _totalPendiente = MutableLiveData<Double>()
    val totalPendiente: LiveData<Double> = _totalPendiente

    init {
        // Cargar datos iniciales
        _gastos.value = FakeRepository.getAllGastos()
        recalcularTotales()
    }

    fun agregarGasto(gasto: Gasto) {
        val listaActual = _gastos.value.orEmpty().toMutableList()
        listaActual.add(0, gasto) // Añadir al principio
        _gastos.value = listaActual
        recalcularTotales()
    }

    fun eliminarGastosSeleccionados(ids: List<String>) {
        val listaActual = _gastos.value.orEmpty().toMutableList()
        listaActual.removeAll { it.id in ids }
        _gastos.value = listaActual
        recalcularTotales()
    }

    private fun recalcularTotales() {
        val lista = _gastos.value.orEmpty()
        _totalMes.value = lista.sumOf { it.monto }
        // Asumiendo que "PENDIENTE" y "PROCESANDO" cuentan como pendiente
        _totalPendiente.value = lista.filter {
            it.estado.name == "PENDIENTE" || it.estado.name == "PROCESANDO"
        }.sumOf { it.monto }
    }

    // Lógica del buscador
    fun filtrarGastos(query: String): List<Gasto> {
        val lista = _gastos.value.orEmpty()
        if (query.isEmpty()) return lista
        return lista.filter {
            it.nombreComercio.lowercase(Locale.ROOT).contains(query.lowercase(Locale.ROOT)) ||
                    it.categoria.lowercase(Locale.ROOT).contains(query.lowercase(Locale.ROOT))
        }
    }
}