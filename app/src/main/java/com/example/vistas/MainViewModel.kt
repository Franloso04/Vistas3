package com.example.vistas

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vistas.data.repository.AppRepository
import com.example.vistas.model.Empleado
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel : ViewModel() {

    private val repository = AppRepository()

    // Estado del usuario logueado
    private val _empleadoSesion = MutableLiveData<Empleado?>()
    val empleadoSesion: LiveData<Empleado?> = _empleadoSesion

    // Estado de carga (para mostrar ProgressBar)
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Mensajes de error/éxito para mostrar Toast en Fragment
    private val _mensajeOp = MutableLiveData<String?>()
    val mensajeOp: LiveData<String?> = _mensajeOp

    // --- LOGIN ---
    fun realizarLogin(email: String, pass: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val resultado = repository.login(email, pass)
            _isLoading.value = false

            resultado.onSuccess { empleado ->
                _empleadoSesion.value = empleado
                _mensajeOp.value = "Bienvenido ${empleado.nombre}"
            }.onFailure { error ->
                _mensajeOp.value = "Error: ${error.message}"
            }
        }
    }

    // --- SUBIR GASTO ---
    fun subirGasto(
        categoria: String,
        fecha: String, // YYYY-MM-DD
        importe: Double,
        comercio: String,
        archivo: File
    ) {
        val emp = _empleadoSesion.value
        if (emp == null) {
            _mensajeOp.value = "Error: No hay sesión iniciada"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val resultado = repository.subirGasto(
                idEmpleado = emp.id,
                idSeccion = emp.seccion,
                categoria = categoria,
                fecha = fecha,
                importe = importe.toString(),
                comercio = comercio,
                archivoFoto = archivo
            )
            _isLoading.value = false

            resultado.onSuccess { msg ->
                _mensajeOp.value = "ÉXITO: $msg"
            }.onFailure { error ->
                _mensajeOp.value = "FALLO: ${error.message}"
            }
        }
    }

    // Limpiar mensaje tras mostrarlo
    fun limpiarMensaje() {
        _mensajeOp.value = null
    }
}