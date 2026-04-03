package com.example.fitgymkt.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

object PushNotificationScheduler {

    private const val WORK_PREFIX = "fitgym_push_"

    fun schedule(context: Context, userId: Int) {
        if (userId <= 0) return

        val request = PeriodicWorkRequestBuilder<PushReminderWorker>(6, TimeUnit.HOURS)
            .addTag(workTag(userId))
            .setInputData(workDataOf(PushReminderWorker.KEY_USER_ID to userId))
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            uniqueWorkName = workName(userId),
            existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
            request = request
        )
    }

    fun cancel(context: Context, userId: Int) {
        if (userId <= 0) return
        WorkManager.getInstance(context).cancelUniqueWork(workName(userId))
    }

    private fun workName(userId: Int): String = "$WORK_PREFIX$userId"
    private fun workTag(userId: Int): String = "tag_$WORK_PREFIX$userId"
}