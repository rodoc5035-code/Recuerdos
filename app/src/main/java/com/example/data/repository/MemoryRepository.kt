package com.example.data.repository

import com.example.data.local.MemoryDao
import com.example.data.model.MemoryEntity
import kotlinx.coroutines.flow.Flow

class MemoryRepository(private val memoryDao: MemoryDao) {
    val allMemories: Flow<List<MemoryEntity>> = memoryDao.getAllMemories()

    fun getMemoriesByDate(dateIso: String): Flow<List<MemoryEntity>> =
        memoryDao.getMemoriesByDate(dateIso)

    fun getMemoriesForMonth(year: Int, month: Int): Flow<List<MemoryEntity>> =
        memoryDao.getMemoriesForMonth(year, month)

    fun getMemoriesOnThisDay(month: Int, day: Int, currentYear: Int): Flow<List<MemoryEntity>> =
        memoryDao.getMemoriesOnThisDay(month, day, currentYear)

    suspend fun getMemoriesOnThisDaySync(month: Int, day: Int, currentYear: Int): List<MemoryEntity> =
        memoryDao.getMemoriesOnThisDaySync(month, day, currentYear)

    suspend fun getRandomMemory(): MemoryEntity? =
        memoryDao.getRandomMemory()

    suspend fun getRecentMemories(limit: Int): List<MemoryEntity> =
        memoryDao.getRecentMemories(limit)

    suspend fun getMemoryById(id: Long): MemoryEntity? =
        memoryDao.getMemoryById(id)

    suspend fun insert(memory: MemoryEntity): Long =
        memoryDao.insertMemory(memory)

    suspend fun update(memory: MemoryEntity) =
        memoryDao.updateMemory(memory)

    suspend fun delete(memory: MemoryEntity) =
        memoryDao.deleteMemory(memory)

    suspend fun getCount(): Int =
        memoryDao.getCount()
}
