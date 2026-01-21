package com.example.vistas

import android.util.Log
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

    // --- LIVE DATA (Observables) ---
    private val _gastosGlobales = MutableLiveData<List<Gasto>>()
    val gastosGlobales: LiveData<List<Gasto>> = _gastosGlobales

    private val _gastosFiltrados = MutableLiveData<List<Gasto>>()
    val gastosFiltrados: LiveData<List<Gasto>> = _gastosFiltrados

    private val _totalMes = MutableLiveData<Double>()
    val totalMes: LiveData<Double> = _totalMes

    private val _totalPendiente = MutableLiveData<Double>()
    val totalPendiente: LiveData<Double> = _totalPendiente

    // --- VARIABLES INTERNAS ---
    private var listaMaestra: List<Gasto> = emptyList()
    var isAdmin = false

    // --- VARIABLES DE FILTRO ---
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
            // Si el email contiene "admin", activamos modo Admin
            isAdmin = user.email?.lowercase()?.contains("admin") == true
            cargarDatos(user.uid)
        }
    }

    private fun cargarDatos(userId: String) {
        val callback = { lista: List<Gasto> ->
            listaMaestra = lista
            actualizarUI() // Esto refresca Dashboard y Totales
        }

        if (isAdmin) {
            repository.getAllGastos(callback)
        } else {
            repository.getMyGastos(userId, callback)
        }
    }

    // --- ACCIONES DE USUARIO (Añadir / Borrar Varios) ---
    fun agregarGasto(gasto: Gasto) {
        val user = auth.currentUser
        val gastoReal = gasto.copy(userId = user?.uid ?: "", emailUsuario = user?.email ?: "")
        repository.addGasto(gastoReal, {}, {})
    }

    fun eliminarGastosSeleccionados(ids: List<String>) {
        ids.forEach { repository.deleteGasto(it) }
    }

    // --- ACCIONES DE ADMIN (Las que te daban error rojo) ---
    fun aprobarGasto(id: String) {
        if (isAdmin) repository.updateEstado(id, EstadoGasto.APROBADO)
    }

    fun rechazarGasto(id: String) {
        if (isAdmin) repository.updateEstado(id, EstadoGasto.RECHAZADO)
    }

    fun eliminarGastoIndividual(id: String) {
        repository.deleteGasto(id)
    }

    // --- ACTUALIZACIÓN UI Y CÁLCULOS ---
    private fun actualizarUI() {
        _gastosGlobales.value = listaMaestra
        _totalMes.value = listaMaestra.sumOf { it.monto }
        _totalPendiente.value = listaMaestra.filter {
            it.estado == EstadoGasto.PENDIENTE || it.estado == EstadoGasto.PROCESANDO
        }.sumOf { it.monto }

        // Aplicamos filtros para actualizar la lista del Historial
        aplicarFiltros()
    }

    // --- LÓGICA DE FILTROS (Para el Historial) ---
    fun filtrarPorTexto(q: String) { busquedaActual = q; aplicarFiltros() }
    fun filtrarPorCategoria(c: String) { categoriaActual = c; aplicarFiltros() }
    fun filtrarPorEstado(e: String) { estadoActual = e; aplicarFiltros() }
    fun ordenarPorFecha(reciente: Boolean) { ordenMasReciente = reciente; aplicarFiltros() }

    private fun aplicarFiltros() {
        var res = listaMaestra

        // 1. Texto
        if (busquedaActual.isNotEmpty()) {
            res = res.filter {
                it.nombreComercio.contains(busquedaActual, true) ||
                        it.emailUsuario.contains(busquedaActual, true)
            }
        }
        // 2. Categoría
        if (categoriaActual != "Todas" && categoriaActual != "Categoría") {
            res = res.filter { it.categoria.equals(categoriaActual, true) }
        }
        // 3. Estado
        if (estadoActual != "Todos" && estadoActual != "Estado") {
            res = res.filter { it.estado.name.equals(estadoActual, true) }
        }
        // 4. Orden
        res = if (ordenMasReciente) res.sortedByDescending { it.timestamp } else res.sortedBy { it.timestamp }

        _gastosFiltrados.value = res
    }
}