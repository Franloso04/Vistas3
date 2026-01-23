package com.example.vistas.data.repository

import com.example.vistas.model.Empleado
import com.example.vistas.data.api.LoginRequest
import com.example.vistas.data.api.RetrofitInstance

class AuthRepository {

    suspend fun login(email: String, password: String): Result<Empleado> {
        return try {
            val response = RetrofitInstance.api.login(
                LoginRequest(email, password)
            )

            if (response.status == "ok" && response.empleado != null) {
                Result.success(response.empleado)
            } else {
                Result.failure(Exception(response.msg))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
