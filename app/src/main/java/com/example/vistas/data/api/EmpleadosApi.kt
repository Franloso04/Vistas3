package com.example.vistas.data.api

import retrofit2.http.Body
import retrofit2.http.POST

interface EmpleadosApi {

    @POST("empleados")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}


