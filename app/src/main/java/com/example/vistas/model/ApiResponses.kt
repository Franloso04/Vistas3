package com.example.vistas.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("status") val status: String,
    @SerializedName("msg") val msg: String,
    @SerializedName("empleado") val empleado: Empleado? // Puede ser nulo si falla
)

data class GastoResponse(
    @SerializedName("status") val status: String,
    @SerializedName("msg") val msg: String,
    @SerializedName("id") val idGasto: Int?,
    @SerializedName("url_ticket") val urlTicket: String?
)