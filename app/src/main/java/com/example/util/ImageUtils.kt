package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageUtils {

    private const val MEMORIES_FOLDER = "memories"

    fun getMemoriesDir(context: Context): File {
        val dir = File(context.filesDir, MEMORIES_FOLDER)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Copies an image from a content Uri (Photo Picker) to the app's internal storage.
     * Returns the absolute path string.
     */
    fun copyUriToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val memoriesDir = getMemoriesDir(context)
            val fileName = "memory_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            val destFile = File(memoriesDir, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Saves a drawable resource into internal storage as a JPG file so it can be shared via FileProvider.
     */
    fun saveDrawableToInternalStorage(context: Context, @DrawableRes drawableResId: Int, name: String): String? {
        return try {
            val memoriesDir = getMemoriesDir(context)
            val destFile = File(memoriesDir, "$name.jpg")
            if (destFile.exists()) {
                return destFile.absolutePath
            }

            val bitmap = BitmapFactory.decodeResource(context.resources, drawableResId)
            FileOutputStream(destFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generates a FileProvider content URI for the given file path.
     */
    fun getShareableUri(context: Context, filePath: String): Uri? {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                val authority = "${context.packageName}.fileprovider"
                FileProvider.getUriForFile(context, authority, file)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
