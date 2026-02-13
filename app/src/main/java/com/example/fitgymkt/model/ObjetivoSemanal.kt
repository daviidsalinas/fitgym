package com.example.fitgymkt.model

data class ObjetivoSemanal(
    val idObjetivo: Int? = null,
    val idUsuario: Int,
    val horasObjetivo: Double,
    val semana: Int,
    val anio: Int
)
