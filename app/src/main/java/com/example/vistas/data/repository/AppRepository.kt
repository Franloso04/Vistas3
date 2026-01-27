package com.example.vistas.data.repository

import android.util.Log
import com.example.vistas.data.api.RetrofitClient
import com.example.vistas.model.Empleado
import com.example.vistas.model.EstadoGasto
import com.example.vistas.model.Gasto
import com.example.vistas.model.Reporte
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File

class AppRepository {

    private val api = RetrofitClient.instance
    private val gson = Gson()

    // Tokens corregidos (Largo en ambos)
    private val T_LOGIN = "C2bbCO18omyhKbUQvha38IqZsdtElOzHSjGe57y36R4wpZ8hgbLjtVohul0fCxrg"
    private val T_GASTOS = "C2bbCO18omyhKbUQvha38IqZsdtElOzHSjGe57y36R4wpZ8hgbLjtVohul0fCxrg"

    suspend fun login(email: String, pass: String): Result<Empleado> {
        return try {
            val res = api.login(email, pass, T_LOGIN)
            if (res.isSuccessful && res.body()?.status == "ok") {
                val emp = res.body()?.empleado
                if (emp != null) Result.success(emp) else Result.failure(Exception("Datos vacíos"))
            } else Result.failure(Exception(res.body()?.msg ?: "Error Login"))
        } catch (e: Exception) { Result.failure(e) }
    }

    // --- CORRECCIÓN: getGastos ahora pide el ID ---
    suspend fun getGastos(empId: String): List<Gasto> {
        return try {
            // Enviamos el ID a la API
            val res = api.obtenerGastos(action = "listar", token = T_GASTOS, idEmpleado = empId)
            val jsonString = res.body()?.string() ?: ""

            Log.d("API_DEBUG", "Historial JSON: $jsonString")

            if (jsonString.isEmpty() || jsonString.contains("\"status\":\"error\"")) {
                return emptyList()
            }

            // Lógica de parseo blindada (Lista directa o envuelta)
            if (jsonString.trim().startsWith("[")) {
                val type = object : TypeToken<List<Gasto>>() {}.type
                return gson.fromJson(jsonString, type)
            }

            val jsonObject = JSONObject(jsonString)
            val array = when {
                jsonObject.has("gastos") -> jsonObject.getJSONArray("gastos")
                jsonObject.has("data") -> jsonObject.getJSONArray("data")
                jsonObject.has("lista") -> jsonObject.getJSONArray("lista")
                else -> null
            }

            if (array != null) {
                val type = object : TypeToken<List<Gasto>>() {}.type
                return gson.fromJson(array.toString(), type)
            }

            emptyList()
        } catch (e: Exception) {
            Log.e("API_ERROR", "Error leyendo historial", e)
            emptyList()
        }
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

    fun getReportes(callback: (List<Reporte>) -> Unit) { callback(emptyList()) }
    fun deleteReporte(id: String) {}

    private fun String.toRB(): RequestBody = this.toRequestBody("text/plain".toMediaTypeOrNull())
}