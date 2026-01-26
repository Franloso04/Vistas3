package com.example.vistas.data.api

import com.example.vistas.model.Gasto
import com.google.firebase.appdistribution.gradle.ApiService

class RemoteDataSource {

    private val api = RetrofitClient.api

    suspend fun obtenerGastos(): List<Gasto> {
        val response = api.getGastos()
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Error API")
        }
    }

    suspend fun crearGasto(gasto: Gasto): Gasto {
        val response = api.crearGasto(gasto)
        if (response.isSuccessful) {
            return response.body()!!
        } else {
            throw Exception("Error API")
        }
    }
}

