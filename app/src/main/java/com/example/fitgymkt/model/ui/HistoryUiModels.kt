package com.example.fitgymkt.model.ui

data class UserReservationItem(
    val className: String,
    val date: String,
    val time: String,
    val state: String
)

data class WorkoutHistoryItem(
    val date: String,
    val durationMinutes: Int
)

data class SubscriptionStatus(
    val type: String,
    val endDate: String,
    val status: String
)

data class AppNotification(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: String,
    val read: Boolean = false
)