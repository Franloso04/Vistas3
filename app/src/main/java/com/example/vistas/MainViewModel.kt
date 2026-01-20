package com.example.vistas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vistas.data.FakeRepository
import com.example.vistas.model.Gasto
import com.example.vistas.model.EstadoGasto

class MainViewModel : ViewModel() {

    private val _gastos = MutableLiveData<List<Gasto>>()
    val gastos: LiveData<List<Gasto>> = _gastos

    private val _totalMes = MutableLiveData<Double>()
    val totalMes: LiveData<Double> = _totalMes

    private val _totalPendiente = MutableLiveData<Double>()
    val totalPendiente: LiveData<Double> = _totalPendiente

    private var listaMaestra: List<Gasto> = emptyList()

    // Estado de filtros
    private var busquedaActual = ""
    private var categoriaActual = "Todas"
    private var estadoActual = "Todos" // NUEVO
    private var ordenFechaAscendente = false // False = Más nuevo primero (Defecto)

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        listaMaestra = FakeRepository.getAllGastos()
        aplicarFiltros()
    }

    fun agregarGasto(gasto: Gasto) {
        FakeRepository.addGasto(gasto)
        listaMaestra = FakeRepository.getAllGastos()
        aplicarFiltros()
    }

    fun eliminarGastosSeleccionados(ids: List<String>) {
        val nuevaLista = listaMaestra.toMutableList()
        nuevaLista.removeAll { it.id in ids }
        listaMaestra = nuevaLista
        aplicarFiltros()
    }

    // --- NUEVAS FUNCIONES DE FILTRO ---

    fun filtrarPorTexto(query: String) {
        busquedaActual = query
        aplicarFiltros()
    }

    fun filtrarPorCategoria(categoria: String) {
        categoriaActual = categoria
        aplicarFiltros()
    }

    fun filtrarPorEstado(estado: String) {
        estadoActual = estado
        aplicarFiltros()
    }

    fun cambiarOrdenFecha(masAntiguoPrimero: Boolean) {
        ordenFechaAscendente = masAntiguoPrimero
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        var resultado = listaMaestra

        // 1. Texto
        if (busquedaActual.isNotEmpty()) {
            resultado = resultado.filter {
                it.nombreComercio.contains(busquedaActual, ignoreCase = true) ||
                        it.categoria.contains(busquedaActual, ignoreCase = true)
            }
        }

        // 2. Categoría
        if (categoriaActual != "Todas" && categoriaActual != "Categoría") {
            resultado = resultado.filter { it.categoria.equals(categoriaActual, ignoreCase = true) }
        }

        // 3. Estado (NUEVO)
        if (estadoActual != "Todos" && estadoActual != "Estado") {
            resultado = resultado.filter { it.estado.name.equals(estadoActual, ignoreCase = true) }
        }

        // 4. Ordenar (NUEVO)
        resultado = if (ordenFechaAscendente) {
            resultado.sortedBy { it.timestamp } // Más antiguo primero (ascendente)
        } else {
            resultado.sortedByDescending { it.timestamp } // Más nuevo primero
        }

        _gastos.value = resultado
        calcularTotales(listaMaestra) // Totales siempre sobre el global
    }

    private fun calcularTotales(lista: List<Gasto>) {
        _totalMes.value = lista.sumOf { it.monto }
        _totalPendiente.value = lista.filter {
            it.estado == EstadoGasto.PENDIENTE || it.estado == EstadoGasto.PROCESANDO
        }.sumOf { it.monto }
    }
}