package com.example.vistas.data.api

import com.example.vistas.model.GastoResponse
import com.example.vistas.model.LoginResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @POST("empleados")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") pass: String,
        @Field("token") token: String
    ): Response<LoginResponse>

    @Multipart
    @POST("gastos.php")
    suspend fun insertarGasto(
        @Part("action") action: RequestBody,
        @Part("token") token: RequestBody,
        @Part("id_empleado") idEmp: RequestBody,
        @Part("id_seccion") idSec: RequestBody,
        @Part("categoria") cat: RequestBody,
        @Part("fecha") fec: RequestBody,
        @Part("importe") imp: RequestBody,
        @Part("nombreComercio") nom: RequestBody,
        @Part ticket: MultipartBody.Part
    ): Response<GastoResponse>

    @FormUrlEncoded
    @POST("gastos.php")
    suspend fun obtenerGastos(
        @Field("action") action: String = "listar",
        @Field("token") token: String,
        @Field("id_empleado") idEmpleado: String // <--- ESTO FALTABA
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("gastos.php")
    suspend fun actualizarEstado(
        @Field("action") action: String = "actualizar_estado",
        @Field("token") token: String,
        @Field("id_gasto") idGasto: String,
        @Field("nuevo_estado") estado: String
    ): Response<GastoResponse>

    @FormUrlEncoded
    @POST("gastos.php")
    suspend fun borrarGasto(
        @Field("action") action: String = "borrar",
        @Field("token") token: String,
        @Field("id_gasto") idGasto: String
    ): Response<GastoResponse>
}