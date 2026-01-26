package com.example.vistas.data.repository

import com.example.vistas.data.api.RetrofitClient
import com.example.vistas.model.Empleado
import com.example.vistas.model.EstadoGasto
import com.example.vistas.model.Gasto
import com.example.vistas.model.Reporte
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class AppRepository {
    private val api = RetrofitClient.instance
    // Tokens fijos (según tus instrucciones)
    private val T_LOGIN = "C2bbCO18omyhKbUQvha38IqZsdtElOzHSjGe57y36R4wpZ8hgbLjtVohul0fCxrg"
    private val T_GASTOS = "C2bbCO18omyhKbUQvha38IqZsdtElOzHSjGe57y36R4wpZ8hgbLj"

    suspend fun login(email: String, pass: String): Result<Empleado> {
        return try {
            val res = api.login(email, pass, T_LOGIN)
            if (res.isSuccessful && res.body()?.status == "ok") {
                val emp = res.body()?.empleado
                if (emp != null) Result.success(emp) else Result.failure(Exception("Datos vacíos"))
            } else Result.failure(Exception(res.body()?.msg ?: "Error Login"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getGastos(): List<Gasto> {
        return try {
            val res = api.obtenerGastos(token = T_GASTOS)
            res.body() ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun subirGasto(empId: String, secId: String, cat: String, fecha: String, imp: String, nom: String, file: File): Result<String> {
        return try {
            val filePart = MultipartBody.Part.createFormData("ticket", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
            val res = api.insertarGasto(
                "insertar".toRB(), T_GASTOS.toRB(), empId.toRB(), secId.toRB(), cat.toRB(), fecha.toRB(), imp.toRB(), nom.toRB(), filePart
            )
            if (res.isSuccessful && res.body()?.status == "ok") Result.success("OK")
            else Result.failure(Exception(res.body()?.msg ?: "Error"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun cambiarEstado(id: String, estado: EstadoGasto) {
        try { api.actualizarEstado(token = T_GASTOS, idGasto = id, estado = estado.name) } catch (_: Exception) {}
    }

    suspend fun borrarGasto(id: String) {
        try { api.borrarGasto(token = T_GASTOS, idGasto = id) } catch (_: Exception) {}
    }

    // Mock para Reportes (para que AdminFragment no crashee)
    fun getReportes(callback: (List<Reporte>) -> Unit) { callback(emptyList()) }
    fun deleteReporte(id: String) {}

    private fun String.toRB(): RequestBody = this.toRequestBody("text/plain".toMediaTypeOrNull())
}