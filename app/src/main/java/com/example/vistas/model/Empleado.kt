package com.example.vistas.model

import androidx.room.Entity

data class Empleado(
    val id: Long,
    val nombre: String,
    val email: String,
    val seccion: Int,
    val privilegios: Int
)
@Entity
@Table(name = "empleados", schema = "Carsmarobe")
data class EmpleadoEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val nombre: String,
    val email: String,
    val password: String,
    val seccion: Int,
    val privilegios: Int
)
