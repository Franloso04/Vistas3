package com.example.vistas

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vistas.data.FirestoreRepository
import com.example.vistas.model.EstadoGasto
import com.example.vistas.model.Gasto
import com.example.vistas.model.Reporte
import com.google.firebase.auth.FirebaseAuth

class MainViewModel : ViewModel() {

    private val repository = FirestoreRepository()
    private val auth = FirebaseAuth.getInstance()

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

    private var listaMaestra: List<Gasto> = emptyList()
    var isAdmin = false

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
            repository.getReportes { listaReportes ->
                _reportes.value = listaReportes
            }
        } else {
            repository.getMyGastos(userId, callback)
        }
    }


    fun subirImagenTicket(uri: Uri, onComplete: (String) -> Unit, onError: (Exception) -> Unit) {
        repository.uploadImagen(uri, 
            onSuccess = { url -> onComplete(url) },
            onFailure = { e -> onError(e) }
        )
    }




    fun agregarGasto(gasto: Gasto) {
        val user = auth.currentUser
        val gastoReal = gasto.copy(userId = user?.uid ?: "", emailUsuario = user?.email ?: "")
        repository.addGasto(gastoReal, {}, {})
    }

    fun eliminarGastosSeleccionados(ids: List<String>) { ids.forEach { repository.deleteGasto(it) } }
    fun aprobarGasto(id: String) { if (isAdmin) repository.updateEstado(id, EstadoGasto.APROBADO) }
    fun rechazarGasto(id: String) { if (isAdmin) repository.updateEstado(id, EstadoGasto.RECHAZADO) }
    fun eliminarGastoIndividual(id: String) { repository.deleteGasto(id) }

    fun tieneIncidencia(gastoId: String): Reporte? {
        return _reportes.value?.find { it.gastoId == gastoId }
    }

    fun enviarReporteFirebase(gasto: Gasto, descripcion: String) {
        val user = auth.currentUser ?: return
        val reporteMap = hashMapOf(
            "userId" to user.uid,
            "emailUsuario" to (user.email ?: "Desconocido"),
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

    private fun actualizarUI() {
        _gastosGlobales.value = listaMaestra
        _totalMes.value = listaMaestra.sumOf { it.importe }
        _totalPendiente.value = listaMaestra.filter { it.estado == EstadoGasto.PENDIENTE }.sumOf { it.importe }

        _statsCategorias.value = listaMaestra.groupBy { it.categoria }
            .mapValues { entry -> entry.value.sumOf { it.importe } }
        _statsEmpleados.value = listaMaestra.groupBy { it.emailUsuario }
            .mapValues { entry -> entry.value.sumOf { it.importe } }

        aplicarFiltros()
    }

    fun filtrarPorTexto(q: String) { busquedaActual = q; aplicarFiltros() }
    fun filtrarPorCategoria(c: String) { categoriaActual = c; aplicarFiltros() }
    fun filtrarPorEstado(e: String) { estadoActual = e; aplicarFiltros() }
    fun ordenarPorFecha(reciente: Boolean) { ordenMasReciente = reciente; aplicarFiltros() }

    private fun aplicarFiltros() {
        var res = listaMaestra
        val currentUid = auth.currentUser?.uid

        if (currentUid != null) {
            res = res.filter { it.userId == currentUid }
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
        res = if (ordenMasReciente) res.sortedByDescending { it.timestamp } else res.sortedBy { it.timestamp }
        _gastosFiltrados.value = res
    }
}
