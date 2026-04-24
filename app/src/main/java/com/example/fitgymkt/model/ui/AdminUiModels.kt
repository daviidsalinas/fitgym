package com.example.fitgymkt.model.ui

data class AdminDashboardData(
    val totalUsers: Int,
    val activeUsers: Int,
    val totalClasses: Int,
    val todaySchedules: Int,
    val todayReservations: Int,
    val recentUsers: List<AdminUserItem>
)

data class AdminUserItem(
    val id: Int,
    val fullName: String,
    val email: String,
    val role: String,
    val active: Boolean,
    val createdAt: String
)

data class AdminClassItem(
    val id: Int,
    val name: String,
    val description: String,
    val imageUrl: String,
    val schedulesCount: Int,
    val reservationsCount: Int
)

data class AdminScheduleItem(
    val id: Int,
    val className: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val roomName: String,
    val monitorName: String,
    val totalSlots: Int,
    val reservedSlots: Int
)

data class AdminBookingItem(
    val id: Int,
    val userName: String,
    val userEmail: String,
    val className: String,
    val date: String,
    val startTime: String,
    val state: String,
    val reservationDate: String
)
