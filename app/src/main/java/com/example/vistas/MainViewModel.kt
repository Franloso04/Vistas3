package com.example.vistas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vistas.data.repository.AppRepository
import com.example.vistas.model.Empleado
import com.example.vistas.model.EstadoGasto
import com.example.vistas.model.Gasto
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel : ViewModel() {

    private val repository = AppRepository()

    // Sesión
    private val _empleadoSesion = MutableLiveData<Empleado?>()
    val empleadoSesion: LiveData<Empleado?> = _empleadoSesion

    // Estado UI
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _mensajeOp = MutableLiveData<String?>()
    val mensajeOp: LiveData<String?> = _mensajeOp

    // Datos
    private var listaMaestra: List<Gasto> = emptyList()

    private val _gastosGlobales = MutableLiveData<List<Gasto>>()
    val gastosGlobales: LiveData<List<Gasto>> = _gastosGlobales

    private val _gastosFiltrados = MutableLiveData<List<Gasto>>()
    val gastosFiltrados: LiveData<List<Gasto>> = _gastosFiltrados



    // Dashboard
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
        recargarSesion()
    }

    fun cerrarSesion() {
        _empleadoSesion.value = null
        listaMaestra = emptyList()
        actualizarUI()
    }

    fun recargarSesion() {
        val empId = _empleadoSesion.value?.id ?: return

        viewModelScope.launch {
            listaMaestra = repository.getGastos(empId)
            actualizarUI()
        }
    }

    private fun actualizarUI() {
        _gastosGlobales.value = listaMaestra
        aplicarFiltros()

        val listaDash = listaMaestra

        _totalMes.value = listaDash.sumOf { it.monto ?: 0.0 }

        _totalPendiente.value = listaDash
            .filter { (it.estado ?: EstadoGasto.PENDIENTE) == EstadoGasto.PENDIENTE }
            .sumOf { it.monto ?: 0.0 }

        _statsCategorias.value = listaDash
            .groupBy { it.categoria ?: "Otros" }
            .mapValues { it.value.sumOf { g -> g.monto ?: 0.0 } }

        _statsEmpleados.value = listaDash
            .groupBy { it.emailUsuario }
            .mapValues { it.value.sumOf { g -> g.monto ?: 0.0 } }
    }

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

    fun eliminarGastoIndividual(id: String) {
        viewModelScope.launch {
            repository.borrarGasto(id)
            listaMaestra = listaMaestra.filter { it.id != id }
            actualizarUI()
        }
    }

    fun eliminarGastosSeleccionados(ids: List<String>) {
        viewModelScope.launch {
            ids.forEach { id ->
                repository.borrarGasto(id)
                listaMaestra = listaMaestra.filter { it.id != id }
            }
            actualizarUI()
        }
    }


    fun enviarReporteFirebase(gasto: Gasto, desc: String) {
        _mensajeOp.value = "Reporte enviado correctamente"
    }

    fun limpiarMensaje() { _mensajeOp.value = null }


    fun filtrarPorTexto(q: String) { busquedaActual = q; aplicarFiltros() }
    fun filtrarPorCategoria(c: String) { categoriaActual = c; aplicarFiltros() }
    fun filtrarPorEstado(e: String) { estadoActual = e; aplicarFiltros() }
    fun ordenarPorFecha(r: Boolean) { ordenMasReciente = r; aplicarFiltros() }

    private fun aplicarFiltros() {
        var lista = listaMaestra

        if (busquedaActual.isNotEmpty()) {
            lista = lista.filter { it.nombreComercio?.contains(busquedaActual, true) == true }
        }

        if (categoriaActual != "Todas") {
            lista = lista.filter { it.categoria?.equals(categoriaActual, true) == true }
        }

        if (estadoActual != "Todos") {
            lista = lista.filter { it.estado?.name?.equals(estadoActual, true) == true }
        }

        lista = if (ordenMasReciente) {
            lista.sortedByDescending { it.timestamp }
        } else {
            lista.sortedBy { it.timestamp }
        }

        _gastosFiltrados.value = lista
    }
}