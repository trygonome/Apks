package com.budgetvoice.app.utils

import android.content.Context
import androidx.work.*
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    fun scheduleNotifications(context: Context) {
        // Cancel existing work
        WorkManager.getInstance(context).cancelAllWorkByTag("budget_reminder")

        // Schedule notification for end of work day (18:00)
        scheduleNotification(context, "evening_reminder", 18, 0)

        // Schedule notification for evening (22:00)
        scheduleNotification(context, "night_reminder", 22, 0)
    }

    private fun scheduleNotification(context: Context, tag: String, hour: Int, minute: Int) {
        val now = LocalTime.now()
        val targetTime = LocalTime.of(hour, minute)

        val initialDelay = if (now.isAfter(targetTime)) {
            // If time has passed today, schedule for tomorrow
            val hoursUntilTarget = 24 - (now.hour - hour)
            hoursUntilTarget * 60L - now.minute + minute
        } else {
            // Schedule for today
            val hoursUntilTarget = hour - now.hour
            hoursUntilTarget * 60L - now.minute + minute
        }

        val notificationWork = PeriodicWorkRequestBuilder<NotificationWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MINUTES)
            .addTag("budget_reminder")
            .addTag(tag)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            tag,
            ExistingPeriodicWorkPolicy.REPLACE,
            notificationWork
        )
    }

    fun cancelNotifications(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag("budget_reminder")
    }
}
