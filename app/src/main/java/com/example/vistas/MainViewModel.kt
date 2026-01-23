package com.example.vistas

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vistas.data.repository.FirestoreRepository
import com.example.vistas.model.EstadoGasto
import com.example.vistas.model.Gasto
import com.example.vistas.model.Reporte
import com.example.vistas.model.Empleado

class MainViewModel : ViewModel() {

    private val repository = FirestoreRepository()

    // 🔐 SESIÓN EMPLEADO (API)
    private val _empleado = MutableLiveData<Empleado?>()
    val empleado: LiveData<Empleado?> = _empleado

    var isAdmin = false
        private set

    // ======================
    // LIVE DATA EXISTENTES
    // ======================

    private val _gastosGlobales = MutableLiveData<List<Gasto>>()
    val gastosGlobales: LiveData<List<Gasto>> = _gastosGlobales

    private val _gastosFiltrados = MutableLiveData<List<Gasto>>()
    val gastosFiltrados: LiveData<List<Gasto>> = _gastosFiltrados

    private val _reportes = MutableLiveData<List<Reporte>>()
    val reportes: LiveData<List<Reporte>> = _reportes

    private val _totalMes = MutableLiveData<Double>()
    val totalMes: LiveData<Double> = _totalMes

    private val _totalPendiente = MutableLiveData<Double>()
    val totalPendiente: LiveData<Double> = _totalPendiente

    private val _statsCategorias = MutableLiveData<Map<String, Double>>()
    val statsCategorias: LiveData<Map<String, Double>> = _statsCategorias

    private val _statsEmpleados = MutableLiveData<Map<String, Double>>()
    val statsEmpleados: LiveData<Map<String, Double>> = _statsEmpleados

    // ======================
    // ESTADO INTERNO
    // ======================

    private var listaMaestra: List<Gasto> = emptyList()

    private var busquedaActual = ""
    private var categoriaActual = "Todas"
    private var estadoActual = "Todos"
    private var ordenMasReciente = true

    // ======================
    // SESIÓN
    // ======================

    fun setEmpleadoSesion(empleado: Empleado) {
        _empleado.value = empleado

        // 🔑 REGLA DE ADMIN
        isAdmin = empleado.privilegios_globales == "1" || empleado.privilegios == "1"

        cargarDatosEmpleado()
    }

    fun cerrarSesion() {
        _empleado.value = null
        listaMaestra = emptyList()
        _gastosGlobales.value = emptyList()
        _gastosFiltrados.value = emptyList()
    }

    // ======================
    // CARGA DE DATOS
    // ======================

    private fun cargarDatosEmpleado() {
        val empleado = _empleado.value ?: return

        val callback = { lista: List<Gasto> ->
            listaMaestra = lista
            actualizarUI()
        }

        if (isAdmin) {
            repository.getAllGastos(callback)
            repository.getReportes { _reportes.value = it }
        } else {
            // ⚠️ usamos ID del empleado API
            repository.getMyGastos(empleado.id, callback)
        }
    }

    fun recargarSesion() {
        cargarDatosEmpleado()
    }

    // ======================
    // GASTOS
    // ======================

    fun subirImagenTicket(
        uri: Uri,
        onComplete: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        repository.uploadImagen(
            uri,
            onSuccess = onComplete,
            onFailure = onError
        )
    }

    fun agregarGasto(gasto: Gasto) {
        val emp = _empleado.value ?: return

        val gastoReal = gasto.copy(
            userId = emp.id,
            emailUsuario = emp.email
        )

        repository.addGasto(gastoReal, {}, {})
    }

    fun eliminarGastosSeleccionados(ids: List<String>) {
        ids.forEach { repository.deleteGasto(it) }
    }

    fun aprobarGasto(id: String) {
        if (isAdmin) repository.updateEstado(id, EstadoGasto.APROBADO)
    }

    fun rechazarGasto(id: String) {
        if (isAdmin) repository.updateEstado(id, EstadoGasto.RECHAZADO)
    }

    fun eliminarGastoIndividual(id: String) {
        repository.deleteGasto(id)
    }

    // ======================
    // REPORTES
    // ======================

    fun tieneIncidencia(gastoId: String): Reporte? {
        return _reportes.value?.find { it.gastoId == gastoId }
    }

    fun enviarReporteFirebase(gasto: Gasto, descripcion: String) {
        val emp = _empleado.value ?: return

        val reporteMap = hashMapOf(
            "userId" to emp.id,
            "emailUsuario" to emp.email,
            "gastoId" to gasto.id,
            "comercio" to gasto.nombreComercio,
            "importe" to gasto.importe,
            "descripcion" to descripcion,
            "fechaReporte" to System.currentTimeMillis(),
            "estado" to "PENDIENTE"
        )

        repository.addReporte(reporteMap)
    }

    fun eliminarReporte(id: String) {
        repository.deleteReporte(id)
        repository.getReportes { _reportes.value = it }
    }

    // ======================
    // UI + FILTROS
    // ======================

    private fun actualizarUI() {
        _gastosGlobales.value = listaMaestra
        _totalMes.value = listaMaestra.sumOf { it.importe }
        _totalPendiente.value =
            listaMaestra.filter { it.estado == EstadoGasto.PENDIENTE }
                .sumOf { it.importe }

        _statsCategorias.value =
            listaMaestra.groupBy { it.categoria }
                .mapValues { it.value.sumOf { g -> g.importe } }

        _statsEmpleados.value =
            listaMaestra.groupBy { it.emailUsuario }
                .mapValues { it.value.sumOf { g -> g.importe } }

        aplicarFiltros()
    }

    fun filtrarPorTexto(q: String) {
        busquedaActual = q
        aplicarFiltros()
    }

    fun filtrarPorCategoria(c: String) {
        categoriaActual = c
        aplicarFiltros()
    }

    fun filtrarPorEstado(e: String) {
        estadoActual = e
        aplicarFiltros()
    }

    fun ordenarPorFecha(reciente: Boolean) {
        ordenMasReciente = reciente
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        var res = listaMaestra
        val empId = _empleado.value?.id

        if (!isAdmin && empId != null) {
            res = res.filter { it.userId == empId }
        }

        if (busquedaActual.isNotEmpty()) {
            res = res.filter {
                it.nombreComercio.contains(busquedaActual, true) ||
                        it.categoria.contains(busquedaActual, true)
            }
        }

        if (categoriaActual != "Todas" && categoriaActual != "Categoría") {
            res = res.filter { it.categoria.equals(categoriaActual, true) }
        }

        if (estadoActual != "Todos" && estadoActual != "Estado") {
            res = res.filter { it.estado.name.equals(estadoActual, true) }
        }

        res = if (ordenMasReciente)
            res.sortedByDescending { it.timestamp }
        else
            res.sortedBy { it.timestamp }

        _gastosFiltrados.value = res
    }
}
