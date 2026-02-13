package com.example.fitgymkt.model
import java.time.LocalDate
import java.time.LocalTime

data class HorarioClase(
    val idHorario: Int,
    val idClase: Int,
    val idMonitor: Int,
    val idSala: Int,
    val fecha: String,       // usamos String para SQLite y compatibilidad API <26
    val horaInicio: String,
    val horaFin: String,
    val plazasTotales: Int
)
