package com.example.vistas.data.repository

import com.example.vistas.data.api.ApiService
import com.example.vistas.data.api.LoginRequest
import com.example.vistas.model.Empleado

class AuthRepository(private val api: ApiService) {

    suspend fun login(email: String, password: String): Empleado {
        return api.login(LoginRequest(email, password))
    }
}
