package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY dateIso DESC, createdAt DESC")
    fun getAllMemories(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE dateIso = :dateIso ORDER BY createdAt DESC")
    fun getMemoriesByDate(dateIso: String): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE year = :year AND month = :month ORDER BY dateIso ASC")
    fun getMemoriesForMonth(year: Int, month: Int): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE month = :month AND day = :day AND year < :currentYear ORDER BY year DESC")
    fun getMemoriesOnThisDay(month: Int, day: Int, currentYear: Int): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE month = :month AND day = :day AND year < :currentYear ORDER BY year DESC")
    suspend fun getMemoriesOnThisDaySync(month: Int, day: Int, currentYear: Int): List<MemoryEntity>

    @Query("SELECT * FROM memories ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomMemory(): MemoryEntity?

    @Query("SELECT * FROM memories ORDER BY dateIso DESC, createdAt DESC LIMIT :limit")
    suspend fun getRecentMemories(limit: Int): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE id = :id LIMIT 1")
    suspend fun getMemoryById(id: Long): MemoryEntity?

    @Query("SELECT COUNT(*) FROM memories")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity): Long

    @Update
    suspend fun updateMemory(memory: MemoryEntity)

    @Delete
    suspend fun deleteMemory(memory: MemoryEntity)
}
