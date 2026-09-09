package com.example.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast

object ShareUtils {

    /**
     * Shares a memory directly to WhatsApp with the image and description.
     * Falls back to general share chooser if WhatsApp is not installed.
     */
    fun shareToWhatsApp(
        context: Context,
        imagePath: String,
        description: String,
        dateFormatted: String = "",
        tags: List<String> = emptyList()
    ) {
        val shareableUri = ImageUtils.getShareableUri(context, imagePath)

        val textBuilder = StringBuilder()
        if (description.isNotBlank()) {
            textBuilder.append(description)
        }
        if (dateFormatted.isNotBlank()) {
            if (textBuilder.isNotEmpty()) textBuilder.append("\n\n")
            textBuilder.append("📅 ").append(dateFormatted)
        }
        if (tags.isNotEmpty()) {
            textBuilder.append("\n").append(tags.joinToString(" ") { if (it.startsWith("#")) it else "#$it" })
        }

        val shareText = textBuilder.toString()

        val intent = Intent(Intent.ACTION_SEND).apply {
            if (shareableUri != null) {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, shareableUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                type = "text/plain"
            }
            putExtra(Intent.EXTRA_TEXT, shareText)
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // WhatsApp is not installed, open standard chooser
            Toast.makeText(context, "WhatsApp no encontrado, abriendo opciones de compartir...", Toast.LENGTH_SHORT).show()
            shareGeneral(context, imagePath, description, dateFormatted, tags)
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo compartir el recuerdo", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    /**
     * Standard Android share sheet fallback.
     */
    fun shareGeneral(
        context: Context,
        imagePath: String,
        description: String,
        dateFormatted: String = "",
        tags: List<String> = emptyList()
    ) {
        val shareableUri = ImageUtils.getShareableUri(context, imagePath)

        val textBuilder = StringBuilder()
        if (description.isNotBlank()) {
            textBuilder.append(description)
        }
        if (dateFormatted.isNotBlank()) {
            if (textBuilder.isNotEmpty()) textBuilder.append("\n\n")
            textBuilder.append("📅 ").append(dateFormatted)
        }
        if (tags.isNotEmpty()) {
            textBuilder.append("\n").append(tags.joinToString(" ") { if (it.startsWith("#")) it else "#$it" })
        }

        val shareText = textBuilder.toString()

        val intent = Intent(Intent.ACTION_SEND).apply {
            if (shareableUri != null) {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, shareableUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                type = "text/plain"
            }
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val chooser = Intent.createChooser(intent, "Compartir recuerdo").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
