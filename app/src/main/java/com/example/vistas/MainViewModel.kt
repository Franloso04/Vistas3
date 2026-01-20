package com.example.vistas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vistas.data.FakeRepository
import com.example.vistas.model.Gasto
import com.example.vistas.model.EstadoGasto

class MainViewModel : ViewModel() {

    // 1. DATA GLOBAL (Para Dashboard - Siempre tiene todo)
    private val _gastosGlobales = MutableLiveData<List<Gasto>>()
    val gastosGlobales: LiveData<List<Gasto>> = _gastosGlobales

    // 2. DATA FILTRADA (Para Historial - Cambia con los filtros)
    private val _gastosFiltrados = MutableLiveData<List<Gasto>>()
    val gastosFiltrados: LiveData<List<Gasto>> = _gastosFiltrados

    // Totales (Siempre calculados sobre el global)
    private val _totalMes = MutableLiveData<Double>()
    val totalMes: LiveData<Double> = _totalMes

    private val _totalPendiente = MutableLiveData<Double>()
    val totalPendiente: LiveData<Double> = _totalPendiente

    // Copia maestra
    private var listaMaestra: List<Gasto> = emptyList()

    // Variables de filtro
    private var busquedaActual = ""
    private var categoriaActual = "Todas"
    private var estadoActual = "Todos"
    private var ordenMasReciente = true

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        listaMaestra = FakeRepository.getAllGastos()
        actualizarGlobales()
        aplicarFiltros() // Inicializa la lista filtrada también
    }

    fun agregarGasto(gasto: Gasto) {
        FakeRepository.addGasto(gasto)
        listaMaestra = FakeRepository.getAllGastos()
        actualizarGlobales()
        aplicarFiltros()
    }

    fun eliminarGastosSeleccionados(ids: List<String>) {
        val nuevaLista = listaMaestra.toMutableList()
        nuevaLista.removeAll { it.id in ids }
        listaMaestra = nuevaLista
        actualizarGlobales()
        aplicarFiltros()
    }

    // Actualiza Dashboard y Totales
    private fun actualizarGlobales() {
        _gastosGlobales.value = listaMaestra

        _totalMes.value = listaMaestra.sumOf { it.monto }
        _totalPendiente.value = listaMaestra.filter {
            it.estado == EstadoGasto.PENDIENTE || it.estado == EstadoGasto.PROCESANDO
        }.sumOf { it.monto }
    }

    // --- LOGICA DE FILTROS (Solo afecta a _gastosFiltrados) ---

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

    fun ordenarPorFecha(masReciente: Boolean) {
        ordenMasReciente = masReciente
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        var resultado = listaMaestra

        if (busquedaActual.isNotEmpty()) {
            resultado = resultado.filter {
                it.nombreComercio.contains(busquedaActual, ignoreCase = true) ||
                        it.categoria.contains(busquedaActual, ignoreCase = true)
            }
        }

        if (categoriaActual != "Todas" && categoriaActual != "Categoría") {
            resultado = resultado.filter { it.categoria.equals(categoriaActual, ignoreCase = true) }
        }

        if (estadoActual != "Todos" && estadoActual != "Estado") {
            resultado = resultado.filter { it.estado.name.equals(estadoActual, ignoreCase = true) }
        }

        resultado = if (ordenMasReciente) {
            resultado.sortedByDescending { it.timestamp }
        } else {
            resultado.sortedBy { it.timestamp }
        }

        // AQUÍ ESTÁ LA CLAVE: Solo actualizamos la lista filtrada
        _gastosFiltrados.value = resultado
    }
}