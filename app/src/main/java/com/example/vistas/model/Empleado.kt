package com.example.vistas.model

import com.google.gson.annotations.SerializedName

data class Empleado(
    @SerializedName("id") val id: String, // La API devuelve ID como String en el JSON ("232")
    @SerializedName("nombre") val nombre: String,
    @SerializedName("email") val email: String,
    @SerializedName("seccion") val seccion: String,
    @SerializedName("privilegios") val privilegios: String,
    @SerializedName("privilegios_globales") val privilegiosGlobales: String,
    @SerializedName("antiguedad") val antiguedad: String
)