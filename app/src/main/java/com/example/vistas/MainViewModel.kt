package com.example.vistas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vistas.data.api.ApiService
import com.example.vistas.data.api.RetrofitInstance
import com.example.vistas.data.repository.AuthRepository
import com.example.vistas.data.repository.GastosRepository
import com.example.vistas.model.Empleado
import com.example.vistas.model.Gasto
import com.example.vistas.model.Reporte
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel : ViewModel() {

    private val api = RetrofitInstance.api
    private val authRepo = AuthRepository(api as ApiService)
    private val gastosRepo = GastosRepository(api as ApiService)

    private val _empleado = MutableStateFlow<Empleado?>(null)
    val empleado: StateFlow<Empleado?> = _empleado

    private val _gastos = MutableStateFlow<List<Gasto>>(emptyList())
    val gastos: StateFlow<List<Gasto>> = _gastos

    private val _reporte = MutableStateFlow<Reporte?>(null)
    val reporte: StateFlow<Reporte?> = _reporte

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _empleado.value = authRepo.login(email, password)
        }
    }

    fun cargarGastos() {
        val emp = _empleado.value ?: return
        viewModelScope.launch {
            _gastos.value = gastosRepo.obtenerGastos(emp.id)
        }
    }

    fun cargarDashboard() {
        val emp = _empleado.value ?: return
        viewModelScope.launch {
            _reporte.value = gastosRepo.obtenerResumen(emp.id)
        }
    }

    fun aprobarGasto(id: Long) {
        viewModelScope.launch {
            gastosRepo.cambiarEstado(id, "aprobado")
            cargarGastos()
        }
    }

    fun rechazarGasto(id: Long) {
        viewModelScope.launch {
            gastosRepo.cambiarEstado(id, "rechazado")
            cargarGastos()
        }
    }
}
