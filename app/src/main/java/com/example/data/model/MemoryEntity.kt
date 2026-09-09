package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val imageUri: String,
    val dateIso: String,
    val year: Int,
    val month: Int,
    val day: Int,
    val description: String,
    val tags: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
) {
    fun getTagList(): List<String> =
        tags.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
}
