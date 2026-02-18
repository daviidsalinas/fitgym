package com.example.fitgymkt.model.ui

data class HomeData(
    val userName: String,
    val calories: Int,
    val trainingHours: String,
    val todayClasses: List<TodayClassItem>,
    val streakDays: Int
)

data class TodayClassItem(
    val className: String,
    val startTime: String,
    val roomName: String
)
