package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.audio.AudioDownloadStatus
import com.example.data.model.AVAILABLE_RECITERS
import com.example.data.model.TRANSLATION_RECITERS
import com.example.data.model.Reciter
import com.example.data.model.PlayerThemeId
import com.example.data.model.AudioFavorite
import com.example.data.mushaf.IndoPakMushafData
import com.example.ui.AudioRepeatMode
import com.example.ui.QuranViewModel
import com.example.ui.SurahsState
import com.example.ui.components.ReciterImage
import com.example.ui.theme.ArabicFontFamily
import com.example.ui.theme.UrduFontFamily
import com.example.ui.theme.UthmaniFontFamily
import com.example.ui.theme.QuranGreen
import com.example.ui.theme.QuranYellow
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineAudioPlayerScreen(
    reciterId: String,
    initialSurahNumber: Int,
    viewModel: QuranViewModel,
    onNavigateToReciterSurahs: ((String) -> Unit)? = null,
    onNavigateToFavorites: (() -> Unit)? = null,
    onBackClick: () -> Unit
) {
    val selectedReciter by viewModel.selectedReciter.collectAsStateWithLifecycle()
    val currentOfflineReciterId by viewModel.currentOfflineReciterId.collectAsStateWithLifecycle()

    val activeReciterId = remember(reciterId, currentOfflineReciterId, selectedReciter) {
        if (reciterId.isNotBlank()) {
            reciterId
        } else {
            currentOfflineReciterId ?: selectedReciter.id
        }
    }

    val reciter = remember(activeReciterId) {
        viewModel.getReciterById(activeReciterId)
    }

    val currentSurahNumber by viewModel.currentOfflineSurahNumber.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlayerPlaying.collectAsStateWithLifecycle()
    val isPaused by viewModel.isPlayerPaused.collectAsStateWithLifecycle()
    val isLoading by viewModel.isPlayerLoading.collectAsStateWithLifecycle()
    val currentPositionMs by viewModel.playerPositionMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.playerDurationMs.collectAsStateWithLifecycle()
    val repeatMode by viewModel.audioRepeatMode.collectAsStateWithLifecycle()
    val playbackSpeed by viewModel.playbackSpeed.collectAsStateWithLifecycle()
    val sleepTimerMinutes by viewModel.sleepTimerMinutes.collectAsStateWithLifecycle()
    val sleepTimerRemainingSec by viewModel.sleepTimerRemainingSeconds.collectAsStateWithLifecycle()
    val playerThemeId by viewModel.playerThemeId.collectAsStateWithLifecycle()
    val playerTheme = remember(playerThemeId) { PlayerThemeId.fromId(playerThemeId) }
    val audioFavorites by viewModel.audioFavorites.collectAsStateWithLifecycle()
    val surahsState by viewModel.surahsState.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val autoNextSurah by viewModel.autoNextSurah.collectAsStateWithLifecycle()
    val nextSurahTransitionMode by viewModel.nextSurahTransitionMode.collectAsStateWithLifecycle()
    val nextSurahDelaySeconds by viewModel.nextSurahDelaySeconds.collectAsStateWithLifecycle()
    val skipSilenceInterval by viewModel.skipSilenceInterval.collectAsStateWithLifecycle()
    val skipIntroSilence by viewModel.skipIntroSilence.collectAsStateWithLifecycle()
    val seekIntervalSeconds by viewModel.seekIntervalSeconds.collectAsStateWithLifecycle()
    val audioStreamQuality by viewModel.audioStreamQuality.collectAsStateWithLifecycle()

    val activeSurahNum = currentSurahNumber ?: initialSurahNumber

    // Auto-start playback on screen entry if not currently playing this surah or reciter
    LaunchedEffect(activeReciterId, initialSurahNumber) {
        viewModel.setSelectedReciter(activeReciterId)
        if (!isPlaying || currentSurahNumber != initialSurahNumber || currentOfflineReciterId != activeReciterId) {
            viewModel.playOfflineSurah(activeReciterId, initialSurahNumber)
        }
    }

    // Lookup Surah info
    val currentSurahInfo: com.example.data.api.Surah = remember(activeSurahNum, surahsState) {
        val fromState = when (val state = surahsState) {
            is SurahsState.Success -> state.surahs.find { it.number == activeSurahNum }
            else -> null
        }
        if (fromState != null) {
            fromState
        } else {
            val idx = (activeSurahNum - 1).coerceIn(0, 113)
            val arabicName = IndoPakMushafData.SURAH_NAMES_ARABIC.getOrElse(idx) { "سورة" }
            val englishName = IndoPakMushafData.SURAH_NAMES_ENGLISH.getOrElse(idx) { "Surah $activeSurahNum" }
            val ayahCount = IndoPakMushafData.SURAH_AYAH_COUNTS.getOrElse(idx) { 7 }
            val revType = if (activeSurahNum in listOf(2, 3, 4, 5, 8, 9, 22, 24, 33, 47, 48, 49, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 76, 98, 110)) "Madani" else "Makki"
            com.example.data.api.Surah(
                number = activeSurahNum,
                name = arabicName,
                englishName = englishName,
                englishNameTranslation = revType,
                numberOfAyahs = ayahCount,
                revelationType = revType
            )
        }
    }

    val audioDownloadStates by viewModel.audioDownloadStates.collectAsStateWithLifecycle()
    val downloadStatus = remember(activeReciterId, activeSurahNum, audioDownloadStates) {
        val key = "$activeReciterId-$activeSurahNum"
        audioDownloadStates[key] ?: if (viewModel.isSurahAudioDownloaded(activeReciterId, activeSurahNum)) {
            AudioDownloadStatus.Downloaded
        } else {
            AudioDownloadStatus.NotDownloaded
        }
    }

    val isDownloaded = downloadStatus is AudioDownloadStatus.Downloaded
    val isDownloading = downloadStatus is AudioDownloadStatus.Downloading
    val isDownloadPaused = downloadStatus is AudioDownloadStatus.Paused
    val downloadProgress = when (downloadStatus) {
        is AudioDownloadStatus.Downloading -> downloadStatus.progress
        is AudioDownloadStatus.Paused -> downloadStatus.progress
        is AudioDownloadStatus.Downloaded -> 1f
        else -> 0f
    }

    val isBookmarked = remember(activeSurahNum, bookmarks) {
        bookmarks.any { it.surahNumber == activeSurahNum && it.ayahNumberInSurah == 0 && it.pageNumber == 0 }
    }

    var showPlaylistSheet by remember { mutableStateOf(false) }
    var showReciterPickerSheet by remember { mutableStateOf(false) }
    var showThemePickerSheet by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showPlaybackSettingsSheet by remember { mutableStateOf(false) }
    var isDraggingSlider by remember { mutableStateOf(false) }
    var draggedSliderValue by remember { mutableFloatStateOf(0f) }

    val isFavorite = remember(activeReciterId, activeSurahNum, audioFavorites) {
        audioFavorites.any { it.reciterId == activeReciterId && it.surahNumber == activeSurahNum }
    }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // Theme Tokens
    val primaryGreen = MaterialTheme.colorScheme.primary
    val goldYellow = MaterialTheme.colorScheme.secondary
    val cardBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
    val cardBorder = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.6f else 0.4f)

    val sliderPosition = if (isDraggingSlider) {
        draggedSliderValue
    } else {
        if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    }

    // Animation for soundwaves
    val infiniteTransition = rememberInfiniteTransition(label = "soundwave_pulse")
    val waveHeight1 by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "w1"
    )
    val waveHeight2 by infiniteTransition.animateFloat(
        initialValue = 24f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "w2"
    )
    val waveHeight3 by infiniteTransition.animateFloat(
        initialValue = 16f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(520, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "w3"
    )

    val cleanArabicTitle = remember(currentSurahInfo.name) {
        if (currentSurahInfo.name.startsWith("سورة") || currentSurahInfo.name.startsWith("سُورَة")) {
            currentSurahInfo.name
        } else {
            "سورة ${currentSurahInfo.name}"
        }
    }

    BackHandler {
        onBackClick()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = cleanArabicTitle,
                        fontFamily = UthmaniFontFamily,
                        color = playerTheme.textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("offline_player_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = playerTheme.controlTint
                        )
                    }
                },
                actions = {
                    // Favorites List Icon
                    IconButton(
                        onClick = { onNavigateToFavorites?.invoke() },
                        modifier = Modifier.testTag("player_favorites_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Favorite Recitations",
                            tint = playerTheme.controlTint
                        )
                    }

                    // Theme Selector Icon (Paint Brush / Palette 🎨)
                    IconButton(
                        onClick = { showThemePickerSheet = true },
                        modifier = Modifier.testTag("player_theme_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Player Themes",
                            tint = playerTheme.controlTint
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = playerTheme.textPrimary,
                    navigationIconContentColor = playerTheme.controlTint,
                    actionIconContentColor = playerTheme.controlTint
                ),
                modifier = Modifier
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            playerTheme.topGradientColor,
                            playerTheme.bottomGradientColor
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Main Audio Visualizer Disc with central Reciter Picture & 360-degree Radial Lines
            RadialVisualizerVinylDisc(
                reciter = reciter,
                isPlaying = isPlaying,
                progress = sliderPosition,
                surahNameArabic = currentSurahInfo.name,
                surahNameEnglish = currentSurahInfo.englishName,
                theme = playerTheme
            )

            // 4 Action Tools Row (Star, Equalizer, Sleep Timer, Settings)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Star / Audio Favorite
                IconButton(
                    onClick = {
                        viewModel.toggleAudioFavorite(
                            activeReciterId,
                            activeSurahNum,
                            currentSurahInfo.name,
                            currentSurahInfo.englishName
                        )
                    },
                    modifier = Modifier.testTag("action_tool_bookmark")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) playerTheme.accentPrimary else playerTheme.controlTint.copy(alpha = 0.8f),
                        modifier = Modifier.size(28.dp)
                    )
                }

                // 2. Playback Speed (Equalizer Icon)
                IconButton(
                    onClick = { showSpeedDialog = true },
                    modifier = Modifier.testTag("action_tool_speed")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Speed/Equalizer",
                        tint = playerTheme.controlTint.copy(alpha = 0.8f),
                        modifier = Modifier.size(28.dp)
                    )
                }

                // 3. Sleep Timer
                IconButton(
                    onClick = { showSleepTimerDialog = true },
                    modifier = Modifier.testTag("action_tool_sleep_timer")
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Sleep Timer",
                        tint = if (sleepTimerMinutes > 0) playerTheme.accentPrimary else playerTheme.controlTint.copy(alpha = 0.8f),
                        modifier = Modifier.size(28.dp)
                    )
                }

                // 4. Settings / More
                IconButton(
                    onClick = { showPlaybackSettingsSheet = true },
                    modifier = Modifier.testTag("action_tool_settings")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Playback Settings",
                        tint = playerTheme.controlTint.copy(alpha = 0.8f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Progress Slider & Timestamps
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayedPositionMs = if (isDraggingSlider) {
                        (draggedSliderValue * durationMs).toLong()
                    } else {
                        currentPositionMs
                    }

                    Text(
                        text = formatDuration(displayedPositionMs),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = playerTheme.textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    
                    Slider(
                        value = sliderPosition,
                        onValueChange = { newValue ->
                            isDraggingSlider = true
                            draggedSliderValue = newValue
                        },
                        onValueChangeFinished = {
                            val targetMs = (draggedSliderValue * durationMs).toLong()
                            viewModel.seekAudioTo(targetMs)
                            isDraggingSlider = false
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = playerTheme.accentPrimary,
                            activeTrackColor = playerTheme.accentPrimary,
                            inactiveTrackColor = playerTheme.inactiveTint
                        ),
                        thumb = {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(playerTheme.accentPrimary, CircleShape)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                            .testTag("audio_progress_slider")
                    )

                    Text(
                        text = formatDuration(durationMs),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = playerTheme.textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Playback Controls Row (Repeat, Previous, Play/Pause, Next, Queue)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Repeat Mode Toggle Button
                IconButton(
                    onClick = { viewModel.toggleAudioRepeatMode() },
                    modifier = Modifier.testTag("repeat_mode_button")
                ) {
                    when (repeatMode) {
                        AudioRepeatMode.OFF -> {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "Repeat Off",
                                tint = playerTheme.inactiveTint,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        AudioRepeatMode.ONE -> {
                            Icon(
                                imageVector = Icons.Default.RepeatOne,
                                contentDescription = "Repeat One",
                                tint = playerTheme.accentPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        AudioRepeatMode.ALL -> {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "Repeat All",
                                tint = playerTheme.accentPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                // Previous Surah Button
                IconButton(
                    onClick = { viewModel.playPreviousOfflineSurah() },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("previous_surah_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Surah",
                        tint = playerTheme.controlTint,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Big Play/Pause Action Button
                Surface(
                    onClick = {
                        viewModel.togglePlayPauseOffline(activeReciterId, activeSurahNum)
                    },
                    shape = CircleShape,
                    color = playerTheme.playButtonBg,
                    modifier = Modifier
                        .size(72.dp)
                        .testTag("play_pause_offline_button")
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = playerTheme.playButtonIcon,
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 3.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = playerTheme.playButtonIcon,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }
                }

                // Next Surah Button
                IconButton(
                    onClick = { viewModel.playNextOfflineSurah() },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("next_surah_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Surah",
                        tint = playerTheme.controlTint,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Quick Playlist Switcher Button (Queue)
                IconButton(
                    onClick = { showPlaylistSheet = true },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("quick_playlist_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "Playlist",
                        tint = playerTheme.controlTint.copy(alpha = 0.7f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
    }

    // Theme Picker Modal Sheet
    if (showThemePickerSheet) {
        ThemePickerBottomSheet(
            currentThemeId = playerThemeId,
            onThemeSelected = { newThemeId ->
                viewModel.setPlayerTheme(newThemeId)
            },
            onDismiss = { showThemePickerSheet = false }
        )
    }

    // Reciter Selector Modal Sheet
    if (showReciterPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showReciterPickerSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            var showSheetTranslationTab by remember { mutableStateOf(reciter.isTranslation) }
            val sheetReciters = remember(showSheetTranslationTab) {
                if (showSheetTranslationTab) TRANSLATION_RECITERS else AVAILABLE_RECITERS
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Select Quran Reciter (Qari)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Category Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Color(0xFF111714) else Color(0xFFE5E7EB))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val isArabicActive = !showSheetTranslationTab
                    val arabicBg = if (isArabicActive) {
                        if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.Black.copy(alpha = 0.2f)
                    } else {
                        Color.Transparent
                    }
                    val arabicText = if (isArabicActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    val arabicBorder = if (isArabicActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(arabicBg)
                            .border(if (isArabicActive) 1.5.dp else 1.dp, arabicBorder, RoundedCornerShape(12.dp))
                            .clickable { showSheetTranslationTab = false }
                            .padding(vertical = 9.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = arabicText
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "تلاوت (عربی)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = UrduFontFamily,
                                    fontSize = 12.sp,
                                    color = arabicText
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    val isTransActive = showSheetTranslationTab
                    val transBg = if (isTransActive) {
                        if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.Black.copy(alpha = 0.2f)
                    } else {
                        Color.Transparent
                    }
                    val transText = if (isTransActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    val transBorder = if (isTransActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(transBg)
                            .border(if (isTransActive) 1.5.dp else 1.dp, transBorder, RoundedCornerShape(12.dp))
                            .clickable { showSheetTranslationTab = true }
                            .padding(vertical = 9.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = transText
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "تلاوت مع اردو ترجمہ",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = UrduFontFamily,
                                    fontSize = 12.sp,
                                    color = transText
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val reciterSheetListState = rememberLazyListState()
                LaunchedEffect(Unit) {
                    reciterSheetListState.scrollToItem(0, 0)
                }

                LazyColumn(
                    state = reciterSheetListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sheetReciters) { r ->
                        val isSelected = r.id == activeReciterId
                        val itemBorderColor = if (r.isTranslation) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        val itemTitleColor = if (r.isTranslation) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        val itemBg = if (isSelected) {
                            if (r.isTranslation) {
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                            } else {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            }
                        } else {
                            if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showReciterPickerSheet = false
                                    viewModel.playOfflineSurah(r.id, activeSurahNum)
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = itemBg
                            ),
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) itemBorderColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ReciterImage(
                                    reciter = r,
                                    size = 48.dp,
                                    borderWidth = 1.5.dp,
                                    borderColor = if (isSelected) itemBorderColor else (if (isDark) Color(0xFF4B5563) else Color(0xFFD1D5DB))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = r.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) itemTitleColor else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Text(
                                        text = "${r.nameArabic} • ${r.country}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = itemBorderColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Sleep Timer Selection Dialog
    if (showSleepTimerDialog) {
        AlertDialog(
            onDismissRequest = { showSleepTimerDialog = false },
            title = {
                Text(
                    text = "Sleep Timer",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        0 to "Turn Off Timer",
                        15 to "15 Minutes",
                        30 to "30 Minutes",
                        45 to "45 Minutes",
                        60 to "60 Minutes (1 Hour)"
                    ).forEach { (minutes, label) ->
                        val isSelected = sleepTimerMinutes == minutes
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setSleepTimer(minutes)
                                    showSleepTimerDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) primaryGreen else MaterialTheme.colorScheme.onSurface
                                )
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = primaryGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSleepTimerDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Playback Speed Controller Dialog
    if (showSpeedDialog) {
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = primaryGreen,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Playback Speed",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "رفتارِ تلاوت کنٹرول",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = primaryGreen
                        )
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Status Badge (Current Speed & Increase / Decrease Status)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            playbackSpeed > 1.0f -> primaryGreen.copy(alpha = 0.15f)
                            playbackSpeed < 1.0f -> goldYellow.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        border = BorderStroke(
                            1.dp,
                            when {
                                playbackSpeed > 1.0f -> primaryGreen.copy(alpha = 0.5f)
                                playbackSpeed < 1.0f -> goldYellow.copy(alpha = 0.5f)
                                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${playbackSpeed}x",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = when {
                                        playbackSpeed > 1.0f -> primaryGreen
                                        playbackSpeed < 1.0f -> goldYellow
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = when {
                                    playbackSpeed > 1.0f -> "▲ Speed Increased (Faster) • رفتار تیز ہے"
                                    playbackSpeed < 1.0f -> "▼ Speed Decreased (Slower) • رفتار دھیمی ہے"
                                    else -> "● Normal Speed (1.0x) • عام قدرتی رفتار"
                                },
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = when {
                                        playbackSpeed > 1.0f -> primaryGreen
                                        playbackSpeed < 1.0f -> goldYellow
                                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    }
                                )
                            )
                        }
                    }

                    // Stepper Controls: Explicit Decrease (-) and Increase (+)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.decreasePlaybackSpeed() },
                            enabled = playbackSpeed > 0.5f,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease speed",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Slower", style = MaterialTheme.typography.labelMedium)
                        }

                        Button(
                            onClick = { viewModel.increasePlaybackSpeed() },
                            enabled = playbackSpeed < 2.0f,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase speed",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Faster", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    // Presets Label
                    Text(
                        text = "Direct Speed Presets:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.align(Alignment.Start)
                    )

                    // Presets Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { sp ->
                            val isSelected = (playbackSpeed == sp)
                            Surface(
                                onClick = { viewModel.setPlaybackSpeed(sp) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) primaryGreen else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(
                                    0.8.dp,
                                    if (isSelected) primaryGreen else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${sp}x",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedDialog = false }) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                if (playbackSpeed != 1.0f) {
                    TextButton(onClick = { viewModel.resetPlaybackSpeed() }) {
                        Text("Reset (1.0x)")
                    }
                }
            }
        )
    }

    // Surah Playlist Modal Bottom Sheet
    if (showPlaylistSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPlaylistSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Surah Playlist (114 Surahs)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                val playlistListState = rememberLazyListState()
                LaunchedEffect(activeSurahNum) {
                    playlistListState.scrollToItem((activeSurahNum - 1).coerceIn(0, 113))
                }

                LazyColumn(
                    state = playlistListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(114) { idx ->
                        val surahNum = idx + 1
                        val isCurrentlyPlayingThis = (surahNum == activeSurahNum)
                        val arabicName = IndoPakMushafData.SURAH_NAMES_ARABIC.getOrElse(idx) { "سورة" }
                        val englishName = IndoPakMushafData.SURAH_NAMES_ENGLISH.getOrElse(idx) { "Surah $surahNum" }
                        val ayahCount = IndoPakMushafData.SURAH_AYAH_COUNTS.getOrElse(idx) { 7 }

                        Surface(
                            onClick = {
                                viewModel.playOfflineSurah(activeReciterId, surahNum)
                                showPlaylistSheet = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCurrentlyPlayingThis) primaryGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.dp,
                                if (isCurrentlyPlayingThis) primaryGreen else cardBorder.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Surah Number Badge
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (isCurrentlyPlayingThis) primaryGreen else MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$surahNum",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = if (isCurrentlyPlayingThis) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = englishName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isCurrentlyPlayingThis) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                    Text(
                                        text = "$ayahCount Verses",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    )
                                }

                                Text(
                                    text = arabicName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = ArabicFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrentlyPlayingThis) primaryGreen else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Playback Settings Modal Bottom Sheet
    if (showPlaybackSettingsSheet) {
        PlaybackSettingsBottomSheet(
            onDismiss = { showPlaybackSettingsSheet = false },
            autoNextSurah = autoNextSurah,
            nextSurahTransitionMode = nextSurahTransitionMode,
            nextSurahDelaySeconds = nextSurahDelaySeconds,
            skipSilenceInterval = skipSilenceInterval,
            skipIntroSilence = skipIntroSilence,
            seekIntervalSeconds = seekIntervalSeconds,
            playbackSpeed = playbackSpeed,
            audioStreamQuality = audioStreamQuality,
            onUpdateAutoNextSurah = { viewModel.updateAutoNextSurah(it) },
            onUpdateTransitionMode = { viewModel.updateNextSurahTransitionMode(it) },
            onUpdateNextSurahDelay = { viewModel.updateNextSurahDelaySeconds(it) },
            onUpdateSkipSilence = { viewModel.updateSkipSilenceInterval(it) },
            onUpdateSkipIntroSilence = { viewModel.updateSkipIntroSilence(it) },
            onUpdateSeekInterval = { viewModel.updateSeekIntervalSeconds(it) },
            onUpdatePlaybackSpeed = { viewModel.setPlaybackSpeed(it) },
            onUpdateAudioStreamQuality = { viewModel.updateAudioStreamQuality(it) },
            primaryGreen = primaryGreen,
            goldYellow = goldYellow
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackSettingsBottomSheet(
    onDismiss: () -> Unit,
    autoNextSurah: Boolean,
    nextSurahTransitionMode: String,
    nextSurahDelaySeconds: Int,
    skipSilenceInterval: Float,
    skipIntroSilence: Boolean,
    seekIntervalSeconds: Int,
    playbackSpeed: Float,
    audioStreamQuality: String,
    onUpdateAutoNextSurah: (Boolean) -> Unit,
    onUpdateTransitionMode: (String) -> Unit,
    onUpdateNextSurahDelay: (Int) -> Unit,
    onUpdateSkipSilence: (Float) -> Unit,
    onUpdateSkipIntroSilence: (Boolean) -> Unit,
    onUpdateSeekInterval: (Int) -> Unit,
    onUpdatePlaybackSpeed: (Float) -> Unit,
    onUpdateAudioStreamQuality: (String) -> Unit,
    primaryGreen: Color,
    goldYellow: Color
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = primaryGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Playback & Bandwidth Settings",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "ترتیباتِ تلاوت و کم ترین انٹرنیٹ کا استعمال",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val settingsListState = rememberLazyListState()
            LaunchedEffect(Unit) {
                settingsListState.scrollToItem(0, 0)
            }

            LazyColumn(
                state = settingsListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 0: Ultra Low Internet / Bandwidth Optimization
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                        ),
                        border = BorderStroke(1.dp, primaryGreen.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WifiTethering,
                                    contentDescription = null,
                                    tint = primaryGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Audio Bandwidth & Live Stream Data",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Stream and download using minimal bytes of internet",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            val qualityOptions = listOf(
                                "ULTRA_LOW" to "Micro-Bytes (<1MB / 32kbps)",
                                "STANDARD" to "Data Saver (64kbps)",
                                "HIGH" to "High Bitrate (128kbps)"
                            )

                            qualityOptions.forEach { (key, label) ->
                                val isSelected = audioStreamQuality == key
                                Surface(
                                    onClick = { onUpdateAudioStreamQuality(key) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) primaryGreen.copy(alpha = 0.15f) else Color.Transparent,
                                    border = BorderStroke(
                                        0.8.dp,
                                        if (isSelected) primaryGreen else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) primaryGreen else MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = primaryGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // Section 1: Automatic 'Next Surah' Transition
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(0.8.dp, primaryGreen.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Auto-Play Next Surah",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Automatically transition to the next Surah upon completion",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    )
                                }
                                Switch(
                                    checked = autoNextSurah,
                                    onCheckedChange = onUpdateAutoNextSurah,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = primaryGreen
                                    ),
                                    modifier = Modifier.testTag("auto_next_surah_switch")
                                )
                            }

                            if (autoNextSurah) {
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = primaryGreen.copy(alpha = 0.15f))
                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "Transition Mode",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = primaryGreen
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                val transitionModes = listOf(
                                    "CONTINUOUS" to "Continuous Flow (1-114)",
                                    "REPEAT_ONE" to "Repeat Current Surah",
                                    "STOP" to "Pause at Surah End"
                                )

                                transitionModes.forEach { (modeKey, modeTitle) ->
                                    val isSelected = nextSurahTransitionMode == modeKey
                                    Surface(
                                        onClick = { onUpdateTransitionMode(modeKey) },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) primaryGreen.copy(alpha = 0.15f) else Color.Transparent,
                                        border = BorderStroke(
                                            0.8.dp,
                                            if (isSelected) primaryGreen else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = modeTitle,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) primaryGreen else MaterialTheme.colorScheme.onSurface
                                                )
                                            )
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = primaryGreen,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Delay Before Next Surah",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = primaryGreen
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(
                                        0 to "0s (Instant)",
                                        3 to "3s",
                                        5 to "5s",
                                        10 to "10s"
                                    ).forEach { (delaySec, label) ->
                                        val isSelected = nextSurahDelaySeconds == delaySec
                                        Surface(
                                            onClick = { onUpdateNextSurahDelay(delaySec) },
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) primaryGreen else MaterialTheme.colorScheme.surface,
                                            border = BorderStroke(
                                                0.8.dp,
                                                if (isSelected) primaryGreen else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                            ),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 2: Custom Skip-Silence & Gapless Playback
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(0.8.dp, goldYellow.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = goldYellow,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Custom Skip-Silence & Trimming",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Trim silent pauses for a continuous recitation flow",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Silence Interval",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = goldYellow
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    0.0f to "Off",
                                    0.5f to "0.5s",
                                    1.0f to "1.0s",
                                    2.0f to "2.0s",
                                    3.0f to "3.0s"
                                ).forEach { (sec, label) ->
                                    val isSelected = (skipSilenceInterval == sec)
                                    Surface(
                                        onClick = { onUpdateSkipSilence(sec) },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) goldYellow else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(
                                            0.8.dp,
                                            if (isSelected) goldYellow else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Skip Intro Silence",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = "Jump directly to recitation on Surah start",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    )
                                }
                                Switch(
                                    checked = skipIntroSilence,
                                    onCheckedChange = onUpdateSkipIntroSilence,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = goldYellow
                                    ),
                                    modifier = Modifier.testTag("skip_intro_silence_switch")
                                )
                            }
                        }
                    }
                }

                // Section 3: Seek Skip Duration
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Seek Jump Duration (Rewind / Fast Forward)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Number of seconds skipped per seek button tap",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(5, 10, 15, 30).forEach { interval ->
                                    val isSelected = (seekIntervalSeconds == interval)
                                    Surface(
                                        onClick = { onUpdateSeekInterval(interval) },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) primaryGreen else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(
                                            0.8.dp,
                                            if (isSelected) primaryGreen else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${interval}s",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 4: Playback Speed Presets
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recitation Speed (رفتارِ تلاوت)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = when {
                                        playbackSpeed > 1.0f -> "${playbackSpeed}x (Faster ▲)"
                                        playbackSpeed < 1.0f -> "${playbackSpeed}x (Slower ▼)"
                                        else -> "1.0x (Normal)"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            playbackSpeed > 1.0f -> primaryGreen
                                            playbackSpeed < 1.0f -> goldYellow
                                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        }
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f).forEach { speed ->
                                    val isSelected = (playbackSpeed == speed)
                                    Surface(
                                        onClick = { onUpdatePlaybackSpeed(speed) },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) primaryGreen else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(
                                            0.8.dp,
                                            if (isSelected) primaryGreen else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${speed}x",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}

@Composable
fun RadialVisualizerVinylDisc(
    reciter: Reciter,
    isPlaying: Boolean,
    progress: Float,
    surahNameArabic: String,
    surahNameEnglish: String,
    theme: PlayerThemeId = PlayerThemeId.MIDNIGHT_DARK,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "disc_anim")

    // Continuous 360-degree rotation angle when playing
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Number of radial bars around 360 degrees
    val barCount = 72
    var waveHeights by remember { mutableStateOf(FloatArray(barCount) { 12f }) }

    // Animate radial wave lines going up and down when playing
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            var time = 0f
            while (isActive) {
                time += 0.16f
                val newHeights = FloatArray(barCount)
                for (i in 0 until barCount) {
                    val sin1 = sin(time * 2f + i * 0.25f)
                    val cos1 = cos(time * 1.3f - i * 0.18f)
                    val sin2 = sin(time * 3.1f + i * 0.4f)
                    // Bar length varies between 8.dp and 34.dp
                    val h = 8f + 26f * ((sin1 * cos1 + sin2 + 2f) / 4f)
                    newHeights[i] = h
                }
                waveHeights = newHeights
                delay(50)
            }
        } else {
            waveHeights = FloatArray(barCount) { 8f }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer Canvas drawing radial soundwave bars and vinyl disc grooves
        Canvas(
            modifier = Modifier.size(320.dp)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerMaxRadius = size.width / 2f
            val discRadius = outerMaxRadius * 0.70f // radius of inner vinyl disc

            // 1. Draw 72 Radial visualizer lines around 360 degrees
            val angleStep = 360f / barCount
            for (i in 0 until barCount) {
                val angleRad = Math.toRadians((i * angleStep).toDouble())
                val barLen = waveHeights.getOrElse(i) { 10f }.dp.toPx() * 1.5f // Make bars taller

                val innerPoint = Offset(
                    x = (center.x + (discRadius + 4.dp.toPx()) * cos(angleRad)).toFloat(),
                    y = (center.y + (discRadius + 4.dp.toPx()) * sin(angleRad)).toFloat()
                )
                val outerPoint = Offset(
                    x = (center.x + (discRadius + 4.dp.toPx() + barLen) * cos(angleRad)).toFloat(),
                    y = (center.y + (discRadius + 4.dp.toPx() + barLen) * sin(angleRad)).toFloat()
                )

                val alpha = if (isPlaying) (0.55f + (barLen / 50.dp.toPx()) * 0.45f).coerceIn(0.5f, 1f) else 0.35f
                val barColor = theme.controlTint

                drawLine(
                    color = barColor.copy(alpha = alpha),
                    start = innerPoint,
                    end = outerPoint,
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 2. Draw Vinyl Disc Base
            drawCircle(
                color = if (theme.isLight) Color(0xFFD6DFE8) else Color(0xFF13131A),
                radius = discRadius,
                center = center
            )

            // Vinyl Record Groove rings
            drawCircle(
                color = theme.controlTint.copy(alpha = 0.08f),
                radius = discRadius * 0.92f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = theme.controlTint.copy(alpha = 0.06f),
                radius = discRadius * 0.82f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = theme.controlTint.copy(alpha = 0.04f),
                radius = discRadius * 0.72f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            // Outer accent rim around vinyl record
            drawCircle(
                color = theme.accentPrimary.copy(alpha = 0.7f),
                radius = discRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // 3. Central Circular Profile Image (Artist / Reciter Picture)
        Box(
            modifier = Modifier
                .size(180.dp)
                .rotate(if (isPlaying) rotationAngle else 0f)
                .clip(CircleShape)
                .border(3.dp, theme.accentPrimary, CircleShape)
                .border(6.dp, theme.accentSecondary.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            ReciterImage(
                reciter = reciter,
                size = 180.dp,
                borderWidth = 0.dp,
                showBorder = false
            )

            // Central Vinyl Spindle Hole & Center Ring
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(theme.bottomGradientColor, CircleShape)
                    .border(3.dp, theme.accentPrimary, CircleShape)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemePickerBottomSheet(
    currentThemeId: String,
    onThemeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Player Themes",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "پلیئر کے خوبصورت رنگ منتخب کریں",
                        fontFamily = ArabicFontFamily,
                        fontSize = 14.sp,
                        color = QuranGreen
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_theme_picker")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val themeListState = rememberLazyListState()
            LaunchedEffect(Unit) {
                themeListState.scrollToItem(0, 0)
            }

            LazyColumn(
                state = themeListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(PlayerThemeId.entries) { theme ->
                    val isSelected = theme.id.equals(currentThemeId, ignoreCase = true)
                    Surface(
                        onClick = {
                            onThemeSelected(theme.id)
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = Color(theme.cardBgHex),
                        border = if (isSelected) BorderStroke(2.dp, Color(theme.accentPrimaryHex)) else BorderStroke(1.dp, Color(theme.accentSecondaryHex).copy(alpha = 0.25f)),
                        shadowElevation = if (isSelected) 6.dp else 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("theme_option_${theme.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular Dual Swatch
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.sweepGradient(
                                            listOf(
                                                Color(theme.topGradientHex),
                                                Color(theme.bottomGradientHex),
                                                Color(theme.accentPrimaryHex),
                                                Color(theme.topGradientHex)
                                            )
                                        )
                                    )
                                    .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(Color(theme.accentPrimaryHex), CircleShape)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = theme.title,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (theme.isLight) Color(0xFF0F172A) else Color.White
                                )
                                Text(
                                    text = theme.titleUrdu,
                                    fontFamily = ArabicFontFamily,
                                    fontSize = 13.sp,
                                    color = Color(theme.accentPrimaryHex)
                                )
                            }

                            if (isSelected) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(theme.accentPrimaryHex),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = if (theme.isLight) Color.White else Color(0xFF0F172A),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

