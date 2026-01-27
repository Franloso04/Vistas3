package com.example.vistas.data.api

import com.example.vistas.model.Empleado

data class LoginResponse(
    val status: String,
    val msg: String,
    val empleado: Empleado?
)
