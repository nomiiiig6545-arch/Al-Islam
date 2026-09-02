package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.db.AppDatabase
import com.example.data.repository.QuranRepository
import com.example.data.repository.SettingsRepository
import com.example.ui.QuranApp
import com.example.ui.QuranViewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        com.example.data.mushaf.OfflineQuranDataProvider.init(applicationContext)

        val database = AppDatabase.getDatabase(this)
        val repository = QuranRepository(
            database.bookmarkDao(),
            database.recentSurahDao(),
            database.surahDao(),
            database.ayahDao(),
            database.mushafPageDao()
        )
        val settingsRepository = SettingsRepository(applicationContext)
        val audioDownloadManager = com.example.data.audio.AudioDownloadManager(applicationContext)
        val mushafDownloadManager = com.example.data.mushaf.MushafPageDownloadManager.getInstance(applicationContext)
        
        // Initialize background resources non-blockingly
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                com.example.ui.components.PageTurnSoundManager.init(applicationContext)
                com.example.data.mushaf.MushafPageCacheManager.initializeIndex(applicationContext)
                repository.getSurahs()
            } catch (_: Exception) {
            }
        }
        
        enableEdgeToEdge()
        setContent {
            val themePreference by settingsRepository.themePreference.collectAsState(initial = 0)
            val brightnessLevel by settingsRepository.brightnessLevel.collectAsState(initial = -1f)
            val keepScreenOn by settingsRepository.keepScreenOn.collectAsState(initial = false)
            
            androidx.compose.runtime.LaunchedEffect(brightnessLevel) {
                val lp = window.attributes
                lp.screenBrightness = if (brightnessLevel < 0f) {
                    android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                } else {
                    brightnessLevel.coerceIn(0.05f, 1.0f)
                }
                window.attributes = lp
            }

            androidx.compose.runtime.LaunchedEffect(keepScreenOn) {
                if (keepScreenOn) {
                    window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
            
            val isDarkTheme = when (themePreference) {
                1 -> false
                0 -> isSystemInDarkTheme()
                else -> true
            }
            
            MyApplicationTheme(themePreference = themePreference, darkTheme = isDarkTheme) {
                val viewModel: QuranViewModel = viewModel(
                    factory = QuranViewModel.Factory(repository, settingsRepository, audioDownloadManager)
                )
                QuranApp(viewModel = viewModel)
            }
        }
    }
}
