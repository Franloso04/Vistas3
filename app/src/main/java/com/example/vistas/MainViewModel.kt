package com.example.vistas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vistas.data.repository.AppRepository
import com.example.vistas.model.Empleado
import com.example.vistas.model.EstadoGasto
import com.example.vistas.model.Gasto
import com.example.vistas.model.Reporte
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel : ViewModel() {

    private val repository = AppRepository()

    // --- SESIÓN ---
    private val _empleadoSesion = MutableLiveData<Empleado?>()
    val empleadoSesion: LiveData<Empleado?> = _empleadoSesion
    var isAdmin = false
        private set

    // --- UI STATES ---
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _mensajeOp = MutableLiveData<String?>()
    val mensajeOp: LiveData<String?> = _mensajeOp

    // --- DATOS ---
    private var listaMaestra: List<Gasto> = emptyList()

    private val _gastosGlobales = MutableLiveData<List<Gasto>>()
    val gastosGlobales: LiveData<List<Gasto>> = _gastosGlobales

    private val _gastosFiltrados = MutableLiveData<List<Gasto>>()
    val gastosFiltrados: LiveData<List<Gasto>> = _gastosFiltrados

    private val _reportes = MutableLiveData<List<Reporte>>()
    val reportes: LiveData<List<Reporte>> = _reportes

    // Totales Dashboard
    private val _totalMes = MutableLiveData<Double>(0.0)
    val totalMes: LiveData<Double> = _totalMes
    private val _totalPendiente = MutableLiveData<Double>(0.0)
    val totalPendiente: LiveData<Double> = _totalPendiente
    private val _statsCategorias = MutableLiveData<Map<String, Double>>()
    val statsCategorias: LiveData<Map<String, Double>> = _statsCategorias
    private val _statsEmpleados = MutableLiveData<Map<String?, Double>>()
    val statsEmpleados: LiveData<Map<String?, Double>> = _statsEmpleados

    // Filtros
    private var busquedaActual = ""
    private var categoriaActual = "Todas"
    private var estadoActual = "Todos"
    private var ordenMasReciente = true

    // --- LOGIN ---
    fun realizarLogin(email: String, pass: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val res = repository.login(email, pass)
            _isLoading.value = false
            res.onSuccess { emp ->
                setEmpleadoSesion(emp)
                _mensajeOp.value = "Bienvenido ${emp.nombre}"
            }.onFailure {
                _mensajeOp.value = "Error: ${it.message}"
            }
        }
    }

    fun setEmpleadoSesion(empleado: Empleado) {
        _empleadoSesion.value = empleado
        isAdmin = empleado.privilegios == "1" || empleado.privilegiosGlobales == "1"
        recargarSesion()
    }

    fun cerrarSesion() {
        _empleadoSesion.value = null
        listaMaestra = emptyList()
        actualizarUI()
    }

    // --- CORRECCIÓN: Pasar ID al Repositorio ---
    fun recargarSesion() {
        val empId = _empleadoSesion.value?.id ?: return // Si no hay usuario, no cargamos nada

        viewModelScope.launch {
            // Llamamos a getGastos pasando el ID
            listaMaestra = repository.getGastos(empId)
            actualizarUI()

            if (isAdmin) repository.getReportes { _reportes.value = it }
        }
    }

    private fun actualizarUI() {
        _gastosGlobales.value = listaMaestra
        aplicarFiltros()

        val listaDash = if(isAdmin) listaMaestra else listaMaestra.filter { it.userId == _empleadoSesion.value?.id }

        _totalMes.value = listaDash.sumOf { it.monto }
        _totalPendiente.value = listaDash.filter { it.estado == EstadoGasto.PENDIENTE }.sumOf { it.monto }
        _statsCategorias.value = listaDash.groupBy { it.categoria }.mapValues { it.value.sumOf { g -> g.monto } }
        _statsEmpleados.value = listaDash.groupBy { it.emailUsuario }.mapValues { it.value.sumOf { g -> g.monto } }
    }

    // --- ACCIONES ---
    fun subirGasto(cat: String, fecha: String, imp: Double, com: String, file: File) {
        val emp = _empleadoSesion.value ?: return
        _isLoading.value = true
        viewModelScope.launch {
            val res = repository.subirGasto(emp.id, emp.seccion, cat, fecha, imp.toString(), com, file)
            _isLoading.value = false
            res.onSuccess {
                _mensajeOp.value = "Subido con éxito"
                recargarSesion()
            }.onFailure { _mensajeOp.value = "Error: ${it.message}" }
        }
    }

    fun aprobarGasto(id: String) { viewModelScope.launch { repository.cambiarEstado(id, EstadoGasto.APROBADO); recargarSesion() } }
    fun rechazarGasto(id: String) { viewModelScope.launch { repository.cambiarEstado(id, EstadoGasto.RECHAZADO); recargarSesion() } }
    fun eliminarGastoIndividual(id: String) { viewModelScope.launch { repository.borrarGasto(id); recargarSesion() } }

    fun eliminarGastosSeleccionados(ids: List<String>) {
        viewModelScope.launch {
            ids.forEach { repository.borrarGasto(it) }
            recargarSesion()
        }
    }

    fun eliminarReporte(id: String) {
        repository.deleteReporte(id)
        val actual = _reportes.value.orEmpty().toMutableList()
        actual.removeAll { it.id == id }
        _reportes.value = actual
    }

    fun tieneIncidencia(gastoId: String): Reporte? = _reportes.value?.find { it.gastoId == gastoId }
    fun enviarReporteFirebase(gasto: Gasto, desc: String) { _mensajeOp.value = "Reporte simulado" }
    fun limpiarMensaje() { _mensajeOp.value = null }

    // --- FILTROS ---
    fun filtrarPorTexto(q: String) { busquedaActual = q; aplicarFiltros() }
    fun filtrarPorCategoria(c: String) { categoriaActual = c; aplicarFiltros() }
    fun filtrarPorEstado(e: String) { estadoActual = e; aplicarFiltros() }
    fun ordenarPorFecha(r: Boolean) { ordenMasReciente = r; aplicarFiltros() }

    private fun aplicarFiltros() {
        var lista = listaMaestra
        val empId = _empleadoSesion.value?.id

        // Si no es Admin, filtramos solo lo suyo (aunque la API ya filtra, lo mantenemos por seguridad)
        if (!isAdmin && empId != null) {
            lista = lista.filter { it.userId == empId }
        }

        if (busquedaActual.isNotEmpty()) lista = lista.filter { it.nombreComercio.contains(busquedaActual, true) }
        if (categoriaActual != "Todas") lista = lista.filter { it.categoria.equals(categoriaActual, true) }
        if (estadoActual != "Todos") lista = lista.filter { it.estado.name.equals(estadoActual, true) }

        lista = if (ordenMasReciente) lista.sortedByDescending { it.timestamp } else lista.sortedBy { it.timestamp }
        _gastosFiltrados.value = lista
    }
}