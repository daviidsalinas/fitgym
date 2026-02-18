package com.example.fitgymkt.model.ui

data class AnalysisData(
    val streakDays: Int,
    val weeklyGoalHours: Double,
    val weeklyCompletedHours: Double,
    val weeklyActivityHours: List<Double>
)