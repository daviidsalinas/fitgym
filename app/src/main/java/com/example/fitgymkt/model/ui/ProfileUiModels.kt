package com.example.fitgymkt.model.ui

data class AnalysisData(
    val streakDays: Int,
    val weeklyGoalHours: Double,
    val weeklyTrainedHours: Double,
    val weekActivityMinutes: List<Int>
)

data class ProfileData(
    val fullName: String,
    val email: String,
    val phone: String,
    val age: Int,
    val weightKg: Double,
    val heightCm: Double,
    val notificationsEnabled: Boolean,
    val language: String
)

data class ReservationDetailData(
    val className: String,
    val classDescription: String,
    val date: String,
    val startTime: String,
    val instructorName: String,
    val roomName: String,
    val occupiedSlots: Int,
    val totalSlots: Int
)