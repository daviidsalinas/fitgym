package com.example.fitgymkt.model.ui

data class ProfileData(
    val fullName: String,
    val profilePhoto: String,
    val email: String,
    val phone: String,
    val age: Int,
    val weightKg: Double,
    val heightCm: Double,
    val notificationsEnabled: Boolean,
    val language: String
)