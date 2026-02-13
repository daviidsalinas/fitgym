package com.example.fitgymkt.model


data class Usuario(
    val id: Int? = null,
    val nombre: String,
    val apellidos: String,
    val email: String,
    val contraseña: String,  // sin acento
    val fotoPerfil: String?
)
