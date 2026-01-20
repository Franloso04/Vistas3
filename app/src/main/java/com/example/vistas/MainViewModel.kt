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

    // LIVEDATA
    private val _gastosGlobales = MutableLiveData<List<Gasto>>()
    val gastosGlobales: LiveData<List<Gasto>> = _gastosGlobales

    private val _gastosFiltrados = MutableLiveData<List<Gasto>>()
    val gastosFiltrados: LiveData<List<Gasto>> = _gastosFiltrados

    private val _totalMes = MutableLiveData<Double>()
    val totalMes: LiveData<Double> = _totalMes
    private val _totalPendiente = MutableLiveData<Double>()
    val totalPendiente: LiveData<Double> = _totalPendiente

    // ESTADO INTERNO
    private var listaMaestra: List<Gasto> = emptyList()
    var isAdmin = false // Público para que la UI sepa si mostrar botones de Admin

    // FILTROS
    private var busquedaActual = ""
    private var categoriaActual = "Todas"
    private var estadoActual = "Todos"
    private var ordenMasReciente = true

    init {
        detectarRolYCargar()
    }

    // --- ESTA ES LA FUNCIÓN QUE FALTABA ---
    fun recargarSesion() {
        Log.d("VIEWMODEL", "Recargando sesión tras Login...")
        detectarRolYCargar()
    }

    private fun detectarRolYCargar() {
        val user = auth.currentUser
        if (user != null) {
            isAdmin = user.email?.lowercase()?.contains("admin") == true
            Log.d("VIEWMODEL", "Usuario: ${user.email} | Es Admin: $isAdmin")
            cargarDatos(user.uid)
        } else {
            Log.e("VIEWMODEL", "No hay usuario logueado. Esperando login...")
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
        if (user != null) {
            val gastoReal = gasto.copy(
                userId = user.uid,
                emailUsuario = user.email ?: "Desconocido"
            )
            repository.addGasto(gastoReal,
                onSuccess = { Log.d("VIEWMODEL", "Gasto añadido ok") },
                onFailure = { Log.e("VIEWMODEL", "Error añadiendo") }
            )
        }
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

    // --- UI Y FILTROS ---
    private fun actualizarUI() {
        _gastosGlobales.value = listaMaestra
        _totalMes.value = listaMaestra.sumOf { it.monto }
        _totalPendiente.value = listaMaestra.filter {
            it.estado == EstadoGasto.PENDIENTE || it.estado == EstadoGasto.PROCESANDO
        }.sumOf { it.monto }
        aplicarFiltros()
    }

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