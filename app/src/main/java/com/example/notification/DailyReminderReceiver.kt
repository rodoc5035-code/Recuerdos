package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.MemoryDatabase
import com.example.util.MemoryDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DailyReminderReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_DAILY_CHECK = "com.example.notification.ACTION_DAILY_CHECK"
        const val ACTION_TEST_ON_THIS_DAY = "com.example.notification.ACTION_TEST_ON_THIS_DAY"
        const val ACTION_TEST_RANDOM = "com.example.notification.ACTION_TEST_RANDOM"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: ACTION_DAILY_CHECK

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (action) {
                    Intent.ACTION_BOOT_COMPLETED,
                    Intent.ACTION_MY_PACKAGE_REPLACED -> {
                        MemoryNotificationManager.scheduleDailyReminders(context)
                    }

                    ACTION_TEST_ON_THIS_DAY -> {
                        MemoryNotificationManager.checkAndShowOnThisDayNotification(context)
                    }

                    ACTION_TEST_RANDOM -> {
                        MemoryNotificationManager.showRandomMemoryNotification(context)
                    }

                    else -> {
                        // Daily check: Check for anniversary "Un día como hoy"
                        val db = MemoryDatabase.getDatabase(context)
                        val today = MemoryDate.today()
                        val onThisDay = db.memoryDao().getMemoriesOnThisDaySync(
                            month = today.month,
                            day = today.day,
                            currentYear = today.year
                        )

                        if (onThisDay.isNotEmpty()) {
                            MemoryNotificationManager.checkAndShowOnThisDayNotification(context)
                        } else {
                            // If no anniversary today, show a random past moment
                            MemoryNotificationManager.showRandomMemoryNotification(context)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
