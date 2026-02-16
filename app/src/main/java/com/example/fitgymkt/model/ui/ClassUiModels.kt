package com.example.fitgymkt.model.ui

data class ClassWithSchedules(
    val classId: Int,
    val className: String,
    val description: String,
    val schedules: List<ClassScheduleItem>
)

data class ClassScheduleItem(
    val scheduleId: Int,
    val time: String,
    val weekDay: String,
    val occupiedSlots: Int,
    val totalSlots: Int,
    val instructorName: String
)
