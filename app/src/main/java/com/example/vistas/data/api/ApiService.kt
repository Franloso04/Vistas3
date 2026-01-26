package com.example.vistas.data.api

import com.example.vistas.model.Empleado
import com.example.vistas.model.Gasto
import com.example.vistas.model.Reporte
import retrofit2.http.*

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Empleado

    @GET("gastos")
    suspend fun obtenerGastos(@Query("empleadoId") empleadoId: Long): List<Gasto>

    @GET("dashboard/resumen")
    suspend fun obtenerResumen(@Query("empleadoId") empleadoId: Long): Reporte

    @POST("gastos")
    suspend fun crearGasto(@Body gasto: Gasto)

    @PUT("gastos/{id}/estado")
    suspend fun cambiarEstado(
        @Path("id") gastoId: Long,
        @Query("estado") estado: String
    )
}
