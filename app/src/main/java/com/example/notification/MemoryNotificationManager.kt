package com.example.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.MemoryDatabase
import com.example.data.model.MemoryEntity
import com.example.util.MemoryDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar

object MemoryNotificationManager {

    const val CHANNEL_ID = "memories_reminders_channel"
    const val NOTIFICATION_ID_ANNIVERSARY = 1001
    const val NOTIFICATION_ID_RANDOM = 1002

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recuerdos y Momentos"
            val descriptionText = "Notificaciones de un día como hoy y momentos al azar para revivir."
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Shows a notification for "Un día como hoy" if matching past memories exist.
     * If none exist for today's date in past years, it optionally sends a random past moment reminder.
     */
    suspend fun checkAndShowOnThisDayNotification(context: Context) = withContext(Dispatchers.IO) {
        if (!hasNotificationPermission(context)) return@withContext

        val db = MemoryDatabase.getDatabase(context)
        val today = MemoryDate.today()

        val onThisDayMemories = db.memoryDao().getMemoriesOnThisDaySync(
            month = today.month,
            day = today.day,
            currentYear = today.year
        )

        if (onThisDayMemories.isNotEmpty()) {
            val memory = onThisDayMemories.first()
            val yearsAgo = today.year - memory.year
            val title = if (yearsAgo == 1) {
                "✨ Un día como hoy hace 1 año"
            } else {
                "✨ Un día como hoy hace $yearsAgo años"
            }
            val text = if (memory.description.isNotBlank()) {
                memory.description
            } else {
                "Tienes un hermoso momento guardado de este día."
            }

            showMemoryNotification(
                context = context,
                notificationId = NOTIFICATION_ID_ANNIVERSARY,
                title = title,
                text = text,
                memory = memory
            )
        }
    }

    /**
     * Shows a notification with a random past memory to reminisce.
     */
    suspend fun showRandomMemoryNotification(context: Context) = withContext(Dispatchers.IO) {
        if (!hasNotificationPermission(context)) return@withContext

        val db = MemoryDatabase.getDatabase(context)
        val randomMemory = db.memoryDao().getRandomMemory() ?: return@withContext
        val date = MemoryDate.fromIso(randomMemory.dateIso)

        val title = "🌟 Recordatorio: Revive este momento"
        val text = if (randomMemory.description.isNotBlank()) {
            "(${date.formattedDisplay}) ${randomMemory.description}"
        } else {
            "Momento guardado el ${date.formattedDisplay}. ¡Toca para verlo!"
        }

        showMemoryNotification(
            context = context,
            notificationId = NOTIFICATION_ID_RANDOM,
            title = title,
            text = text,
            memory = randomMemory
        )
    }

    private fun showMemoryNotification(
        context: Context,
        notificationId: Int,
        title: String,
        text: String,
        memory: MemoryEntity
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_MEMORY_ID", memory.id)
            putExtra("EXTRA_DATE_ISO", memory.dateIso)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        // Load thumbnail if available
        val bitmap = try {
            if (memory.imageUri.startsWith("/")) {
                val file = File(memory.imageUri)
                if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
            } else null
        } catch (e: Exception) {
            null
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_memory)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(
                if (bitmap != null) {
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                        .setSummaryText(text)
                } else {
                    NotificationCompat.BigTextStyle().bigText(text)
                }
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (bitmap != null) {
            builder.setLargeIcon(bitmap)
        }

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    /**
     * Schedules daily notification reminders using AlarmManager.
     */
    fun scheduleDailyReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, DailyReminderReceiver::class.java).apply {
            action = DailyReminderReceiver.ACTION_DAILY_CHECK
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            2001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        // Set for 10:00 AM every day
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
