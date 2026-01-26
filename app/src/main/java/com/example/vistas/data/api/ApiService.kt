package com.example.vistas.data.api

import com.example.vistas.model.GastoResponse
import com.example.vistas.model.LoginResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    // LOGIN: FormUrlEncoded porque se envían campos simples
    @FormUrlEncoded
    @POST("empleados")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") pass: String,
        @Field("token") token: String
    ): Response<LoginResponse>

    // INSERTAR GASTO: Multipart porque enviamos TEXTO + ARCHIVO (Foto)
    @Multipart
    @POST("gastos.php")
    suspend fun insertarGasto(
        @Part("action") action: RequestBody,
        @Part("token") token: RequestBody,
        @Part("id_empleado") idEmpleado: RequestBody,
        @Part("id_seccion") idSeccion: RequestBody,
        @Part("categoria") categoria: RequestBody,
        @Part("fecha") fecha: RequestBody,
        @Part("importe") importe: RequestBody,
        @Part("nombreComercio") nombreComercio: RequestBody,
        @Part ticket: MultipartBody.Part // La foto
    ): Response<GastoResponse>
}