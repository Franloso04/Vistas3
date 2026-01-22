package com.example.vistas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vistas.data.FirestoreRepository
import com.example.vistas.model.EstadoGasto
import com.example.vistas.model.Gasto
import com.example.vistas.model.Reporte // Asegúrate de tener este import
import com.google.firebase.auth.FirebaseAuth

class MainViewModel : ViewModel() {

    private val repository = FirestoreRepository()
    private val auth = FirebaseAuth.getInstance()

    // --- LIVE DATA ---
    // 1. GLOBAL (Para AdminFragment y Dashboard - Contiene TODO si eres admin)
    private val _gastosGlobales = MutableLiveData<List<Gasto>>()
    val gastosGlobales: LiveData<List<Gasto>> = _gastosGlobales

    // 2. FILTRADO (Para ExpensesFragment - Historial - AHORA SOLO TUS TICKETS)
    private val _gastosFiltrados = MutableLiveData<List<Gasto>>()
    val gastosFiltrados: LiveData<List<Gasto>> = _gastosFiltrados

    // 3. REPORTES (NUEVO: Para que el Admin vea las incidencias)
    private val _reportes = MutableLiveData<List<Reporte>>()
    val reportes: LiveData<List<Reporte>> = _reportes

    // 4. TOTALES (Globales para Admin, Personales para Empleado)
    private val _totalMes = MutableLiveData<Double>()
    val totalMes: LiveData<Double> = _totalMes

    private val _totalPendiente = MutableLiveData<Double>()
    val totalPendiente: LiveData<Double> = _totalPendiente

    // 5. ESTADÍSTICAS (Para los gráficos del Dashboard)
    private val _statsCategorias = MutableLiveData<Map<String, Double>>()
    val statsCategorias: LiveData<Map<String, Double>> = _statsCategorias

    private val _statsEmpleados = MutableLiveData<Map<String, Double>>()
    val statsEmpleados: LiveData<Map<String, Double>> = _statsEmpleados

    // --- VARIABLES INTERNAS ---
    private var listaMaestra: List<Gasto> = emptyList()
    var isAdmin = false

    // Filtros de UI
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

        // Si es Admin descargamos TODO para poder calcular los totales de la empresa
        if (isAdmin) {
            repository.getAllGastos(callback)

            // NUEVO: Si es admin, también descargamos los reportes de incidencias
            repository.getReportes { listaReportes ->
                _reportes.value = listaReportes
            }
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

    // --- NUEVO: FUNCIÓN AUXILIAR PARA VERIFICAR INCIDENCIAS ---
    fun tieneIncidencia(gastoId: String): Reporte? {
        return _reportes.value?.find { it.gastoId == gastoId }
    }

    // --- ACCIÓN REPORTE (USUARIO) ---
    fun enviarReporteFirebase(gasto: Gasto, descripcion: String) {
        val user = auth.currentUser ?: return

        // Creamos un mapa con todos los datos necesarios
        val reporteMap = hashMapOf(
            "userId" to user.uid,
            "emailUsuario" to (user.email ?: "Desconocido"),
            "gastoId" to gasto.id,
            "comercio" to gasto.nombreComercio,
            "monto" to gasto.monto,
            "descripcion" to descripcion,
            "fechaReporte" to System.currentTimeMillis(), // Fecha de hoy en milisegundos
            "estado" to "PENDIENTE" // Estado inicial del reporte
        )

        // Llamamos al repositorio
        repository.addReporte(reporteMap)
    }

    // --- CÁLCULOS Y ACTUALIZACIÓN ---
    private fun actualizarUI() {
        // 1. Actualizamos la lista GLOBAL (Admin Panel la necesita completa)
        _gastosGlobales.value = listaMaestra

        // 2. Calculamos Totales (Si es Admin, esto suma toda la empresa)
        _totalMes.value = listaMaestra.sumOf { it.monto }
        _totalPendiente.value = listaMaestra.filter {
            it.estado == EstadoGasto.PENDIENTE || it.estado == EstadoGasto.PENDIENTE
        }.sumOf { it.monto }

        // 3. Estadísticas para Gráficos
        _statsCategorias.value = listaMaestra.groupBy { it.categoria }
            .mapValues { entry -> entry.value.sumOf { it.monto } }

        _statsEmpleados.value = listaMaestra.groupBy { it.emailUsuario }
            .mapValues { entry -> entry.value.sumOf { it.monto } }

        // 4. Actualizar el Historial (Aquí aplicamos el filtro personal)
        aplicarFiltros()
    }

    // --- LÓGICA DE FILTROS ---
    fun filtrarPorTexto(q: String) { busquedaActual = q; aplicarFiltros() }
    fun filtrarPorCategoria(c: String) { categoriaActual = c; aplicarFiltros() }
    fun filtrarPorEstado(e: String) { estadoActual = e; aplicarFiltros() }
    fun ordenarPorFecha(reciente: Boolean) { ordenMasReciente = reciente; aplicarFiltros() }

    private fun aplicarFiltros() {
        var res = listaMaestra
        val currentUid = auth.currentUser?.uid

        // --- CLAVE: SIEMPRE FILTRAR POR USUARIO EN EL HISTORIAL ---
        if (currentUid != null) {
            res = res.filter { it.userId == currentUid }
        }

        // Filtro Texto
        if (busquedaActual.isNotEmpty()) {
            res = res.filter {
                it.nombreComercio.contains(busquedaActual, true) ||
                        it.categoria.contains(busquedaActual, true)
            }
        }
        // Filtro Categoría
        if (categoriaActual != "Todas" && categoriaActual != "Categoría") {
            res = res.filter { it.categoria.equals(categoriaActual, true) }
        }
        // Filtro Estado
        if (estadoActual != "Todos" && estadoActual != "Estado") {
            res = res.filter { it.estado.name.equals(estadoActual, true) }
        }
        // Orden
        res = if (ordenMasReciente) res.sortedByDescending { it.timestamp } else res.sortedBy { it.timestamp }

        // Enviamos la lista filtrada (PERSONAL) al Historial
        _gastosFiltrados.value = res
    }
}