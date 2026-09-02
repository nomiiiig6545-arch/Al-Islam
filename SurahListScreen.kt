package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.api.Surah
import com.example.data.audio.AudioDownloadStatus
import com.example.data.model.ALL_RECITERS
import com.example.data.model.AVAILABLE_RECITERS
import com.example.data.model.Reciter
import com.example.data.mushaf.IndoPakMushafData
import com.example.ui.QuranViewModel
import com.example.ui.SurahsState
import com.example.ui.components.ReciterImage
import com.example.ui.theme.ArabicFontFamily
import com.example.ui.theme.HandmadeBrushesFontFamily
import com.example.ui.theme.UrduFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahListScreen(
    viewModel: QuranViewModel,
    onPlaySurahClick: (reciterId: String, surahNumber: Int) -> Unit = { _, _ -> },
    onRecentlyPlayedClick: () -> Unit = {},
    onFavoritesClick: () -> Unit = {},
    onMushafClick: () -> Unit = {},
    onPrayerTimesClick: () -> Unit = {},
    onNavigateToReciters: () -> Unit = {},
    onNavigateToTranslationReciters: () -> Unit = onNavigateToReciters,
    onNavigateToReciterSurahs: (String) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onBackClick: () -> Unit
) {
    val surahsState by viewModel.surahsState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val recentSurahs by viewModel.recentSurahs.collectAsStateWithLifecycle()
    val cachedSurahs by viewModel.cachedSurahs.collectAsStateWithLifecycle()
    val downloadingSurahs by viewModel.downloadingSurahNumbers.collectAsStateWithLifecycle()
    val isBatchDownloadingAudio by viewModel.isBatchDownloadingAudio.collectAsStateWithLifecycle()
    val isBatchDownloadingTranslation by viewModel.isBatchDownloadingTranslation.collectAsStateWithLifecycle()
    val selectedReciter by viewModel.selectedReciter.collectAsStateWithLifecycle()
    val isSurahAudioPlaying by viewModel.isSurahAudioPlaying.collectAsStateWithLifecycle()
    val currentPlayingSurah by viewModel.currentPlayingSurah.collectAsStateWithLifecycle()
    val currentlyPlayingType by viewModel.currentlyPlayingType.collectAsStateWithLifecycle()
    var showReciterDialog by remember { mutableStateOf(false) }

    val arabicFontFamily = ArabicFontFamily
    val context = LocalContext.current
    var showExitDialog by remember { mutableStateOf(false) }


    // Design System Color Tokens (High Contrast, Bold, Vibrant)
    val isDark = MaterialTheme.colorScheme.background.red * 0.299f +
                 MaterialTheme.colorScheme.background.green * 0.587f +
                 MaterialTheme.colorScheme.background.blue * 0.114f < 0.5f

    val bgColor = MaterialTheme.colorScheme.background
    val topBarBgColor = MaterialTheme.colorScheme.background
    val topBarContentColor = MaterialTheme.colorScheme.primary
    val topBarBorderColor = MaterialTheme.colorScheme.outlineVariant

    val headerBgColor = if (isDark) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary
    val headerTitleColor = if (isDark) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary
    val headerSubtitleColor = MaterialTheme.colorScheme.secondary

    val searchBarBgColor = MaterialTheme.colorScheme.surface
    val searchBarBorderColor = MaterialTheme.colorScheme.outlineVariant
    val searchTextColor = MaterialTheme.colorScheme.onSurface
    val searchPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant

    val cardBgColor = MaterialTheme.colorScheme.surface
    val cardBorderColor = MaterialTheme.colorScheme.outlineVariant
    val badgeBgColor = MaterialTheme.colorScheme.primaryContainer
    val badgeTextColor = MaterialTheme.colorScheme.onPrimaryContainer
    val cardTitleColor = MaterialTheme.colorScheme.onSurface
    val cardSubtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val arabicTitleColor = MaterialTheme.colorScheme.primary
    val cardDividerColor = MaterialTheme.colorScheme.outlineVariant

    val arabicBtnBgColor = if (isDark) Color(0xFF0D281E) else Color(0xFFE8F5EE)
    val arabicBtnTextColor = if (isDark) Color(0xFF6EE7B7) else Color(0xFF0A3324)
    val transBtnBgColor = if (isDark) Color(0xFF26190B) else Color(0xFFFEF3C7)
    val transBtnTextColor = if (isDark) Color(0xFFFCD34D) else Color(0xFF78350F)
    val downloadBorderColor = if (isDark) Color(0xFF1E523A) else Color(0xFF10B981).copy(alpha = 0.6f)
    val transBorderColor = if (isDark) Color(0xFF5E3A10) else Color(0xFFF59E0B).copy(alpha = 0.6f)

    BackHandler {
        onBackClick()
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = {
                Text(
                    text = "Exit App",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to exit Al-Quran Majeed?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        (context as? Activity)?.finish()
                    }
                ) {
                    Text("Exit", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Cancel", color = topBarContentColor)
                }
            }
        )
    }

    if (showReciterDialog) {
        ReciterSelectionDialog(
            currentReciter = selectedReciter,
            onDismiss = { showReciterDialog = false },
            onSelectReciter = { reciter ->
                viewModel.setSelectedReciter(reciter.id)
                showReciterDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(bgColor)
    ) {
        // Top Header Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = topBarBgColor
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = topBarContentColor
                        )
                    }

                    Text(
                        text = "Al-Quran Majeed",
                        fontFamily = HandmadeBrushesFontFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = topBarContentColor,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.size(48.dp))
                }

                // Header Bottom Accent Line
                HorizontalDivider(
                    thickness = 2.dp,
                    color = topBarBorderColor
                )
            }
        }

        val listState = rememberLazyListState()
        LaunchedEffect(Unit) {
            listState.scrollToItem(0, 0)
        }

        // Scrollable Body Content
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Banner Section
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = headerBgColor,
                    border = BorderStroke(1.5.dp, Color(0xFFD4AF37)),
                    shadowElevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Decorative Background Watermark Icon
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.08f),
                            modifier = Modifier
                                .size(110.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 20.dp, y = (-20).dp)
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Audio Quran with Tarjama",
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = headerTitleColor,
                                letterSpacing = 0.8.sp,
                                textAlign = TextAlign.Center
                            )

                            // Decorative Gold Divider & Star Ornaments
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(40.dp)
                                        .height(1.5.dp)
                                        .background(Color(0xFFD4AF37))
                                )
                                Text(
                                    text = "✦",
                                    fontSize = 12.sp,
                                    color = Color(0xFFD4AF37)
                                )
                                Box(
                                    modifier = Modifier
                                        .width(40.dp)
                                        .height(1.5.dp)
                                        .background(Color(0xFFD4AF37))
                                )
                            }

                            Text(
                                text = "Listen to beautiful recitations with translations.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = headerSubtitleColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Reciter Selector Pill (Tapping opens the Reciter Selection modal dialog)
                            Surface(
                                onClick = { showReciterDialog = true },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.Black.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.testTag("reciter_selector_pill")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    ReciterImage(
                                        reciter = selectedReciter,
                                        size = 24.dp,
                                        borderWidth = 1.dp,
                                        borderColor = Color(0xFFD4AF37)
                                    )

                                    Text(
                                        text = selectedReciter.name,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                    )

                                    Icon(
                                        imageVector = Icons.Default.ArrowForwardIos,
                                        contentDescription = "Select Reciter",
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Search Bar & History Button Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Search Bar Input
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = CircleShape,
                        color = searchBarBgColor,
                        border = BorderStroke(1.dp, searchBarBorderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = searchPlaceholderColor,
                                modifier = Modifier.size(20.dp)
                            )

                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search Surah...",
                                        style = TextStyle(
                                            color = searchPlaceholderColor,
                                            fontSize = 14.sp
                                        )
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.updateSearchQuery(it) },
                                    textStyle = TextStyle(
                                        color = searchTextColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    cursorBrush = SolidColor(searchTextColor),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.updateSearchQuery("") },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = searchPlaceholderColor
                                    )
                                }
                            }
                        }
                    }

                    // Favorites Button (Rounded Rectangle Shape)
                    Surface(
                        onClick = onFavoritesClick,
                        modifier = Modifier
                            .size(width = 48.dp, height = 46.dp)
                            .testTag("audio_favorites_button"),
                        shape = RoundedCornerShape(12.dp),
                        color = headerBgColor.copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f)),
                        shadowElevation = 2.dp
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Favorite Recitations",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Recently Played/History Button (Rounded Rectangle Shape)
                    Surface(
                        onClick = onRecentlyPlayedClick,
                        modifier = Modifier
                            .size(width = 48.dp, height = 46.dp)
                            .testTag("recently_played_history_button"),
                        shape = RoundedCornerShape(12.dp),
                        color = headerBgColor.copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        shadowElevation = 2.dp
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Recently Played",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Download Action Buttons (Side-by-side: Download Audio & Download with Translation)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Button: "Download Audio" (Dark Green tone matching Arabic Play)
                    Button(
                        onClick = onNavigateToReciters,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("download_audio_all_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = arabicBtnBgColor,
                            contentColor = arabicBtnTextColor
                        ),
                        border = BorderStroke(1.5.dp, downloadBorderColor),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 3.dp,
                            pressedElevation = 6.dp
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isBatchDownloadingAudio) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = arabicBtnTextColor,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Downloading...",
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = arabicBtnTextColor
                                    ),
                                    maxLines = 1
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Download Audio",
                                    tint = arabicBtnTextColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Download Audio",
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = arabicBtnTextColor
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Right Button: "Download with Translation" (Translation Gold #FBC02D)
                    Button(
                        onClick = onNavigateToTranslationReciters,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("download_translation_all_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = transBtnBgColor,
                            contentColor = transBtnTextColor
                        ),
                        border = BorderStroke(1.5.dp, transBorderColor),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 3.dp,
                            pressedElevation = 6.dp
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isBatchDownloadingTranslation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = transBtnTextColor,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Downloading...",
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = transBtnTextColor
                                    ),
                                    maxLines = 1
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = "Download with Translation",
                                    tint = transBtnTextColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Download with Translation",
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = transBtnTextColor
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Surah Cards List State
            when (val state = surahsState) {
                is SurahsState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = topBarContentColor)
                        }
                    }
                }
                is SurahsState.Error -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.loadSurahs() },
                                    colors = ButtonDefaults.buttonColors(containerColor = headerBgColor)
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }
                is SurahsState.Success -> {
                    val filteredSurahs = if (searchQuery.isBlank()) {
                        state.surahs
                    } else {
                        state.surahs.filter {
                            it.englishName.contains(searchQuery, ignoreCase = true) ||
                                    it.name.contains(searchQuery, ignoreCase = true) ||
                                    it.englishNameTranslation.contains(searchQuery, ignoreCase = true) ||
                                    it.number.toString() == searchQuery.trim()
                        }
                    }

                    items(filteredSurahs, key = { it.number }) { surah ->
                        val isThisSurahPlaying = isSurahAudioPlaying && currentPlayingSurah == surah.number
                        val isArabicPlaying = isThisSurahPlaying && currentlyPlayingType == "ARABIC"
                        val isTranslationPlaying = isThisSurahPlaying && currentlyPlayingType == "TRANSLATION"

                        SurahCardItem(
                            surah = surah,
                            arabicFontFamily = arabicFontFamily,
                            isArabicPlaying = isArabicPlaying,
                            isTranslationPlaying = isTranslationPlaying,
                            cardBgColor = cardBgColor,
                            cardBorderColor = cardBorderColor,
                            badgeBgColor = badgeBgColor,
                            badgeTextColor = badgeTextColor,
                            cardTitleColor = cardTitleColor,
                            cardSubtitleColor = cardSubtitleColor,
                            arabicTitleColor = arabicTitleColor,
                            cardDividerColor = cardDividerColor,
                            arabicBtnBgColor = arabicBtnBgColor,
                            arabicBtnTextColor = arabicBtnTextColor,
                            transBtnBgColor = transBtnBgColor,
                            transBtnTextColor = transBtnTextColor,
                            downloadBorderColor = downloadBorderColor,
                            transBorderColor = transBorderColor,
                            onArabicPlayClick = {
                                val reciterId = if (!selectedReciter.isTranslation) {
                                    selectedReciter.id
                                } else {
                                    "ar.alafasy"
                                }
                                onPlaySurahClick(reciterId, surah.number)
                            },
                            onTranslationPlayClick = {
                                val reciterId = if (selectedReciter.isTranslation) {
                                    selectedReciter.id
                                } else {
                                    "ur.khan"
                                }
                                onPlaySurahClick(reciterId, surah.number)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SurahCardItem(
    surah: Surah,
    arabicFontFamily: androidx.compose.ui.text.font.FontFamily,
    isArabicPlaying: Boolean,
    isTranslationPlaying: Boolean,
    cardBgColor: Color,
    cardBorderColor: Color,
    badgeBgColor: Color,
    badgeTextColor: Color,
    cardTitleColor: Color,
    cardSubtitleColor: Color,
    arabicTitleColor: Color,
    cardDividerColor: Color,
    arabicBtnBgColor: Color,
    arabicBtnTextColor: Color,
    transBtnBgColor: Color,
    transBtnTextColor: Color,
    downloadBorderColor: Color,
    transBorderColor: Color,
    onArabicPlayClick: () -> Unit,
    onTranslationPlayClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("surah_item_${surah.number}"),
        shape = RoundedCornerShape(16.dp),
        color = cardBgColor,
        border = BorderStroke(1.dp, cardBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Row: Badge, Names, Arabic Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Circular Surah Number Badge
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(badgeBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = surah.number.toString(),
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeTextColor
                            )
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = surah.englishName,
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = cardTitleColor
                            )
                        )

                        Text(
                            text = "${surah.englishNameTranslation} • ${surah.numberOfAyahs} Ayahs",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = cardSubtitleColor
                            )
                        )
                    }
                }

                // Arabic Surah Title (Clean name without Surah / Suratul prefix)
                val cleanArabicName = IndoPakMushafData.SURAH_NAMES_ARABIC.getOrNull(surah.number - 1)
                    ?: surah.name.replace(Regex("^(سُورَةُ|سُورَةِ|سُورَةَ|سُورَةٌ|سورة|سُورَة|سُورَتُ|سورةُ)\\s*"), "").trim()

                Text(
                    text = cleanArabicName,
                    fontFamily = arabicFontFamily,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = arabicTitleColor
                )
            }

            // Divider Line
            HorizontalDivider(
                thickness = 1.dp,
                color = cardDividerColor
            )

            // Bottom Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Arabic Play Button
                val currentArabicBgColor = if (isArabicPlaying) MaterialTheme.colorScheme.primary else arabicBtnBgColor
                val currentArabicTextColor = if (isArabicPlaying) MaterialTheme.colorScheme.onPrimary else arabicBtnTextColor
                val currentArabicBorderColor = if (isArabicPlaying) MaterialTheme.colorScheme.primary else downloadBorderColor

                Button(
                    onClick = onArabicPlayClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = currentArabicBgColor,
                        contentColor = currentArabicTextColor
                    ),
                    border = BorderStroke(1.5.dp, currentArabicBorderColor),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 5.dp
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (isArabicPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Arabic Play",
                            tint = currentArabicTextColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Arabic Play",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = currentArabicTextColor
                            ),
                            maxLines = 1
                        )
                    }
                }

                // With Translation Button
                val currentTransBgColor = if (isTranslationPlaying) MaterialTheme.colorScheme.secondary else transBtnBgColor
                val currentTransTextColor = if (isTranslationPlaying) MaterialTheme.colorScheme.onSecondary else transBtnTextColor
                val currentTransBorderColor = if (isTranslationPlaying) MaterialTheme.colorScheme.secondary else transBorderColor

                Button(
                    onClick = onTranslationPlayClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = currentTransBgColor,
                        contentColor = currentTransTextColor
                    ),
                    border = BorderStroke(1.5.dp, currentTransBorderColor),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 5.dp
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (isTranslationPlaying) Icons.Default.Pause else Icons.Default.Translate,
                            contentDescription = "With Translation",
                            tint = currentTransTextColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "With Translation",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = currentTransTextColor
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun ReciterSelectionDialog(
    currentReciter: Reciter,
    onDismiss: () -> Unit,
    onSelectReciter: (Reciter) -> Unit
) {
    var showTranslationTab by remember { mutableStateOf(currentReciter.isTranslation) }
    val filteredReciters = remember(showTranslationTab) {
        if (showTranslationTab) ALL_RECITERS.filter { it.isTranslation }
        else ALL_RECITERS.filter { !it.isTranslation }
    }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val dialogBg = if (isDark) Color(0xFF1B231F) else MaterialTheme.colorScheme.surface
    val titleTextColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val tabContainerBg = if (isDark) Color(0xFF111714) else Color(0xFFE5E7EB)
    val activeAccent = if (showTranslationTab) (if (isDark) Color(0xFFF5D061) else Color(0xFFB5870F)) else (if (isDark) Color(0xFF34D399) else Color(0xFF059669))

    val arabicBtnBgColor = MaterialTheme.colorScheme.primary
    val arabicBtnTextColor = MaterialTheme.colorScheme.onPrimary
    val downloadBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)

    val transBtnBgColor = MaterialTheme.colorScheme.secondary
    val transBtnTextColor = MaterialTheme.colorScheme.onSecondary
    val transBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogBg,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Select Reciter (Qari)",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = titleTextColor,
                    fontSize = 20.sp
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Category Filter Buttons (Arabic vs Translation)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(tabContainerBg)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Arabic Tab Button
                    val isArabicActive = !showTranslationTab
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
                            .clickable { showTranslationTab = false }
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
                                    fontSize = 12.sp,
                                    fontFamily = UrduFontFamily,
                                    color = arabicText
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Urdu Translation Tab Button
                    val isTransActive = showTranslationTab
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
                            .clickable { showTranslationTab = true }
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
                                    fontSize = 12.sp,
                                    fontFamily = UrduFontFamily,
                                    color = transText
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Scrollable List of Reciters
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    filteredReciters.forEach { reciter ->
                        val isSelected = reciter.id == currentReciter.id
                        val itemAccent = if (reciter.isTranslation) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        val itemBorderColor = if (reciter.isTranslation) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        val itemTitleActiveColor = if (reciter.isTranslation) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary

                        val itemBg = if (isSelected) {
                            if (reciter.isTranslation) {
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                            } else {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            }
                        } else {
                            if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                        }

                        val itemBorder = if (isSelected) {
                            BorderStroke(1.5.dp, itemBorderColor)
                        } else {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectReciter(reciter) },
                            shape = RoundedCornerShape(14.dp),
                            color = itemBg,
                            border = itemBorder
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    ReciterImage(
                                        reciter = reciter,
                                        size = 38.dp,
                                        borderWidth = 1.5.dp,
                                        borderColor = if (isSelected) itemBorderColor else (if (isDark) Color(0xFF4B5563) else Color(0xFFD1D5DB))
                                    )
                                    Column {
                                        Text(
                                            text = reciter.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) (if (isDark) itemTitleActiveColor else itemAccent) else titleTextColor,
                                                fontSize = 14.sp
                                            ),
                                            maxLines = 1
                                        )
                                        Text(
                                            text = reciter.subtext,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = if (isDark) Color(0xFF9CA3AF) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 11.sp
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                }
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onSelectReciter(reciter) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = itemBorderColor,
                                        unselectedColor = if (isDark) Color(0xFF6B7280) else Color(0xFF9CA3AF)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Close",
                    color = activeAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    )
}
