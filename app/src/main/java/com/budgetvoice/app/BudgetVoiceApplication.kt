package com.budgetvoice.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.budgetvoice.app.data.local.BudgetDatabase
import com.budgetvoice.app.data.repository.ExpenseRepository

class BudgetVoiceApplication : Application() {

    val database by lazy { BudgetDatabase.getInstance(this) }
    val repository by lazy { ExpenseRepository(database.expenseDao()) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notification_channel_description)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "budget_reminders"
    }
}
