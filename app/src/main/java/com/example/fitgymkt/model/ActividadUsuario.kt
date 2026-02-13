package com.example.fitgymkt.model

import java.time.LocalDate

data class ActividadUsuario(
    val idActividad: Int? = null,
    val idUsuario: Int,
    val fecha: LocalDate,
    val duracionMinutos: Int,
    val idHorario: Int?
)
