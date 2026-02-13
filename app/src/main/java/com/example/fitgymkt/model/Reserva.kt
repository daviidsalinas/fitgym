package com.example.fitgymkt.model




data class Reserva(
    val idReserva: Int,
    val idUsuario: Int,
    val idHorario: Int,
    val estado: String,
    val fechaReserva: String
)
