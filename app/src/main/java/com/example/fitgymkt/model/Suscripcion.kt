package com.example.fitgymkt.model

import java.time.LocalDate

data class Suscripcion(
    val idSuscripcion: Int? = null,
    val idUsuario: Int,
    val tipo: String,
    val fechaInicio: LocalDate,
    val fechaFin: LocalDate?,
    val estado: String
)
