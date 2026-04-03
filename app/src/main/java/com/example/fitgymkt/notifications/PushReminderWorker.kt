package com.example.fitgymkt.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.fitgymkt.R
import com.example.fitgymkt.repository.FitGymRepository

class PushReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        val userId = inputData.getInt(KEY_USER_ID, -1)
        if (userId <= 0) return Result.success()

        val repository = FitGymRepository(applicationContext)
        val profileData = repository.getProfileData(userId)
        if (!profileData.notificationsEnabled) return Result.success()

        val reminder = repository.getUpcomingClassReminder(userId) ?: return Result.success()

        val prefs = applicationContext.getSharedPreferences(PREFS_PUSH, Context.MODE_PRIVATE)
        val lastSentKey = prefs.getString(KEY_LAST_PUSH_ID, null)
        if (lastSentKey == reminder.uniqueId) return Result.success()

        createNotificationChannel(applicationContext)

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(reminder.title)
            .setContentText(reminder.message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(PUSH_NOTIFICATION_ID, notification)

        prefs.edit().putString(KEY_LAST_PUSH_ID, reminder.uniqueId).apply()

        return Result.success()
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.push_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.push_channel_description)
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val KEY_USER_ID = "user_id"
        private const val PREFS_PUSH = "fitgym_push_prefs"
        private const val KEY_LAST_PUSH_ID = "last_push_id"
        private const val PUSH_NOTIFICATION_ID = 2201
        const val CHANNEL_ID = "fitgym_push_channel"
    }
}