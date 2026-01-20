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

    // Variables para guardar el estado de los filtros
    private var listaOriginal: List<Gasto> = emptyList()
    private var filtroTexto: String = ""
    private var filtroCategoria: String? = null // Null significa "Todas"

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        // Carga inicial
        listaOriginal = FakeRepository.getAllGastos()
        aplicarFiltros()
    }

    fun agregarGasto(gasto: Gasto) {
        FakeRepository.addGasto(gasto)
        listaOriginal = FakeRepository.getAllGastos()
        aplicarFiltros() // Re-aplicar filtros al añadir
    }

    fun eliminarGastosSeleccionados(ids: List<String>) {
        val nuevaLista = listaOriginal.toMutableList()
        nuevaLista.removeAll { it.id in ids }
        listaOriginal = nuevaLista
        aplicarFiltros()
    }

    // --- LÓGICA DE FILTRADO ---

    fun setFiltroTexto(texto: String) {
        filtroTexto = texto
        aplicarFiltros()
    }

    fun setFiltroCategoria(categoria: String?) {
        filtroCategoria = categoria
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        var listaFiltrada = listaOriginal

        // 1. Filtro por Texto (Buscador)
        if (filtroTexto.isNotEmpty()) {
            listaFiltrada = listaFiltrada.filter {
                it.nombreComercio.contains(filtroTexto, ignoreCase = true) ||
                        it.categoria.contains(filtroTexto, ignoreCase = true)
            }
        }

        // 2. Filtro por Categoría (Chip)
        if (filtroCategoria != null && filtroCategoria != "Todas") {
            listaFiltrada = listaFiltrada.filter {
                it.categoria.equals(filtroCategoria, ignoreCase = true)
            }
        }

        // Actualizamos la UI
        _gastos.value = listaFiltrada
        calcularTotales(listaFiltrada)
    }

    private fun calcularTotales(lista: List<Gasto>) {
        _totalMes.value = lista.sumOf { it.monto }
        _totalPendiente.value = lista.filter {
            it.estado == EstadoGasto.PENDIENTE || it.estado == EstadoGasto.PROCESANDO
        }.sumOf { it.monto }
    }
}