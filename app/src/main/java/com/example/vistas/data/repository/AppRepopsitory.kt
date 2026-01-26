package com.example.vistas.data.repository

import com.example.vistas.data.api.RetrofitClient
import com.example.vistas.model.Empleado
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class AppRepository {

    private val api = RetrofitClient.instance
    // TOKEN CONSTANTE DEFINIDO EN EL CÓDIGO
    private val TOKEN_API = "C2bbCO18omyhKbUQvha38IqZsdtElOzHSjGe57y36R4wpZ8hgbLjtVohul0fCxrg"

    // --- LOGIN ---
    suspend fun login(email: String, pass: String): Result<Empleado> {
        return try {
            val response = api.login(email, pass, TOKEN_API)
            if (response.isSuccessful && response.body()?.status == "ok") {
                val empleado = response.body()?.empleado
                if (empleado != null) {
                    Result.success(empleado)
                } else {
                    Result.failure(Exception("Datos de empleado vacíos"))
                }
            } else {
                Result.failure(Exception(response.body()?.msg ?: "Error en login"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- SUBIR GASTO ---
    suspend fun subirGasto(
        idEmpleado: String,
        idSeccion: String,
        categoria: String,
        fecha: String, // Formato YYYY-MM-DD
        importe: String,
        comercio: String,
        archivoFoto: File
    ): Result<String> {
        return try {
            // Convertimos Strings a RequestBody
            val actionBody = "insertar".toTextBody()
            val tokenBody = "C2bbCO18omyhKbUQvha38IqZsdtElOzHSjGe57y36R4wpZ8hgbLj".toTextBody() // Ojo, en tu prompt el token de login y de gasto eran distintos, he usado el del gasto
            val empBody = idEmpleado.toTextBody()
            val secBody = idSeccion.toTextBody()
            val catBody = categoria.toTextBody()
            val fechaBody = fecha.toTextBody()
            val impBody = importe.toTextBody()
            val comBody = comercio.toTextBody()

            // Preparamos la imagen
            val requestFile = archivoFoto.asRequestBody("image/*".toMediaTypeOrNull())
            val imagenPart = MultipartBody.Part.createFormData("ticket", archivoFoto.name, requestFile)

            val response = api.insertarGasto(
                actionBody, tokenBody, empBody, secBody, catBody, fechaBody, impBody, comBody, imagenPart
            )

            if (response.isSuccessful && response.body()?.status == "ok") {
                Result.success(response.body()?.msg ?: "Gasto guardado")
            } else {
                Result.failure(Exception(response.body()?.msg ?: "Error al guardar"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Extensión para facilitar la conversión string -> requestbody
    private fun String.toTextBody(): RequestBody {
        return this.toRequestBody("text/plain".toMediaTypeOrNull())
    }
}