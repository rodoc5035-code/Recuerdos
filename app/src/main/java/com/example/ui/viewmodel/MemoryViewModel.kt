package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.local.MemoryDatabase
import com.example.data.model.MemoryEntity
import com.example.data.repository.MemoryRepository
import com.example.notification.MemoryNotificationManager
import com.example.widget.MemoryAppWidgetProvider
import com.example.util.ImageUtils
import com.example.util.MemoryDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class ViewMode {
    CALENDAR,
    TIMELINE
}

data class MemoryUiState(
    val allMemories: List<MemoryEntity> = emptyList(),
    val filteredMemories: List<MemoryEntity> = emptyList(),
    val selectedDate: MemoryDate = MemoryDate.today(),
    val calendarMonthDate: MemoryDate = MemoryDate.today(),
    val selectedTag: String? = null,
    val availableTags: List<String> = emptyList(),
    val memoriesForSelectedDate: List<MemoryEntity> = emptyList(),
    val memoryDatesSet: Set<String> = emptySet(), // Iso strings with at least one memory
    val flashbackMemories: List<MemoryEntity> = emptyList(),
    val randomPastMemory: MemoryEntity? = null,
    val isDarkTheme: Boolean = false,
    val viewMode: ViewMode = ViewMode.CALENDAR,
    val selectedDetailMemory: MemoryEntity? = null,
    val isAddSheetOpen: Boolean = false
)

class MemoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MemoryRepository
    private val _selectedDate = MutableStateFlow(MemoryDate.today())
    private val _calendarMonthDate = MutableStateFlow(MemoryDate.today())
    private val _selectedTag = MutableStateFlow<String?>(null)
    private val _isDarkTheme = MutableStateFlow(false)
    private val _viewMode = MutableStateFlow(ViewMode.CALENDAR)
    private val _selectedDetailMemory = MutableStateFlow<MemoryEntity?>(null)
    private val _isAddSheetOpen = MutableStateFlow(false)
    private val _randomPastMemory = MutableStateFlow<MemoryEntity?>(null)

    val uiState: StateFlow<MemoryUiState>

    init {
        val db = MemoryDatabase.getDatabase(application, viewModelScope)
        repository = MemoryRepository(db.memoryDao())

        // Combine flows for clean MVI/MVVM reactive UI State
        uiState = combine(
            repository.allMemories,
            _selectedDate,
            _calendarMonthDate,
            _selectedTag,
            _isDarkTheme,
            _viewMode,
            _selectedDetailMemory,
            _isAddSheetOpen,
            _randomPastMemory
        ) { values ->
            @Suppress("UNCHECKED_CAST")
            val allList = values[0] as List<MemoryEntity>
            val selDate = values[1] as MemoryDate
            val calMonth = values[2] as MemoryDate
            val selTag = values[3] as String?
            val dark = values[4] as Boolean
            val mode = values[5] as ViewMode
            val detail = values[6] as MemoryEntity?
            val addOpen = values[7] as Boolean
            val randomPast = values[8] as MemoryEntity?

            // All unique tags extracted from memories
            val tags = allList.flatMap { it.getTagList() }.distinct().sorted()

            // Filtered by selected tag if any
            val filtered = if (selTag == null) {
                allList
            } else {
                allList.filter { it.getTagList().any { tag -> tag.equals(selTag, ignoreCase = true) } }
            }

            // Memories for the selected day in calendar
            val dayMemories = allList.filter { it.dateIso == selDate.isoString }

            // Set of ISO dates that have memories
            val datesWithMemories = allList.map { it.dateIso }.toSet()

            // Flashbacks: Memories created on this same month and day in previous years
            val today = MemoryDate.today()
            val flashbacks = allList.filter {
                it.month == today.month && it.day == today.day && it.year < today.year
            }

            // Pick a random past memory if none chosen yet or refresh
            val effectiveRandom = randomPast ?: allList.filter { it.dateIso != today.isoString }.randomOrNull()

            MemoryUiState(
                allMemories = allList,
                filteredMemories = filtered,
                selectedDate = selDate,
                calendarMonthDate = calMonth,
                selectedTag = selTag,
                availableTags = tags,
                memoriesForSelectedDate = dayMemories,
                memoryDatesSet = datesWithMemories,
                flashbackMemories = flashbacks,
                randomPastMemory = effectiveRandom,
                isDarkTheme = dark,
                viewMode = mode,
                selectedDetailMemory = detail,
                isAddSheetOpen = addOpen
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MemoryUiState()
        )

        // Seed initial starter memories if database is completely empty
        seedStarterMemoriesIfEmpty()
    }

    private fun seedStarterMemoriesIfEmpty() {
        viewModelScope.launch {
            if (repository.getCount() == 0) {
                val app = getApplication<Application>()
                val sunsetPath = ImageUtils.saveDrawableToInternalStorage(app, R.drawable.sample_sunset, "starter_sunset")
                val coffeePath = ImageUtils.saveDrawableToInternalStorage(app, R.drawable.sample_coffee, "starter_coffee")

                val today = MemoryDate.today()
                // Yesterday date
                val calYesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                val yesterday = MemoryDate(
                    calYesterday.get(Calendar.YEAR),
                    calYesterday.get(Calendar.MONTH) + 1,
                    calYesterday.get(Calendar.DAY_OF_MONTH)
                )

                // 1 year ago today for flashback
                val oneYearAgo = MemoryDate(today.year - 1, today.month, today.day)

                if (sunsetPath != null) {
                    repository.insert(
                        MemoryEntity(
                            imageUri = sunsetPath,
                            dateIso = yesterday.isoString,
                            year = yesterday.year,
                            month = yesterday.month,
                            day = yesterday.day,
                            description = "Un atardecer dorado en la costa, disfrutando del silencio y la brisa.",
                            tags = "Naturaleza, Viajes, Paz",
                            isFavorite = true
                        )
                    )
                }

                if (coffeePath != null) {
                    repository.insert(
                        MemoryEntity(
                            imageUri = coffeePath,
                            dateIso = oneYearAgo.isoString,
                            year = oneYearAgo.year,
                            month = oneYearAgo.month,
                            day = oneYearAgo.day,
                            description = "Mañana tranquila de café caliente y cuaderno de notas con nuevas metas.",
                            tags = "Especial, Inspiración, Café",
                            isFavorite = false
                        )
                    )
                }
            }
        }
    }

    fun selectDate(date: MemoryDate) {
        _selectedDate.value = date
        _calendarMonthDate.value = MemoryDate(date.year, date.month, 1)
    }

    fun previousMonth() {
        val current = _calendarMonthDate.value
        val (newYear, newMonth) = if (current.month == 1) {
            Pair(current.year - 1, 12)
        } else {
            Pair(current.year, current.month - 1)
        }
        _calendarMonthDate.value = MemoryDate(newYear, newMonth, 1)
    }

    fun nextMonth() {
        val current = _calendarMonthDate.value
        val (newYear, newMonth) = if (current.month == 12) {
            Pair(current.year + 1, 1)
        } else {
            Pair(current.year, current.month + 1)
        }
        _calendarMonthDate.value = MemoryDate(newYear, newMonth, 1)
    }

    fun jumpToToday() {
        val today = MemoryDate.today()
        _selectedDate.value = today
        _calendarMonthDate.value = MemoryDate(today.year, today.month, 1)
    }

    fun selectTag(tag: String?) {
        _selectedTag.value = if (_selectedTag.value == tag) null else tag
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }

    fun openDetail(memory: MemoryEntity) {
        _selectedDetailMemory.value = memory
    }

    fun closeDetail() {
        _selectedDetailMemory.value = null
    }

    fun openAddSheet(date: MemoryDate? = null) {
        if (date != null) {
            _selectedDate.value = date
        }
        _isAddSheetOpen.value = true
    }

    fun closeAddSheet() {
        _isAddSheetOpen.value = false
    }

    fun refreshRandomFlashback() {
        val pastList = uiState.value.allMemories.filter { it.dateIso != MemoryDate.today().isoString }
        if (pastList.isNotEmpty()) {
            _randomPastMemory.value = pastList.random()
        }
    }

    fun saveMemory(
        imagePath: String,
        date: MemoryDate,
        description: String,
        tags: List<String>
    ) {
        viewModelScope.launch {
            val entity = MemoryEntity(
                imageUri = imagePath,
                dateIso = date.isoString,
                year = date.year,
                month = date.month,
                day = date.day,
                description = description.trim(),
                tags = tags.joinToString(", ") { it.trim().removePrefix("#") }
            )
            repository.insert(entity)
            _selectedDate.value = date
            _calendarMonthDate.value = MemoryDate(date.year, date.month, 1)
            closeAddSheet()
            MemoryAppWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun deleteMemory(memory: MemoryEntity) {
        viewModelScope.launch {
            repository.delete(memory)
            if (_selectedDetailMemory.value?.id == memory.id) {
                closeDetail()
            }
            MemoryAppWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun toggleFavorite(memory: MemoryEntity) {
        viewModelScope.launch {
            val updated = memory.copy(isFavorite = !memory.isFavorite)
            repository.update(updated)
            if (_selectedDetailMemory.value?.id == memory.id) {
                _selectedDetailMemory.value = updated
            }
            MemoryAppWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun triggerOnThisDayNotification() {
        viewModelScope.launch {
            MemoryNotificationManager.checkAndShowOnThisDayNotification(getApplication())
        }
    }

    fun triggerRandomMemoryNotification() {
        viewModelScope.launch {
            MemoryNotificationManager.showRandomMemoryNotification(getApplication())
        }
    }

    fun openMemoryById(id: Long) {
        viewModelScope.launch {
            val memory = repository.getMemoryById(id)
            if (memory != null) {
                _selectedDate.value = MemoryDate.fromIso(memory.dateIso)
                _selectedDetailMemory.value = memory
            }
        }
    }
}
