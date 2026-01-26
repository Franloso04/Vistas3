package com.example.vistas.data.api
import com.example.vistas.model.Gasto
import okhttp3.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @GET("gastos")
    suspend fun getGastos(): Response<List<Gasto>>

    @POST("gastos")
    suspend fun crearGasto(@Body gasto: Gasto): Response<Gasto>
}
