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

    private val _empleadoSesion = MutableLiveData<Empleado?>()
    val empleadoSesion: LiveData<Empleado?> = _empleadoSesion
    var isAdmin = false
        private set

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _mensajeOp = MutableLiveData<String?>()
    val mensajeOp: LiveData<String?> = _mensajeOp

    private var listaMaestra: List<Gasto> = emptyList()

    private val _gastosGlobales = MutableLiveData<List<Gasto>>()
    val gastosGlobales: LiveData<List<Gasto>> = _gastosGlobales

    private val _gastosFiltrados = MutableLiveData<List<Gasto>>()
    val gastosFiltrados: LiveData<List<Gasto>> = _gastosFiltrados

    private val _reportes = MutableLiveData<List<Reporte>>()
    val reportes: LiveData<List<Reporte>> = _reportes

    private val _reportesLocales = mutableListOf<Reporte>()

    private val _totalMes = MutableLiveData<Double>(0.0)
    val totalMes: LiveData<Double> = _totalMes
    private val _totalPendiente = MutableLiveData<Double>(0.0)
    val totalPendiente: LiveData<Double> = _totalPendiente
    private val _statsCategorias = MutableLiveData<Map<String, Double>>()
    val statsCategorias: LiveData<Map<String, Double>> = _statsCategorias
    private val _statsEmpleados = MutableLiveData<Map<String?, Double>>()
    val statsEmpleados: LiveData<Map<String?, Double>> = _statsEmpleados

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
        isAdmin = (empleado.privilegios == "1" || empleado.privilegiosGlobales == "1")
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

            if (isAdmin) {
                _reportes.value = _reportesLocales.toList()
            }
        }
    }

    private fun actualizarUI() {
        _gastosGlobales.value = listaMaestra
        aplicarFiltros()

        val listaDash = if(isAdmin) listaMaestra else listaMaestra.filter { it.userId == _empleadoSesion.value?.id }

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

    fun aprobarGasto(id: String) { viewModelScope.launch { repository.cambiarEstado(id, EstadoGasto.APROBADO); recargarSesion() } }
    fun rechazarGasto(id: String) { viewModelScope.launch { repository.cambiarEstado(id, EstadoGasto.RECHAZADO); recargarSesion() } }

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
        val reporte = Reporte(
            id = System.currentTimeMillis().toString(),
            gastoId = gasto.id,
            descripcion = desc,
            comercio = gasto.nombreComercio ?: "Comercio",
            emailUsuario = _empleadoSesion.value?.email ?: "usuario"
        )
        _reportesLocales.add(0, reporte)
        if (isAdmin) _reportes.value = _reportesLocales.toList()
        _mensajeOp.value = "Reporte registrado"
    }

    fun eliminarReporte(id: String) {
        _reportesLocales.removeAll { it.id == id }
        _reportes.value = _reportesLocales.toList()
    }

    fun tieneIncidencia(gastoId: String): Reporte? = _reportesLocales.find { it.gastoId == gastoId }
    fun limpiarMensaje() { _mensajeOp.value = null }

    fun filtrarPorTexto(q: String) { busquedaActual = q; aplicarFiltros() }
    fun filtrarPorCategoria(c: String) { categoriaActual = c; aplicarFiltros() }
    fun filtrarPorEstado(e: String) { estadoActual = e; aplicarFiltros() }
    fun ordenarPorFecha(r: Boolean) { ordenMasReciente = r; aplicarFiltros() }

    private fun aplicarFiltros() {
        var lista = listaMaestra
        val empId = _empleadoSesion.value?.id

        if (!isAdmin && empId != null) {
            lista = lista.filter { it.userId == empId }
        }

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