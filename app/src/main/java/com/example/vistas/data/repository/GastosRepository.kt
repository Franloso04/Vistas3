package com.example.vistas.data.repository

import com.example.vistas.data.api.ApiService
import com.example.vistas.model.Gasto
import com.example.vistas.model.Reporte

class GastosRepository(private val api: ApiService) {

    suspend fun obtenerGastos(empleadoId: Long): List<Gasto> =
        api.obtenerGastos(empleadoId)

    suspend fun obtenerResumen(empleadoId: Long): Reporte =
        api.obtenerResumen(empleadoId)

    suspend fun crearGasto(gasto: Gasto) =
        api.crearGasto(gasto)

    suspend fun cambiarEstado(gastoId: Long, estado: String) =
        api.cambiarEstado(gastoId, estado)
}
