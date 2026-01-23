package com.example.vistas.data.api

data class LoginResponse(
    val status: String,
    val msg: String,
    val empleado: Empleado?
)

data class Empleado(
    val id: String,
    val nombre: String,
    val email: String,
    val seccion: String,
    val privilegios: String,
    val privilegios_globales: String,
    val antiguedad: String
)
