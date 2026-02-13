package com.example.fitgymkt.model
import java.time.LocalDate

data class Favorito(
    val idFavorito: Int? = null,
    val idUsuario: Int,
    val idClase: Int,
    val fechaMarcado: LocalDate
)
