package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notification.MemoryNotificationManager
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MemoryViewModel

class MainActivity : ComponentActivity() {

  private val memoryViewModel: MemoryViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize notification channels and schedule daily reminders
    MemoryNotificationManager.createNotificationChannel(this)
    MemoryNotificationManager.scheduleDailyReminders(this)

    // Handle intent if launched from notification or widget
    handleIntent(intent)

    setContent {
      val uiState by memoryViewModel.uiState.collectAsStateWithLifecycle()

      // Ask for notification permission gracefully on Android 13+
      val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
      ) { /* Permission result handled gracefully */ }

      LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          val hasPerm = ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.POST_NOTIFICATIONS
          ) == PackageManager.PERMISSION_GRANTED
          if (!hasPerm) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
          }
        }
      }

      MyApplicationTheme(darkTheme = uiState.isDarkTheme) {
        HomeScreen(viewModel = memoryViewModel)
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleIntent(intent)
  }

  private fun handleIntent(intent: Intent?) {
    if (intent == null) return
    val memoryId = intent.getLongExtra("EXTRA_MEMORY_ID", -1L)
    if (memoryId != -1L) {
      memoryViewModel.openMemoryById(memoryId)
    }
  }
}


