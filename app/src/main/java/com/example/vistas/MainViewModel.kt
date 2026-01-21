package com.example.vistas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vistas.data.FirestoreRepository
import com.example.vistas.model.EstadoGasto
import com.example.vistas.model.Gasto
import com.google.firebase.auth.FirebaseAuth

class MainViewModel : ViewModel() {

    private val repository = FirestoreRepository()
    private val auth = FirebaseAuth.getInstance()

    // --- LIVE DATA BÁSICOS ---
    private val _gastosGlobales = MutableLiveData<List<Gasto>>()
    val gastosGlobales: LiveData<List<Gasto>> = _gastosGlobales

    private val _gastosFiltrados = MutableLiveData<List<Gasto>>()
    val gastosFiltrados: LiveData<List<Gasto>> = _gastosFiltrados

    private val _totalMes = MutableLiveData<Double>()
    val totalMes: LiveData<Double> = _totalMes

    private val _totalPendiente = MutableLiveData<Double>()
    val totalPendiente: LiveData<Double> = _totalPendiente

    // --- NUEVOS LIVE DATA (ESTADÍSTICAS) ---
    // Mapa: "Comida" -> 150.00
    private val _statsCategorias = MutableLiveData<Map<String, Double>>()
    val statsCategorias: LiveData<Map<String, Double>> = _statsCategorias

    // Mapa: "paco@gmail.com" -> 500.00
    private val _statsEmpleados = MutableLiveData<Map<String, Double>>()
    val statsEmpleados: LiveData<Map<String, Double>> = _statsEmpleados

    // --- VARIABLES INTERNAS ---
    private var listaMaestra: List<Gasto> = emptyList()
    var isAdmin = false

    // Filtros
    private var busquedaActual = ""
    private var categoriaActual = "Todas"
    private var estadoActual = "Todos"
    private var ordenMasReciente = true

    init {
        detectarRolYCargar()
    }

    fun recargarSesion() { detectarRolYCargar() }

    private fun detectarRolYCargar() {
        val user = auth.currentUser
        if (user != null) {
            isAdmin = user.email?.lowercase()?.contains("admin") == true
            cargarDatos(user.uid)
        }
    }

    private fun cargarDatos(userId: String) {
        val callback = { lista: List<Gasto> ->
            listaMaestra = lista
            actualizarUI()
        }

        if (isAdmin) {
            repository.getAllGastos(callback)
        } else {
            repository.getMyGastos(userId, callback)
        }
    }

    // --- ACCIONES ---
    fun agregarGasto(gasto: Gasto) {
        val user = auth.currentUser
        val gastoReal = gasto.copy(userId = user?.uid ?: "", emailUsuario = user?.email ?: "")
        repository.addGasto(gastoReal, {}, {})
    }

    fun eliminarGastosSeleccionados(ids: List<String>) { ids.forEach { repository.deleteGasto(it) } }
    fun aprobarGasto(id: String) { if (isAdmin) repository.updateEstado(id, EstadoGasto.APROBADO) }
    fun rechazarGasto(id: String) { if (isAdmin) repository.updateEstado(id, EstadoGasto.RECHAZADO) }
    fun eliminarGastoIndividual(id: String) { repository.deleteGasto(id) }

    // --- CÁLCULOS Y UI ---
    private fun actualizarUI() {
        // 1. Totales Generales
        _gastosGlobales.value = listaMaestra
        _totalMes.value = listaMaestra.sumOf { it.monto }
        _totalPendiente.value = listaMaestra.filter {
            it.estado == EstadoGasto.PENDIENTE || it.estado == EstadoGasto.PROCESANDO
        }.sumOf { it.monto }

        // 2. Agrupación por CATEGORÍA (Para el gráfico/lista)
        // Agrupa por nombre de categoría y suma los montos
        val mapCat = listaMaestra.groupBy { it.categoria }
            .mapValues { entry -> entry.value.sumOf { it.monto } }
        _statsCategorias.value = mapCat

        // 3. Agrupación por EMPLEADO (Solo útil si eres Admin)
        val mapEmp = listaMaestra.groupBy { it.emailUsuario }
            .mapValues { entry -> entry.value.sumOf { it.monto } }
        _statsEmpleados.value = mapEmp

        aplicarFiltros()
    }

    // --- LÓGICA DE FILTROS ---
    fun filtrarPorTexto(q: String) { busquedaActual = q; aplicarFiltros() }
    fun filtrarPorCategoria(c: String) { categoriaActual = c; aplicarFiltros() }
    fun filtrarPorEstado(e: String) { estadoActual = e; aplicarFiltros() }
    fun ordenarPorFecha(reciente: Boolean) { ordenMasReciente = reciente; aplicarFiltros() }

    private fun aplicarFiltros() {
        var res = listaMaestra
        if (busquedaActual.isNotEmpty()) {
            res = res.filter {
                it.nombreComercio.contains(busquedaActual, true) ||
                        it.emailUsuario.contains(busquedaActual, true)
            }
        }
        if (categoriaActual != "Todas" && categoriaActual != "Categoría") {
            res = res.filter { it.categoria.equals(categoriaActual, true) }
        }
        if (estadoActual != "Todos" && estadoActual != "Estado") {
            res = res.filter { it.estado.name.equals(estadoActual, true) }
        }
        res = if (ordenMasReciente) res.sortedByDescending { it.timestamp } else res.sortedBy { it.timestamp }
        _gastosFiltrados.value = res
    }
}