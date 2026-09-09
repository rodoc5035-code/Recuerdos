package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddMemorySheet
import com.example.ui.components.CalendarView
import com.example.ui.components.FlashbackCard
import com.example.ui.components.MemoryCard
import com.example.ui.components.MemoryDetailDialog
import com.example.ui.theme.BrandPink
import com.example.ui.viewmodel.MemoryViewModel
import com.example.ui.viewmodel.ViewMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MemoryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showNotifMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Recuerdos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = BrandPink,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                actions = {
                    // Notifications options menu
                    Box {
                        IconButton(
                            onClick = { showNotifMenu = true },
                            modifier = Modifier.testTag("btn_notifications_menu")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Notificaciones de recuerdos",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        DropdownMenu(
                            expanded = showNotifMenu,
                            onDismissRequest = { showNotifMenu = false },
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("✨ Notificación: Un día como hoy", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("Recordar aniversario de esta fecha", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                    }
                                },
                                onClick = {
                                    showNotifMenu = false
                                    viewModel.triggerOnThisDayNotification()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("🎲 Notificación: Momento al azar", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("Revivir un recuerdo aleatorio", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                    }
                                },
                                onClick = {
                                    showNotifMenu = false
                                    viewModel.triggerRandomMemoryNotification()
                                }
                            )
                        }
                    }

                    // View Mode Switcher (Calendar vs Timeline/Feed)
                    IconButton(
                        onClick = {
                            val nextMode = if (uiState.viewMode == ViewMode.CALENDAR) {
                                ViewMode.TIMELINE
                            } else {
                                ViewMode.CALENDAR
                            }
                            viewModel.setViewMode(nextMode)
                        },
                        modifier = Modifier.testTag("btn_switch_view_mode")
                    ) {
                        Icon(
                            imageVector = if (uiState.viewMode == ViewMode.CALENDAR) {
                                Icons.Default.PhotoLibrary
                            } else {
                                Icons.Default.CalendarMonth
                            },
                            contentDescription = "Cambiar vista",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Light / Dark Theme Mode Switcher
                    IconButton(
                        onClick = { viewModel.toggleTheme() },
                        modifier = Modifier.testTag("btn_toggle_theme")
                    ) {
                        Icon(
                            imageVector = if (uiState.isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Cambiar tema",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openAddSheet(uiState.selectedDate) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                },
                text = {
                    Text(
                        text = "Nuevo Recuerdo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_memory")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Horizontal Tag Filter Strip
            if (uiState.availableTags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isAllSelected = uiState.selectedTag == null
                    FilterChip(
                        selected = isAllSelected,
                        onClick = { viewModel.selectTag(null) },
                        label = { Text("Todos (${uiState.allMemories.size})", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = null,
                        shape = RoundedCornerShape(10.dp)
                    )

                    uiState.availableTags.forEach { tag ->
                        val isSelected = uiState.selectedTag.equals(tag, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectTag(tag) },
                            label = { Text(if (tag.startsWith("#")) tag else "#$tag", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = null,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // Main Content Area (Calendar or Timeline)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                // Item: Flashback / "Revivir momentos pasados" card
                item {
                    val anniversaryMemory = uiState.flashbackMemories.firstOrNull()
                    val targetMemory = anniversaryMemory ?: uiState.randomPastMemory
                    if (targetMemory != null) {
                        FlashbackCard(
                            flashbackMemory = targetMemory,
                            isAnniversaryToday = anniversaryMemory != null,
                            onMemoryClick = { viewModel.openDetail(it) },
                            onRefreshRandom = { viewModel.refreshRandomFlashback() }
                        )
                    }
                }

                if (uiState.viewMode == ViewMode.CALENDAR) {
                    // Item: Interactive Calendar Grid
                    item {
                        CalendarView(
                            calendarMonthDate = uiState.calendarMonthDate,
                            selectedDate = uiState.selectedDate,
                            datesWithMemories = uiState.memoryDatesSet,
                            onDateSelected = { viewModel.selectDate(it) },
                            onPreviousMonth = { viewModel.previousMonth() },
                            onNextMonth = { viewModel.nextMonth() },
                            onJumpToToday = { viewModel.jumpToToday() }
                        )
                    }

                    // Section Title: Selected Date Info
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = uiState.selectedDate.formattedDisplay,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = if (uiState.memoriesForSelectedDate.isEmpty())
                                        "Sin recuerdos registrados"
                                    else
                                        "${uiState.memoriesForSelectedDate.size} momento${if (uiState.memoriesForSelectedDate.size > 1) "s" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            ElevatedButton(
                                onClick = { viewModel.openAddSheet(uiState.selectedDate) },
                                colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_add_to_selected_date")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Añadir", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // Empty State or List of Memories for Selected Date
                    if (uiState.memoriesForSelectedDate.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Ningún recuerdo para este día",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Sube una foto y escribe unas palabras para recordarlo siempre.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(
                            items = uiState.memoriesForSelectedDate,
                            key = { it.id }
                        ) { memory ->
                            MemoryCard(
                                memory = memory,
                                onClick = { viewModel.openDetail(memory) },
                                onToggleFavorite = { viewModel.toggleFavorite(memory) },
                                onDelete = { viewModel.deleteMemory(memory) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                } else {
                    // TIMELINE / FEED VIEW
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (uiState.selectedTag != null) "Etiqueta #${uiState.selectedTag}" else "Línea de Tiempo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${uiState.filteredMemories.size} momentos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    if (uiState.filteredMemories.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 20.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No se encontraron momentos",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Prueba seleccionando otra etiqueta o agrega un nuevo recuerdo.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(
                            items = uiState.filteredMemories,
                            key = { it.id }
                        ) { memory ->
                            MemoryCard(
                                memory = memory,
                                onClick = { viewModel.openDetail(memory) },
                                onToggleFavorite = { viewModel.toggleFavorite(memory) },
                                onDelete = { viewModel.deleteMemory(memory) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: Add New Memory
    if (uiState.isAddSheetOpen) {
        AddMemorySheet(
            initialDate = uiState.selectedDate,
            onDismiss = { viewModel.closeAddSheet() },
            onSave = { imagePath, date, description, tags ->
                viewModel.saveMemory(imagePath, date, description, tags)
            }
        )
    }

    // Fullscreen / Modal Memory Detail Dialog
    uiState.selectedDetailMemory?.let { memory ->
        MemoryDetailDialog(
            memory = memory,
            onDismiss = { viewModel.closeDetail() },
            onToggleFavorite = { viewModel.toggleFavorite(memory) },
            onDelete = { viewModel.deleteMemory(memory) }
        )
    }
}
