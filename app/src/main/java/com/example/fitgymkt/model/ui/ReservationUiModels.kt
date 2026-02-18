package com.example.fitgymkt.model.ui

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