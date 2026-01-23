package com.example.vistas.model

data class Empleado(
    val id: String,
    val nombre: String,
    val email: String,
    val seccion: String,
    val privilegios: String,
    val privilegios_globales: String,
    val antiguedad: String
)
