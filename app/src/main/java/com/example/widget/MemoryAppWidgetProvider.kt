package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.local.MemoryDatabase
import com.example.data.model.MemoryEntity
import com.example.util.MemoryDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class MemoryAppWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.example.widget.ACTION_REFRESH_WIDGET"

        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, MemoryAppWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val widgetManager = AppWidgetManager.getInstance(context)
            val ids = widgetManager.getAppWidgetIds(
                ComponentName(context, MemoryAppWidgetProvider::class.java)
            )
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = MemoryDatabase.getDatabase(context)
                val today = MemoryDate.today()

                // 1. Try to find "Un día como hoy"
                val onThisDayMemories = db.memoryDao().getMemoriesOnThisDaySync(
                    month = today.month,
                    day = today.day,
                    currentYear = today.year
                )

                val (memoryToShow, isAnniversary) = if (onThisDayMemories.isNotEmpty()) {
                    Pair(onThisDayMemories.random(), true)
                } else {
                    val random = db.memoryDao().getRandomMemory()
                    Pair(random, false)
                }

                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(
                        context = context,
                        appWidgetManager = appWidgetManager,
                        appWidgetId = appWidgetId,
                        memory = memoryToShow,
                        isAnniversary = isAnniversary
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, MemoryAppWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        memory: MemoryEntity?,
        isAnniversary: Boolean
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_memory_layout)

        if (memory != null) {
            val memoryDate = MemoryDate.fromIso(memory.dateIso)
            val today = MemoryDate.today()
            val yearsAgo = today.year - memoryDate.year

            val headerText = if (isAnniversary && yearsAgo > 0) {
                if (yearsAgo == 1) "✨ Un día como hoy hace 1 año" else "✨ Un día como hoy hace $yearsAgo años"
            } else {
                "✨ Momento para revivir"
            }

            views.setTextViewText(R.id.widget_title, headerText)
            views.setTextViewText(R.id.widget_date, memoryDate.formattedDisplay)

            val descriptionText = if (memory.description.isNotBlank()) {
                memory.description
            } else {
                "Toca para abrir y revivir este momento."
            }
            views.setTextViewText(R.id.widget_description, descriptionText)

            val tags = memory.getTagList()
            if (tags.isNotEmpty()) {
                views.setViewVisibility(R.id.widget_tag, View.VISIBLE)
                views.setTextViewText(R.id.widget_tag, "#${tags.first().removePrefix("#")}")
            } else {
                views.setViewVisibility(R.id.widget_tag, View.GONE)
            }

            // Load and round image
            val roundedBitmap = getRoundedMemoryBitmap(memory.imageUri)
            if (roundedBitmap != null) {
                views.setImageViewBitmap(R.id.widget_image, roundedBitmap)
            } else {
                views.setImageViewResource(R.id.widget_image, R.drawable.sample_sunset)
            }

            // Click on widget opens app to that specific memory
            val clickIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("EXTRA_MEMORY_ID", memory.id)
                putExtra("EXTRA_DATE_ISO", memory.dateIso)
            }
            val pendingClick = PendingIntent.getActivity(
                context,
                appWidgetId,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingClick)

        } else {
            views.setTextViewText(R.id.widget_title, "✨ Recuerdos")
            views.setTextViewText(R.id.widget_date, "Hoy")
            views.setTextViewText(R.id.widget_description, "Aún no tienes recuerdos guardados. ¡Toca aquí para crear el primero!")
            views.setViewVisibility(R.id.widget_tag, View.GONE)
            views.setImageViewResource(R.id.widget_image, R.drawable.sample_sunset)

            val clickIntent = Intent(context, MainActivity::class.java)
            val pendingClick = PendingIntent.getActivity(
                context,
                appWidgetId,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingClick)
        }

        // Refresh button click
        val refreshIntent = Intent(context, MemoryAppWidgetProvider::class.java).apply {
            action = ACTION_REFRESH_WIDGET
        }
        val pendingRefresh = PendingIntent.getBroadcast(
            context,
            appWidgetId + 9000,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )
        views.setOnClickPendingIntent(R.id.widget_btn_refresh, pendingRefresh)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun getRoundedMemoryBitmap(imageUri: String): Bitmap? {
        return try {
            val originalBitmap = if (imageUri.startsWith("/")) {
                val file = File(imageUri)
                if (file.exists()) {
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeFile(file.absolutePath, options)
                    options.inSampleSize = calculateInSampleSize(options, 200, 200)
                    options.inJustDecodeBounds = false
                    BitmapFactory.decodeFile(file.absolutePath, options)
                } else null
            } else null

            originalBitmap?.let { createRoundedBitmap(it, 24f) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun createRoundedBitmap(src: Bitmap, cornerRadius: Float): Bitmap {
        val size = Math.min(src.width, src.height)
        val x = (src.width - size) / 2
        val y = (src.height - size) / 2

        val squaredBitmap = Bitmap.createBitmap(src, x, y, size, size)

        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = Rect(0, 0, size, size)
        val rectF = RectF(rect)

        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(squaredBitmap, rect, rect, paint)

        return output
    }
}
