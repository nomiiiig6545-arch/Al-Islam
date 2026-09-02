package com.example.ui.screens

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import com.example.ui.components.WordByWordColorizer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.mushaf.IndoPakMushafData
import com.example.data.TafseerDataStore
import com.example.data.TafseerTranslationEngine
import com.example.data.AyahTafseerDetails
import com.example.data.WordTafseerInfo
import com.example.data.TafseerBookmarkManager
import com.example.data.TafseerBookmark
import com.example.data.TafseerSettingsManager
import com.example.data.db.AppDatabase
import com.example.data.db.AyahEntity
import com.example.data.db.BookmarkEntity
import com.example.data.api.QuranApi
import com.example.ui.QuranViewModel
import com.example.ui.theme.HandmadeBrushesFontFamily
import com.example.ui.theme.IndoPakFontFamily
import com.example.ui.theme.UrduFontFamily
import com.example.ui.theme.UthmaniFontFamily
import com.example.ui.theme.getQuranFontFamily
import com.example.ui.components.AyahTafseerVerticalItem
import com.example.ui.components.BismillahHeader
import com.example.util.QuranSanitizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

// List of 28 Medinan Surahs (all other 86 are Meccan)
private val MedinanSurahNumbers = setOf(
    2, 3, 4, 5, 8, 9, 22, 24, 33, 47, 48, 49, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 76, 98, 110
)

data class TafseerBook(
    val id: String,
    val name: String,
    val urduName: String,
    val subtitle: String,
    val isDarkCard: Boolean
)

val TafseerBookList = listOf(
    TafseerBook(
        id = "jawahir",
        name = "Tafseer Jawahir-ul-Quran",
        urduName = "تفسیر جواہر القرآن",
        subtitle = "Urdu commentary by Allama Ghulam Ullah Khan",
        isDarkCard = true
    ),
    TafseerBook(
        id = "mazhari",
        name = "Tafseer Mazhari",
        urduName = "تفسیر مظہری",
        subtitle = "Urdu commentary by Allama Qazi Thanaullah Panipati",
        isDarkCard = true
    ),
    TafseerBook(
        id = "usmani",
        name = "Tafseer Usmani",
        urduName = "تفسیر عثمانی",
        subtitle = "Urdu commentary by Mufti Muhammad Shafi",
        isDarkCard = true
    ),
    TafseerBook(
        id = "ibn_kaseer",
        name = "Tafseer Ibn Kaseer",
        urduName = "تفسیر ابن کثیر",
        subtitle = "Classical Arabic commentary, widely trusted",
        isDarkCard = true
    ),
    TafseerBook(
        id = "jalalayn",
        name = "Tafseer Al-Jalalayn",
        urduName = "تفسیر الجلالین",
        subtitle = "Concise Sunni commentary",
        isDarkCard = true
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TafseerScreen(
    viewModel: QuranViewModel? = null,
    onNavigateToSettings: () -> Unit = {},
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val tafseerPrefs = remember { context.getSharedPreferences("tafseer_reading_prefs", Context.MODE_PRIVATE) }
    val coroutineScope = rememberCoroutineScope()

    // Mode state: 0 = Select Tafseer (Commentary Selection), 1 = Surah Index for Selected Tafseer, 2 = Tafseer Reader
    var currentMode by remember { mutableIntStateOf(0) }
    var selectedTafseer by remember { mutableStateOf(TafseerBookList[0]) }
    var selectedSurahNumber by remember { mutableIntStateOf(1) }
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("ALL") } // ALL, MECCAN, MEDINAN, BOOKMARKS
    val surahListState = rememberLazyListState()

    // When entering Surah Index, ensure it is positioned directly at the selected Surah (e.g. Surah Yunus)
    LaunchedEffect(currentMode, selectedSurahNumber) {
        if (currentMode == 1 && selectedSurahNumber in 1..114) {
            val targetIdx = (selectedSurahNumber - 1).coerceIn(0, 113)
            surahListState.scrollToItem(targetIdx)
        }
    }
    
    // Reactive Bookmarks for the selected Tafseer (Independent per Tafseer)
    val currentTafseerBookmarks by TafseerBookmarkManager.getBookmarksFlow(context, selectedTafseer.id).collectAsStateWithLifecycle(initialValue = emptyList())
    val bookmarkedSurahs by remember(currentTafseerBookmarks) {
        derivedStateOf {
            currentTafseerBookmarks.filter { it.ayahNumberInSurah == 0 }.map { it.surahNumber }.toSet()
        }
    }

    var showSearchSheet by remember { mutableStateOf(false) }
    var showReaderSettingsSheet by remember { mutableStateOf(false) }
    var showGoToAyahSheet by remember { mutableStateOf(false) }
    var showBookmarksSheet by remember { mutableStateOf(false) }
    var initialAyahForReader by remember { mutableIntStateOf(1) }

    fun toggleSurahBookmark(surahNum: Int) {
        val surahArabic = IndoPakMushafData.SURAH_NAMES_ARABIC.getOrElse(surahNum - 1) { "" }
        val surahEng = IndoPakMushafData.SURAH_NAMES_ENGLISH.getOrElse(surahNum - 1) { "Surah $surahNum" }
        val isNowBookmarked = TafseerBookmarkManager.toggleSurahBookmark(
            context = context,
            tafseerId = selectedTafseer.id,
            surahNumber = surahNum,
            surahName = if (surahArabic.isNotBlank()) "سورة $surahArabic" else surahEng,
            urduSubtitle = "تفسیر: ${selectedTafseer.urduName} • $surahEng"
        )
        if (isNowBookmarked) {
            Toast.makeText(context, "سورة بک مارک ہو گئی! (${selectedTafseer.urduName})", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "سورة کا بک مارک ہٹا دیا گیا", Toast.LENGTH_SHORT).show()
        }
    }

    val handleResumeClick: () -> Unit = {
        val lastTafseerId = tafseerPrefs.getString("last_read_tafseer_id", selectedTafseer.id) ?: selectedTafseer.id
        val lastSurah = tafseerPrefs.getInt("last_read_tafseer_surah", 1).coerceIn(1, 114)
        val lastAyah = tafseerPrefs.getInt("last_read_tafseer_ayah", 1).coerceAtLeast(1)

        val matchedTafseer = TafseerBookList.find { it.id == lastTafseerId } ?: TafseerBookList[0]
        selectedTafseer = matchedTafseer
        selectedSurahNumber = lastSurah
        initialAyahForReader = lastAyah
        currentMode = 2

        val surahName = IndoPakMushafData.SURAH_NAMES_ARABIC.getOrElse(lastSurah - 1) { "سورة $lastSurah" }
        Toast.makeText(context, "$surahName (آیت $lastAyah) پر مطالعہ جاری ہے", Toast.LENGTH_SHORT).show()
    }

    val allSurahs = remember {
        IndoPakMushafData.SURAH_NAMES_ARABIC.mapIndexed { index, rawName ->
            val num = index + 1
            val isMedinan = MedinanSurahNumbers.contains(num)
            SurahTafseerItem(
                number = num,
                rawArabicName = rawName,
                arabicName = rawName,
                englishName = IndoPakMushafData.SURAH_NAMES_ENGLISH.getOrElse(index) { "Surah $num" },
                revelationType = if (isMedinan) "Medinan" else "Meccan",
                ayahCount = IndoPakMushafData.SURAH_AYAH_COUNTS.getOrElse(index) { 7 }
            )
        }
    }

    val filteredSurahs = remember(searchQuery, filterType, bookmarkedSurahs) {
        allSurahs.filter { item ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                item.arabicName.contains(searchQuery, ignoreCase = true) ||
                item.englishName.contains(searchQuery, ignoreCase = true) ||
                item.number.toString() == searchQuery.trim()
            }
            val matchesFilter = when (filterType) {
                "MECCAN" -> item.revelationType == "Meccan"
                "MEDINAN" -> item.revelationType == "Medinan"
                "BOOKMARKS" -> bookmarkedSurahs.contains(item.number)
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val topBarBgColor = MaterialTheme.colorScheme.background
    val topBarContentColor = if (isDark) Color(0xFF6EE7B7) else Color(0xFF046A38)
    val topBarBorderColor = if (isDark) Color(0xFF1E3A2E) else Color(0xFFB8E6D2)

    BackHandler {
        when (currentMode) {
            2 -> currentMode = 1
            1 -> currentMode = 0
            else -> onBackClick()
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = when (currentMode) {
                    2 -> if (isDark) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary
                    0 -> MaterialTheme.colorScheme.background
                    else -> topBarBgColor
                },
                shadowElevation = if (currentMode == 2) 4.dp else 0.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .height(64.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                when (currentMode) {
                                    2 -> currentMode = 1
                                    1 -> currentMode = 0
                                    else -> onBackClick()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = when (currentMode) {
                                    2 -> if (isDark) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary
                                    0 -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            when (currentMode) {
                                0, 1 -> {
                                    Text(
                                        text = "Al-Quran Majeed",
                                        fontFamily = HandmadeBrushesFontFamily,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                2 -> {
                                    Text(
                                        text = selectedTafseer.name,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        // Right quick-action button
                        if (currentMode == 2) {
                            IconButton(
                                onClick = {
                                    showReaderSettingsSheet = true
                                },
                                modifier = Modifier.testTag("tafseer_settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        } else if (currentMode == 1) {
                            IconButton(
                                onClick = {
                                    showReaderSettingsSheet = true
                                },
                                modifier = Modifier.testTag("tafseer_settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(48.dp))
                        }
                    }

                    if (currentMode == 1) {
                        HorizontalDivider(
                            thickness = 1.5.dp,
                            color = topBarBorderColor
                        )
                    }
                }
            }
        },
        containerColor = when (currentMode) {
            1 -> MaterialTheme.colorScheme.surface
            0 -> if (isDark) MaterialTheme.colorScheme.background else Color(0xFFFAF8F5)
            else -> MaterialTheme.colorScheme.background
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = currentMode,
                label = "TafseerModeTransition"
            ) { mode ->
                when (mode) {
                    0 -> TafseerSelectionView(
                        onSelectTafseer = { tafseer ->
                            selectedTafseer = tafseer
                            currentMode = 1
                        }
                    )
                    1 -> {
                        val tafseerPrefs = remember(selectedTafseer.id) { TafseerSettingsManager.getPrefs(context, selectedTafseer.id) }
                        val currentScriptStyle = remember(selectedTafseer.id) { tafseerPrefs.getString(TafseerSettingsManager.KEY_SCRIPT_STYLE, "UTHMANI") ?: "UTHMANI" }
                        TafseerSurahIndexView(
                            selectedTafseer = selectedTafseer,
                            surahs = filteredSurahs,
                            bookmarkedSurahNumbers = bookmarkedSurahs,
                            searchQuery = searchQuery,
                            onSearchChange = { searchQuery = it },
                            filterType = filterType,
                            onFilterChange = { filterType = it },
                            onSurahSelect = { surahNum ->
                                selectedSurahNumber = surahNum
                                initialAyahForReader = 1
                                currentMode = 2
                            },
                            onSearchClick = { showSearchSheet = !showSearchSheet },
                            showSearchBox = showSearchSheet,
                            onGoToAyahClick = { showGoToAyahSheet = true },
                            onResumeClick = handleResumeClick,
                            onBookmarksClick = { showBookmarksSheet = true },
                            scriptStyle = currentScriptStyle,
                            surahListState = surahListState
                        )
                    }
                    2 -> TafseerReaderView(
                        selectedTafseer = selectedTafseer,
                        surahNumber = selectedSurahNumber,
                        initialAyahNumber = initialAyahForReader,
                        isBookmarked = bookmarkedSurahs.contains(selectedSurahNumber),
                        onToggleBookmark = {
                            toggleSurahBookmark(selectedSurahNumber)
                        },
                        onPreviousSurah = {
                            if (selectedSurahNumber > 1) selectedSurahNumber--
                        },
                        onNextSurah = {
                            if (selectedSurahNumber < 114) selectedSurahNumber++
                        },
                        showSettingsSheet = showReaderSettingsSheet,
                        onShowSettingsSheetChange = { showReaderSettingsSheet = it }
                    )
                }
            }
        }

        if (showGoToAyahSheet) {
            var inputSurah by remember { mutableStateOf("") }
            var inputAyah by remember { mutableStateOf("") }
            val context = LocalContext.current
            
            ModalBottomSheet(
                onDismissRequest = { showGoToAyahSheet = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                    Text(
                        text = "Go To Specific Ayah", 
                        style = MaterialTheme.typography.titleLarge, 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = inputSurah,
                        onValueChange = { inputSurah = it },
                        label = { Text("Surah Number (1-114)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputAyah,
                        onValueChange = { inputAyah = it },
                        label = { Text("Ayah Number") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            val sNum = inputSurah.toIntOrNull()
                            val aNum = inputAyah.toIntOrNull()
                            if (sNum != null && sNum in 1..114 && aNum != null && aNum > 0) {
                                selectedSurahNumber = sNum
                                initialAyahForReader = aNum
                                currentMode = 2
                                showGoToAyahSheet = false
                            } else {
                                Toast.makeText(context, "Invalid Surah or Ayah Number", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Navigate to Ayah", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        if (showBookmarksSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBookmarksSheet = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp).fillMaxWidth().fillMaxHeight(0.8f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "محفوظ شدہ بک مارکس (${selectedTafseer.urduName})", 
                            style = MaterialTheme.typography.titleLarge, 
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = Color(0xFFFFD700)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    if (currentTafseerBookmarks.isEmpty()) {
                        Text(
                            "آپ نے ${selectedTafseer.urduName} میں ابھی تک کوئی سورت یا آیت بک مارک نہیں کی۔ مطالعہ کے دوران اوپر موجود بُک مارک آئیکن دبا کر بک مارک محفوظ کریں۔", 
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 22.sp
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(currentTafseerBookmarks, key = { it.id }) { bm ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.clickable {
                                        selectedSurahNumber = bm.surahNumber
                                        initialAyahForReader = bm.ayahNumberInSurah.coerceAtLeast(1)
                                        currentMode = 2
                                        showBookmarksSheet = false
                                    }
                                ) {
                                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (bm.ayahNumberInSurah == 0) {
                                                    "${bm.surahName} • مکمل سورت (سورة ${bm.surahNumber})"
                                                } else {
                                                    "${bm.surahName} • آیت ${bm.ayahNumberInSurah}"
                                                },
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            IconButton(
                                                onClick = {
                                                    TafseerBookmarkManager.deleteBookmark(context, selectedTafseer.id, bm.id)
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteOutline,
                                                    contentDescription = "Delete Bookmark",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        if (bm.urduTranslation.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = bm.urduTranslation,
                                                fontFamily = UrduFontFamily,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 2,
                                                textAlign = TextAlign.End,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        if (showReaderSettingsSheet) {
            TafseerSettingsSheet(
                tafseer = selectedTafseer,
                onDismissRequest = { showReaderSettingsSheet = false }
            )
        }
    }
}

/**
 * Main Selection View for choosing a Tafseer commentary.
 */
@Composable
private fun TafseerSelectionView(
    onSelectTafseer: (TafseerBook) -> Unit
) {
    val bgVal = MaterialTheme.colorScheme.background
    val isDark = (bgVal.red * 0.299f + bgVal.green * 0.587f + bgVal.blue * 0.114f) < 0.5f

    val headerBgColor = if (isDark) Color(0xFF0F3A2A) else Color(0xFF0A3324)
    val headerBorderColor = if (isDark) Color(0xFF1E523A) else Color(0xFF13523B)
    val cardBgColor = if (isDark) Color(0xFF1E201C) else Color.White
    val cardBorderColor = if (isDark) Color(0xFF2E3D35) else Color(0xFFD0E8DC)
    val badgeBgColor = if (isDark) Color(0xFF1B382C) else Color(0xFFE8F5EE)
    val primaryGreen = if (isDark) Color(0xFF6EE7B7) else Color(0xFF0D6847)
    val titleColor = if (isDark) Color.White else Color(0xFF0D6847)
    val goldYellow = if (isDark) Color(0xFFFBBF24) else Color(0xFFC59B27)

    val bgColor = if (isDark) MaterialTheme.colorScheme.background else Color(0xFFFAF8F5)

    val scrollState = rememberScrollState()
    LaunchedEffect(Unit) {
        scrollState.scrollTo(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(horizontal = 14.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Grand Calligraphic Header Section (matching home screen style)
        val tafseerHeaderBg = if (isDark) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary
        val tafseerHeaderTitle = if (isDark) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary
        val tafseerHeaderAccent = MaterialTheme.colorScheme.secondary

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            shape = RoundedCornerShape(20.dp),
            color = tafseerHeaderBg,
            border = BorderStroke(1.5.dp, tafseerHeaderAccent),
            shadowElevation = 4.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Decorative Background Watermark Icon
                Icon(
                    imageVector = Icons.Default.AutoStories,
                    contentDescription = null,
                    tint = tafseerHeaderTitle.copy(alpha = 0.08f),
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
                        text = "Tafseer Al-Quran",
                        fontFamily = UthmaniFontFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = tafseerHeaderTitle,
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
                                .background(tafseerHeaderAccent)
                        )
                        Text(
                            text = "✦",
                            fontSize = 12.sp,
                            color = tafseerHeaderAccent
                        )
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(1.5.dp)
                                .background(tafseerHeaderAccent)
                        )
                    }

                    Text(
                        text = "Authentic Commentaries (مستند تفاسیر)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = tafseerHeaderAccent,
                            fontSize = 13.sp,
                            fontFamily = UrduFontFamily
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Text(
            text = "Explore the depth of divine guidance",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TafseerBookList.forEach { tafseer ->
                val cardBg = if (isDark) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary
                val cardBorder = if (isDark) MaterialTheme.colorScheme.outlineVariant else tafseerHeaderAccent
                val iconCircleBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.Black.copy(alpha = 0.15f)
                val iconTint = if (isDark) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                val mainTextColor = if (isDark) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary
                val subTextColor = if (isDark) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectTafseer(tafseer) },
                    shape = RoundedCornerShape(16.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, cardBorder),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 96.dp)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Circular Icon
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(iconCircleBg)
                                .border(1.dp, iconTint.copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))

                        // Center Texts
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = tafseer.name,
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = mainTextColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tafseer.urduName,
                                fontFamily = UrduFontFamily,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = mainTextColor,
                                textAlign = TextAlign.Start
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = tafseer.subtitle,
                                fontSize = 12.sp,
                                color = subTextColor,
                                lineHeight = 16.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))

                        // Right Arrow
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Open",
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
    }
}



@Composable
private fun QuickActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    val isDark = MaterialTheme.colorScheme.background.red * 0.299f +
                 MaterialTheme.colorScheme.background.green * 0.587f +
                 MaterialTheme.colorScheme.background.blue * 0.114f < 0.5f

    val circleBg = if (isDark) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary
    val circleBorder = MaterialTheme.colorScheme.secondary
    val iconTint = if (isDark) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary
    val labelColor = MaterialTheme.colorScheme.onSurface

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = circleBg,
            border = BorderStroke(1.dp, circleBorder),
            shadowElevation = 3.dp,
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = labelColor,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * Surah Selection list for Tafseer Study matching the provided HTML/Tailwind Dark Design.
 */
@Composable
private fun TafseerSurahIndexView(
    selectedTafseer: TafseerBook = TafseerBookList[0],
    surahs: List<SurahTafseerItem>,
    bookmarkedSurahNumbers: Set<Int> = emptySet(),
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    filterType: String,
    onFilterChange: (String) -> Unit,
    onSurahSelect: (Int) -> Unit,
    onSearchClick: () -> Unit,
    showSearchBox: Boolean,
    onGoToAyahClick: () -> Unit,
    onResumeClick: () -> Unit,
    onBookmarksClick: () -> Unit,
    scriptStyle: String = "UTHMANI",
    surahListState: androidx.compose.foundation.lazy.LazyListState = rememberLazyListState()
) {
    val isDark = MaterialTheme.colorScheme.background.red * 0.299f +
                 MaterialTheme.colorScheme.background.green * 0.587f +
                 MaterialTheme.colorScheme.background.blue * 0.114f < 0.5f

    val bannerBg = if (isDark) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary
    val goldColor = if (isDark) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary
    val goldBorder = MaterialTheme.colorScheme.secondary
    val subtitleColor = if (isDark) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Prominent Tafseer Header Banner
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 4.dp),
            shape = RoundedCornerShape(16.dp),
            color = bannerBg,
            border = BorderStroke(1.dp, goldBorder),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    modifier = Modifier.size(38.dp),
                    shape = CircleShape,
                    color = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.Black.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, goldBorder)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = null,
                            tint = if (isDark) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(14.dp)
                                .height(1.5.dp)
                                .background(goldBorder)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = selectedTafseer.name,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.3.sp,
                            color = goldColor,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .width(14.dp)
                                .height(1.5.dp)
                                .background(goldBorder)
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${selectedTafseer.urduName} • ${selectedTafseer.subtitle}",
                        fontFamily = UrduFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = subtitleColor,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }

                Surface(
                    modifier = Modifier.size(38.dp),
                    shape = CircleShape,
                    color = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.Black.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, goldBorder)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = if (isDark) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Quick Action Buttons (4 circular buttons as in the screenshot)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuickActionButton(icon = Icons.Default.Search, label = "SEARCH", onClick = onSearchClick)
            QuickActionButton(icon = Icons.Default.GpsFixed, label = "GO TO AYAH", onClick = onGoToAyahClick)
            QuickActionButton(icon = Icons.Default.PlayCircle, label = "RESUME", onClick = onResumeClick)
            QuickActionButton(icon = Icons.Default.Bookmark, label = "BOOKMARKS", onClick = onBookmarksClick)
        }

        AnimatedVisibility(visible = showSearchBox) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Search Surah by Name or Number...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Surah Grid / List
        LazyColumn(
            state = surahListState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(surahs, key = { it.number }) { item ->
                val isItemBookmarked = bookmarkedSurahNumbers.contains(item.number)
                val cardBg = if (isDark) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface
                val badgeBg = if (isDark) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary
                val badgeTextColor = if (isDark) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary
                val badgeBorder = MaterialTheme.colorScheme.secondary

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSurahSelect(item.number) },
                    shape = RoundedCornerShape(12.dp),
                    color = cardBg,
                    border = BorderStroke(
                        if (isItemBookmarked) 1.5.dp else 1.dp,
                        if (isItemBookmarked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Section: Number Badge
                        Surface(
                            modifier = Modifier.size(52.dp),
                            shape = CircleShape,
                            color = badgeBg,
                            border = BorderStroke(1.dp, badgeBorder)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = item.number.toString(),
                                    color = badgeTextColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Middle Section: English Title & Info
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.englishName,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 24.sp
                                )
                                if (isItemBookmarked) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = "Bookmarked",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${item.revelationType} • ${item.ayahCount} Verses",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Right Section: Arabic Title
                        val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                        Text(
                            text = item.arabicName,
                            fontFamily = getQuranFontFamily(scriptStyle),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

/**
 * Reader view displaying all complete Ayahs of the Surah with their Arabic text,
 * Urdu translation, and Tafseer Ibn Kaseer commentary below each Ayah.
 */
@Composable
private fun TafseerReaderView(
    selectedTafseer: TafseerBook = TafseerBookList[0],
    surahNumber: Int,
    initialAyahNumber: Int = 1,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onPreviousSurah: () -> Unit,
    onNextSurah: () -> Unit,
    showSettingsSheet: Boolean = false,
    onShowSettingsSheetChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val tafseerPrefs = remember(selectedTafseer.id) { TafseerSettingsManager.getPrefs(context, selectedTafseer.id) }
    var fontSizeSp by remember(selectedTafseer.id) {
        mutableFloatStateOf(tafseerPrefs.getFloat(TafseerSettingsManager.KEY_FONT_SIZE, 28f))
    }
    var lineHeightMultiplier by remember(selectedTafseer.id) {
        mutableFloatStateOf(tafseerPrefs.getFloat(TafseerSettingsManager.KEY_LINE_SPACING, 1.2f))
    }
    
    // Settings State with Persistence per Tafseer
    var selectedLanguage by remember(selectedTafseer.id) {
        mutableStateOf(tafseerPrefs.getString(TafseerSettingsManager.KEY_TRANSLATION_LANG, "URDU") ?: "URDU")
    }
    var isDarkMode by remember(selectedTafseer.id) {
        mutableStateOf(tafseerPrefs.getBoolean(TafseerSettingsManager.KEY_DARK_MODE, false))
    }
    val wordByWordEnabled = false
    var scriptStyle by remember(selectedTafseer.id) {
        mutableStateOf(tafseerPrefs.getString(TafseerSettingsManager.KEY_SCRIPT_STYLE, "UTHMANI") ?: "UTHMANI")
    }

    val prefListener = remember(selectedTafseer.id) {
        SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            when (key) {
                TafseerSettingsManager.KEY_FONT_SIZE -> fontSizeSp = prefs.getFloat(key, 28f)
                TafseerSettingsManager.KEY_LINE_SPACING -> lineHeightMultiplier = prefs.getFloat(key, 1.2f)
                TafseerSettingsManager.KEY_TRANSLATION_LANG -> selectedLanguage = prefs.getString(key, "URDU") ?: "URDU"
                TafseerSettingsManager.KEY_DARK_MODE -> isDarkMode = prefs.getBoolean(key, false)
                TafseerSettingsManager.KEY_SCRIPT_STYLE -> scriptStyle = prefs.getString(key, "UTHMANI") ?: "UTHMANI"
            }
        }
    }
    DisposableEffect(selectedTafseer.id, tafseerPrefs) {
        tafseerPrefs.registerOnSharedPreferenceChangeListener(prefListener)
        onDispose {
            tafseerPrefs.unregisterOnSharedPreferenceChangeListener(prefListener)
        }
    }

    // Default to "ALL" (Mukammal Surat / Full Surah continuous view)
    var viewMode by remember { mutableStateOf("ALL") }
    var selectedAyahNumber by remember { mutableIntStateOf(initialAyahNumber) }
    var showFindAyahDialog by remember { mutableStateOf(false) }

    // If reading a single Ayah, Back press returns to Full Surah view for this Surah
    BackHandler(enabled = (viewMode == "SINGLE")) {
        viewMode = "ALL"
    }
    
    val db = remember { AppDatabase.getDatabase(context) }
    val tafseerBookmarks by TafseerBookmarkManager.getBookmarksFlow(context, selectedTafseer.id).collectAsStateWithLifecycle(initialValue = emptyList())
    var ayahToNote by remember { mutableStateOf<Int?>(null) }
    var currentNoteText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    var apiTafseerTexts by remember(surahNumber, selectedTafseer.id) { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var isLoadingApiTafseer by remember(surahNumber, selectedTafseer.id) { mutableStateOf(false) }

    LaunchedEffect(surahNumber, selectedTafseer.id) {
        isLoadingApiTafseer = true
        try {
            val fetched = com.example.data.api.TafseerFetcher.fetchChapterTafseer(surahNumber, selectedTafseer.id)
            if (fetched.isNotEmpty()) {
                apiTafseerTexts = fetched
            } else {
                apiTafseerTexts = emptyMap()
            }
        } catch (e: Exception) {
            android.util.Log.e("TafseerScreen", "Error fetching api tafseer", e)
        } finally {
            isLoadingApiTafseer = false
        }
    }

    LaunchedEffect(surahNumber, initialAyahNumber) {
        if (initialAyahNumber > 1) {
            selectedAyahNumber = initialAyahNumber
            viewMode = "SINGLE"
        }
    }

    val surahName = IndoPakMushafData.SURAH_NAMES_ARABIC.getOrElse(surahNumber - 1) { "" }
    val englishName = IndoPakMushafData.SURAH_NAMES_ENGLISH.getOrElse(surahNumber - 1) { "" }
    val totalAyahs = IndoPakMushafData.SURAH_AYAH_COUNTS.getOrElse(surahNumber - 1) { 7 }
    val isMedinan = MedinanSurahNumbers.contains(surahNumber)
    val revelationType = if (isMedinan) "مدنیہ" else "مکیہ"

    val keyThemes = getKeyThemesForSurah(surahNumber)

    var liveAyahs by remember(surahNumber) { mutableStateOf<List<AyahEntity>>(emptyList()) }
    var isLoadingAyahs by remember(surahNumber) { mutableStateOf(true) }

    LaunchedEffect(surahNumber) {
        isLoadingAyahs = true
        try {
            // 1. Try local cache from Room
            val cached = withContext(Dispatchers.IO) {
                db.ayahDao().getAyahsForSurahSync(surahNumber)
            }
            if (cached.isNotEmpty() && cached.none { it.arabicText.startsWith("آيَةُ") || it.urduText.contains("کا مستند اردو ترجمہ") }) {
                liveAyahs = cached
                isLoadingAyahs = false
            } else {
                val offline = withContext(Dispatchers.IO) {
                    com.example.data.mushaf.OfflineQuranDataProvider.getOfflineAyahsForSurah(surahNumber, context)
                }
                if (offline.isNotEmpty()) {
                    withContext(Dispatchers.IO) {
                        db.ayahDao().insertAyahs(offline)
                    }
                    liveAyahs = offline
                    isLoadingAyahs = false
                }
            }

            // 2. Fetch authentic editions from API if online
            val apiEditions = withContext(Dispatchers.IO) {
                try {
                    QuranApi.service.getSurahWithTranslation(surahNumber).data
                } catch (e: Exception) {
                    emptyList()
                }
            }
            if (apiEditions.size >= 2) {
                val arabicEdition = apiEditions.find { it.edition?.identifier == "quran-uthmani" }
                val urduEdition = apiEditions.find { it.edition?.identifier == "ur.jalandhry" }
                if (arabicEdition != null && urduEdition != null) {
                    val entities = arabicEdition.ayahs.mapIndexed { idx, arAyah ->
                        val cleanAr = QuranSanitizer.cleanAyahArabic(arAyah.text, surahNumber, arAyah.numberInSurah)
                        val cleanUr = QuranSanitizer.cleanAyahUrdu(urduEdition.ayahs.getOrNull(idx)?.text ?: "", surahNumber, arAyah.numberInSurah)
                        AyahEntity(
                            surahNumber = surahNumber,
                            numberInSurah = arAyah.numberInSurah,
                            overallNumber = arAyah.number,
                            arabicText = cleanAr,
                            urduText = cleanUr
                        )
                    }
                    withContext(Dispatchers.IO) {
                        db.ayahDao().insertAyahs(entities)
                    }
                    liveAyahs = entities
                }
            } else if (liveAyahs.isEmpty() || liveAyahs.any { it.arabicText.startsWith("آيَةُ") }) {
                val offline = withContext(Dispatchers.IO) {
                    com.example.data.mushaf.OfflineQuranDataProvider.getOfflineAyahsForSurah(surahNumber, context)
                }
                liveAyahs = offline
            }
        } catch (e: Exception) {
            if (liveAyahs.isEmpty() || liveAyahs.any { it.arabicText.startsWith("آيَةُ") }) {
                liveAyahs = com.example.data.mushaf.OfflineQuranDataProvider.getOfflineAyahsForSurah(surahNumber, context)
            }
        } finally {
            isLoadingAyahs = false
        }
    }

    // Fast O(1) indexed lookup for live ayahs
    val liveAyahsMap = remember(liveAyahs) {
        liveAyahs.associateBy { it.numberInSurah }
    }

    // Lazy memory cache for ayah details so we only compute visible ayahs when scrolled into view
    val ayahDetailsCache = remember(surahNumber, liveAyahsMap, selectedTafseer.id, selectedLanguage, apiTafseerTexts, isLoadingApiTafseer) {
        mutableMapOf<Int, AyahTafseerDetails>()
    }

    fun getOrComputeAyahDetails(aNum: Int): AyahTafseerDetails {
        return ayahDetailsCache.getOrPut(aNum) {
            var baseTafseer = getAyahTafseerDetails(surahNumber, aNum, selectedTafseer.id, selectedLanguage)
            
            // Check if we have fetched API text for this Ayah
            val apiText = apiTafseerTexts[aNum]
            if (apiText != null && !isLoadingApiTafseer) {
                if (selectedTafseer.id == "jalalayn" && selectedLanguage == "ARABIC") {
                    // For Jalalayn in Arabic, the API text is Arabic commentary, so put it in the Tafseer section
                    val paragraphs = apiText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                    baseTafseer = baseTafseer.copy(
                        tafseerText = apiText,
                        tafseerParagraphs = if (paragraphs.isNotEmpty()) paragraphs else listOf(apiText)
                    )
                } else if (selectedLanguage == "URDU") {
                    // For Urdu mode, if API text exists for Urdu, populate urduTranslation
                    baseTafseer = baseTafseer.copy(
                        urduTranslation = apiText
                    )
                }
            }

            val matchingLiveAyah = liveAyahsMap[aNum]
            if (matchingLiveAyah != null && matchingLiveAyah.arabicText.isNotBlank() && !matchingLiveAyah.arabicText.startsWith("آيَةُ")) {
                val resolvedArabic = QuranSanitizer.cleanAyahArabic(matchingLiveAyah.arabicText, surahNumber, aNum)
                val authenticUrdu = if (matchingLiveAyah.urduText.isNotBlank() && !matchingLiveAyah.urduText.startsWith("ترجمہ") && !matchingLiveAyah.urduText.startsWith("اللہ کے نام سے جو رحمن و رحیم ہے۔ (آیت")) {
                    QuranSanitizer.cleanAyahUrdu(matchingLiveAyah.urduText, surahNumber, aNum)
                } else QuranSanitizer.cleanAyahUrdu(baseTafseer.urduTranslation, surahNumber, aNum)

                val authenticHindi = if (selectedLanguage == "HINDI" && baseTafseer.hindiTranslation.isBlank()) {
                    TafseerTranslationEngine.getHindiTranslation(surahNumber, aNum, authenticUrdu)
                } else baseTafseer.hindiTranslation

                val dynamicWords = WordByWordColorizer.getWordsForAyah(
                    arabicText = resolvedArabic,
                    urduTranslation = authenticUrdu,
                    englishTranslation = baseTafseer.englishTranslation,
                    language = selectedLanguage
                )

                baseTafseer.copy(
                    arabicText = resolvedArabic,
                    urduTranslation = authenticUrdu,
                    hindiTranslation = authenticHindi,
                    words = dynamicWords
                )
            } else {
                val cleanedAr = QuranSanitizer.cleanAyahArabic(baseTafseer.arabicText, surahNumber, aNum)
                val cleanedUr = QuranSanitizer.cleanAyahUrdu(baseTafseer.urduTranslation, surahNumber, aNum)
                val dynamicWords = WordByWordColorizer.getWordsForAyah(
                    arabicText = cleanedAr,
                    urduTranslation = cleanedUr,
                    englishTranslation = baseTafseer.englishTranslation,
                    language = selectedLanguage
                )
                baseTafseer.copy(
                    arabicText = cleanedAr,
                    urduTranslation = cleanedUr,
                    words = dynamicWords
                )
            }
        }
    }

    val readerListState = rememberLazyListState()
    LaunchedEffect(surahNumber, initialAyahNumber) {
        if (initialAyahNumber > 1) {
            readerListState.scrollToItem(initialAyahNumber)
        } else {
            readerListState.scrollToItem(0, 0)
        }
    }

    val visibleAyah = remember {
        derivedStateOf {
            if (viewMode == "SINGLE") {
                selectedAyahNumber
            } else {
                val firstVisible = readerListState.firstVisibleItemIndex
                if (firstVisible >= 3) {
                    (firstVisible - 2).coerceIn(1, totalAyahs)
                } else {
                    1
                }
            }
        }
    }

    LaunchedEffect(surahNumber, selectedTafseer.id, visibleAyah.value) {
        tafseerPrefs.edit()
            .putString("last_read_tafseer_id", selectedTafseer.id)
            .putInt("last_read_tafseer_surah", surahNumber)
            .putInt("last_read_tafseer_ayah", visibleAyah.value)
            .apply()
    }

    LazyColumn(
        state = readerListState,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ==========================================
        // 1. SURAH HERO HEADER BANNER
        // ==========================================
        item(key = "surah_hero_banner", contentType = "hero_banner") {
            val isDarkTheme = MaterialTheme.colorScheme.background.red * 0.299f +
                             MaterialTheme.colorScheme.background.green * 0.587f +
                             MaterialTheme.colorScheme.background.blue * 0.114f < 0.5f

            val heroBg = if (isDarkTheme) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary
            val heroTextColor = if (isDarkTheme) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary
            val heroAccent = MaterialTheme.colorScheme.secondary
            val heroBorder = MaterialTheme.colorScheme.secondary

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = heroBg,
                shadowElevation = 4.dp,
                border = BorderStroke(1.dp, heroBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else Color.Black.copy(alpha = 0.15f),
                            border = BorderStroke(1.2.dp, heroBorder)
                        ) {
                            Text(
                                text = "سورت نمبر $surahNumber • $revelationType",
                                fontFamily = UrduFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = heroAccent,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }

                        // Bookmark Surah Button
                        IconButton(
                            onClick = onToggleBookmark,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else Color.Black.copy(alpha = 0.15f))
                                .border(BorderStroke(1.2.dp, heroBorder), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark Surah",
                                tint = heroAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "سُورَةُ $surahName",
                        fontFamily = getQuranFontFamily(scriptStyle),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = heroTextColor,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "SURAH ${englishName.uppercase()} • $totalAyahs VERSES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = heroAccent
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mode Toggle: Ayah by Ayah (آیت بہ آیت) vs Full Surah (مکمل سورت) + Quick Find Ayah
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else Color.Black.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, heroBorder.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Localized Labels & Fonts based on Selected Language
                            val fullSurahLabel = when (selectedLanguage) {
                                "ENGLISH" -> "Complete Surah"
                                "ARABIC" -> "السورة كاملة"
                                "HINDI" -> "पूरी सूरह"
                                else -> "مکمل سورت"
                            }
                            val singleAyahLabel = when (selectedLanguage) {
                                "ENGLISH" -> "Verse by Verse"
                                "ARABIC" -> "آية بآية"
                                "HINDI" -> "आयत दर आयत"
                                else -> "آیت بہ آیت"
                            }
                            val toggleFontFamily = when (selectedLanguage) {
                                "ENGLISH", "HINDI" -> FontFamily.Default
                                "ARABIC" -> getQuranFontFamily(scriptStyle)
                                else -> UrduFontFamily
                            }

                            // Option 1: Full Surah (مکمل سورت / Complete Surah / السورة كاملة / पूरी सूरह)
                            val isAllSelected = viewMode == "ALL"
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewMode = "ALL" },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isAllSelected) heroAccent else if (isDarkTheme) MaterialTheme.colorScheme.surface.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.15f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isAllSelected) heroBorder else Color.Transparent
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = if (isAllSelected) MaterialTheme.colorScheme.surface else heroTextColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = fullSurahLabel,
                                        fontFamily = toggleFontFamily,
                                        fontSize = if (selectedLanguage == "ENGLISH" || selectedLanguage == "HINDI") 12.sp else 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAllSelected) MaterialTheme.colorScheme.surface else heroTextColor
                                    )
                                }
                            }

                            // Option 2: Ayah by Ayah (آیت بہ آیت / Verse by Verse / آية بآية / आयत दर आयत)
                            val isSingleSelected = viewMode == "SINGLE"
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewMode = "SINGLE" },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSingleSelected) heroAccent else if (isDarkTheme) MaterialTheme.colorScheme.surface.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.15f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSingleSelected) heroBorder else Color.Transparent
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ViewList,
                                        contentDescription = null,
                                        tint = if (isSingleSelected) MaterialTheme.colorScheme.surface else heroTextColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = singleAyahLabel,
                                        fontFamily = toggleFontFamily,
                                        fontSize = if (selectedLanguage == "ENGLISH" || selectedLanguage == "HINDI") 12.sp else 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSingleSelected) MaterialTheme.colorScheme.surface else heroTextColor
                                    )
                                }
                            }

                            // Option 3: Quick "Find Ayah" Search Icon Button
                            Surface(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { showFindAyahDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDarkTheme) MaterialTheme.colorScheme.surface.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, heroBorder.copy(alpha = 0.7f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Find Ayah (آیت تلاش کریں)",
                                        tint = heroAccent,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. DYNAMIC AYAH QUICK TAB BAR (Shown ONLY in "آیت بہ آیت" / SINGLE Mode)
        // ==========================================
        if (totalAyahs > 1 && viewMode == "SINGLE") {
            item(key = "ayah_picker_row", contentType = "picker_row") {
                val ayahPickerListState = rememberLazyListState()
                LaunchedEffect(selectedAyahNumber) {
                    val targetIndex = (selectedAyahNumber - 1).coerceIn(0, totalAyahs - 1)
                    ayahPickerListState.animateScrollToItem(targetIndex)
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        val isRtlLayout = selectedLanguage == "URDU" || selectedLanguage == "ARABIC"
                        CompositionLocalProvider(LocalLayoutDirection provides (if (isRtlLayout) LayoutDirection.Rtl else LayoutDirection.Ltr)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when (selectedLanguage) {
                                        "ENGLISH" -> "Selected Verse:"
                                        "ARABIC" -> "الآية المحددة:"
                                        "HINDI" -> "चयनित आयत:"
                                        else -> "منتخب آیت (آیت بہ آیت مطالعہ):"
                                    },
                                    fontFamily = when (selectedLanguage) {
                                        "ENGLISH", "HINDI" -> FontFamily.Default
                                        "ARABIC" -> getQuranFontFamily(scriptStyle)
                                        else -> UrduFontFamily
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = when (selectedLanguage) {
                                        "ENGLISH" -> "Verse $selectedAyahNumber of $totalAyahs"
                                        "ARABIC" -> "آية ${toEasternArabicDigits(selectedAyahNumber)} من ${toEasternArabicDigits(totalAyahs)}"
                                        "HINDI" -> "आयत $selectedAyahNumber / $totalAyahs"
                                        else -> "آیت $selectedAyahNumber از $totalAyahs"
                                    },
                                    fontFamily = when (selectedLanguage) {
                                        "ENGLISH", "HINDI" -> FontFamily.Default
                                        "ARABIC" -> getQuranFontFamily(scriptStyle)
                                        else -> UrduFontFamily
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD4AF37)
                                )
                            }

                            LazyRow(
                                state = ayahPickerListState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                items(
                                    count = totalAyahs,
                                    key = { index -> "picker_ayah_${index + 1}" },
                                    contentType = { "picker_chip" }
                                ) { index ->
                                    val aNum = index + 1
                                    val isAyahActive = (aNum == selectedAyahNumber)
                                    val chipText = when (selectedLanguage) {
                                        "ENGLISH" -> "$englishName $aNum"
                                        "ARABIC" -> "$surahName ${toEasternArabicDigits(aNum)}"
                                        "HINDI" -> "$englishName $aNum"
                                        else -> "$surahName $aNum"
                                    }
                                    val chipFont = when (selectedLanguage) {
                                        "ENGLISH", "HINDI" -> FontFamily.Default
                                        "ARABIC" -> getQuranFontFamily(scriptStyle)
                                        else -> UrduFontFamily
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isAyahActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        border = if (isAyahActive) BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary) else BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                                        shadowElevation = if (isAyahActive) 3.dp else 0.dp,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                selectedAyahNumber = aNum
                                            }
                                    ) {
                                        Text(
                                            text = chipText,
                                            fontFamily = chipFont,
                                            fontSize = 13.sp,
                                            fontWeight = if (isAyahActive) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isAyahActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. STANDALONE BISMILLAH BANNER
        // ==========================================
        if (surahNumber != 9 && (viewMode == "ALL" || selectedAyahNumber == 1)) {
            item(key = "bismillah_banner", contentType = "bismillah") {
                BismillahHeader(
                    showUrduTranslation = true,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    borderColor = Color(0xFFD4AF37),
                    textColor = MaterialTheme.colorScheme.primary,
                    scriptStyle = scriptStyle
                )
            }
        }

        // ==========================================
        // 4. AYAH BLOCKS (EACH AYAH LAZILY LOADED ON DEMAND)
        // ==========================================
        if (viewMode == "ALL") {
            items(
                count = totalAyahs,
                key = { index -> "ayah_${surahNumber}_${index + 1}" },
                contentType = { "ayah_block" }
            ) { index ->
                val aNum = index + 1
                val ayahDetails = remember(surahNumber, aNum, liveAyahsMap, selectedLanguage) {
                    getOrComputeAyahDetails(aNum)
                }
                val isAyahBookmarked = remember(tafseerBookmarks, surahNumber, aNum) {
                    tafseerBookmarks.any { it.surahNumber == surahNumber && it.ayahNumberInSurah == aNum }
                }
                AyahTafseerVerticalItem(
                    surahNumber = surahNumber,
                    surahNameArabic = surahName,
                    ayahNumber = aNum,
                    totalAyahs = totalAyahs,
                    ayahDetails = ayahDetails,
                    tafseerName = selectedTafseer.name,
                    tafseerUrduName = selectedTafseer.urduName,
                    tafseerSubtitle = selectedTafseer.subtitle,
                    selectedLanguage = selectedLanguage,
                    fontSizeSp = fontSizeSp,
                    lineHeightMultiplier = lineHeightMultiplier,
                    isDarkMode = isDarkMode,
                    wordByWordEnabled = wordByWordEnabled,
                    scriptStyle = scriptStyle,
                    isBookmarked = isAyahBookmarked,
                    onBookmarkToggle = {
                        val cleanUrdu = QuranSanitizer.cleanAyahUrdu(ayahDetails.urduTranslation, surahNumber, aNum)
                        val isNowBookmarked = TafseerBookmarkManager.toggleAyahBookmark(
                            context = context,
                            tafseerId = selectedTafseer.id,
                            surahNumber = surahNumber,
                            surahName = "$englishName • آیت $aNum",
                            ayahNumber = aNum,
                            urduTranslation = cleanUrdu
                        )
                        if (isNowBookmarked) {
                            Toast.makeText(context, "آیت نمبر $aNum بک مارک ہو گئی! (${selectedTafseer.urduName})", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "آیت نمبر $aNum کا بک مارک ہٹا دیا گیا", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onAddNote = {
                        ayahToNote = aNum
                    }
                )
            }
        } else {
            item(
                key = "ayah_${surahNumber}_$selectedAyahNumber",
                contentType = "ayah_block"
            ) {
                val aNum = selectedAyahNumber
                val ayahDetails = remember(surahNumber, aNum, liveAyahsMap, selectedLanguage) {
                    getOrComputeAyahDetails(aNum)
                }
                val isAyahBookmarked = remember(tafseerBookmarks, surahNumber, aNum) {
                    tafseerBookmarks.any { it.surahNumber == surahNumber && it.ayahNumberInSurah == aNum }
                }
                var totalDragOffset by remember { mutableFloatStateOf(0f) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(selectedAyahNumber, totalAyahs) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (totalDragOffset < -50f) {
                                        // Swipe Left -> Next Ayah
                                        if (selectedAyahNumber < totalAyahs) {
                                            selectedAyahNumber++
                                        }
                                    } else if (totalDragOffset > 50f) {
                                        // Swipe Right -> Previous Ayah
                                        if (selectedAyahNumber > 1) {
                                            selectedAyahNumber--
                                        }
                                    }
                                    totalDragOffset = 0f
                                },
                                onDragCancel = {
                                    totalDragOffset = 0f
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    totalDragOffset += dragAmount
                                }
                            )
                        }
                ) {
                    AyahTafseerVerticalItem(
                        surahNumber = surahNumber,
                        surahNameArabic = surahName,
                        ayahNumber = aNum,
                        totalAyahs = totalAyahs,
                        ayahDetails = ayahDetails,
                        tafseerName = selectedTafseer.name,
                        tafseerUrduName = selectedTafseer.urduName,
                        tafseerSubtitle = selectedTafseer.subtitle,
                        selectedLanguage = selectedLanguage,
                        fontSizeSp = fontSizeSp,
                        lineHeightMultiplier = lineHeightMultiplier,
                        isDarkMode = isDarkMode,
                        wordByWordEnabled = wordByWordEnabled,
                        scriptStyle = scriptStyle,
                        isBookmarked = isAyahBookmarked,
                        onBookmarkToggle = {
                            val cleanUrdu = QuranSanitizer.cleanAyahUrdu(ayahDetails.urduTranslation, surahNumber, aNum)
                            val isNowBookmarked = TafseerBookmarkManager.toggleAyahBookmark(
                                context = context,
                                tafseerId = selectedTafseer.id,
                                surahNumber = surahNumber,
                                surahName = "$englishName • آیت $aNum",
                                ayahNumber = aNum,
                                urduTranslation = cleanUrdu
                            )
                            if (isNowBookmarked) {
                                Toast.makeText(context, "آیت نمبر $aNum بک مارک ہو گئی! (${selectedTafseer.urduName})", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "آیت نمبر $aNum کا بک مارک ہٹا دیا گیا", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onAddNote = {
                            ayahToNote = aNum
                        }
                    )

                    // Bottom Navigation Bar for Ayah-by-Ayah Mode
                    if (totalAyahs > 1) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    if (selectedAyahNumber > 1) {
                                        selectedAyahNumber--
                                    }
                                },
                                enabled = selectedAyahNumber > 1,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0C4D35),
                                    disabledContainerColor = Color(0xFF1F3529)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Previous Ayah",
                                    tint = if (selectedAyahNumber > 1) Color(0xFFFFD700) else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "پچھلی آیت (${selectedAyahNumber - 1})",
                                    fontFamily = UrduFontFamily,
                                    fontSize = 13.sp,
                                    color = if (selectedAyahNumber > 1) Color.White else Color.Gray
                                )
                            }

                            Text(
                                text = "$selectedAyahNumber / $totalAyahs",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD4AF37)
                            )

                            Button(
                                onClick = {
                                    if (selectedAyahNumber < totalAyahs) {
                                        selectedAyahNumber++
                                    }
                                },
                                enabled = selectedAyahNumber < totalAyahs,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0C4D35),
                                    disabledContainerColor = Color(0xFF1F3529)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "اگلی آیت (${selectedAyahNumber + 1})",
                                    fontFamily = UrduFontFamily,
                                    fontSize = 13.sp,
                                    color = if (selectedAyahNumber < totalAyahs) Color.White else Color.Gray
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next Ayah",
                                    tint = if (selectedAyahNumber < totalAyahs) Color(0xFFFFD700) else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 5. SURAH KEY THEMES (Shown on Ayah 1 or Full Surah mode)
        // ==========================================
        if (selectedAyahNumber == 1 || viewMode == "ALL") {
            item(key = "surah_key_themes", contentType = "key_themes") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "مرکزی موضوعات و احکام (Key Themes)",
                        fontFamily = UrduFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    keyThemes.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { theme ->
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp)),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text(
                                            text = theme.title,
                                            fontFamily = UrduFontFamily,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            textAlign = TextAlign.End
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = theme.subtitle,
                                            fontFamily = UrduFontFamily,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Settings Bottom Sheet
    if (showSettingsSheet) {
        TafseerSettingsSheet(
            tafseer = selectedTafseer,
            onDismissRequest = { onShowSettingsSheetChange(false) }
        )
    }

    if (ayahToNote != null) {
        AlertDialog(
            onDismissRequest = { 
                ayahToNote = null 
                currentNoteText = ""
            },
            title = { Text("Add Note (Ayah $ayahToNote)") },
            text = {
                OutlinedTextField(
                    value = currentNoteText,
                    onValueChange = { currentNoteText = it },
                    label = { Text("Your Note") },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    maxLines = 5
                )
            },
            confirmButton = {
                Button(onClick = {
                    coroutineScope.launch {
                        db.noteDao().insertNote(
                            com.example.data.db.NoteEntity(
                                surahNumber = surahNumber,
                                surahName = englishName,
                                ayahNumber = ayahToNote!!,
                                noteText = currentNoteText,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                        Toast.makeText(context, "Note Saved!", Toast.LENGTH_SHORT).show()
                        ayahToNote = null
                        currentNoteText = ""
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    ayahToNote = null 
                    currentNoteText = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showFindAyahDialog) {
        var inputAyahStr by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showFindAyahDialog = false },
            title = {
                Text(
                    text = "آیت تلاش کریں (Find Ayah)",
                    fontFamily = UrduFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "سورة $surahName میں کل $totalAyahs آیات ہیں۔ جس آیت کا مطالعہ کرنا ہو اس کا نمبر درج کریں:",
                        fontFamily = UrduFontFamily,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputAyahStr,
                        onValueChange = { inputAyahStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("آیت نمبر (1 - $totalAyahs)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = inputAyahStr.toIntOrNull()
                        if (parsed != null && parsed in 1..totalAyahs) {
                            selectedAyahNumber = parsed
                            viewMode = "SINGLE"
                            showFindAyahDialog = false
                        } else {
                            Toast.makeText(context, "براہ کرم 1 سے $totalAyahs کے درمیان درست آیت نمبر لکھیں", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("کھولیں", fontFamily = UrduFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFindAyahDialog = false }) {
                    Text("منسوخ", fontFamily = UrduFontFamily)
                }
            }
        )
    }
}

@Composable
private fun MetadataBentoItem(
    modifier: Modifier = Modifier,
    title: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

private data class SurahThemeInfo(
    val title: String,
    val subtitle: String
)

private fun getRukuCount(surahNum: Int): Int {
    return when (surahNum) {
        1 -> 1
        2 -> 40
        3 -> 20
        4 -> 24
        5 -> 16
        6 -> 20
        7 -> 24
        8 -> 10
        9 -> 16
        18 -> 12
        36 -> 5
        55 -> 3
        67 -> 2
        112, 113, 114 -> 1
        else -> (IndoPakMushafData.SURAH_AYAH_COUNTS.getOrElse(surahNum - 1) { 10 } / 15).coerceAtLeast(1)
    }
}

private fun getKeyThemesForSurah(surahNum: Int): List<SurahThemeInfo> {
    return when (surahNum) {
        1 -> listOf(
            SurahThemeInfo("توحید و حمدِ الٰہی", "اللہ کی ربوبیت اور رحمانیت کا اقرار"),
            SurahThemeInfo("عبادت و دعا", "صرف اللہ سے مدد اور استعانت"),
            SurahThemeInfo("صراطِ مستقیم", "انبیاء اور صدیقین کا راستہ"),
            SurahThemeInfo("گمراہی سے پناہ", "مغضوب اور ضالین کی راہ سے بچاؤ")
        )
        2 -> listOf(
            SurahThemeInfo("تقویٰ اور ہدایت", "اہلِ ایمان اور متقین کی صفات"),
            SurahThemeInfo("احکامِ شریعت", "روزہ، حج، زکوٰۃ اور خاندانی قوانین"),
            SurahThemeInfo("توحیدِ خالص", "آیت الکرسی اور اللہ کی قدرت"),
            SurahThemeInfo("انفاق و معیشت", "سود کی ممانعت اور صدقات کی فضیلت")
        )
        3 -> listOf(
            SurahThemeInfo("ثابت قدمی و جہاد", "غزوہ احد اور بدر کے اسباق"),
            SurahThemeInfo("وحدانیت اور رسالت", "اہلِ کتاب سے مناظرہ و مکالمہ"),
            SurahThemeInfo("اتحادِ امت", "حبل اللہ کو مضبوطی سے تھامنا"),
            SurahThemeInfo("دعا و استغفار", "اہلِ ایمان کی عاجزانہ دعائیں")
        )
        18 -> listOf(
            SurahThemeInfo("ایمانی آزمائش", "اصحاب کہف کی ثابت قدمی"),
            SurahThemeInfo("مال کی آزمائش", "دو باغوں کے مالک کا قصہ"),
            SurahThemeInfo("علم کی آزمائش", "حضرت موسیٰ و خضر علیہ السلام"),
            SurahThemeInfo("طاقت کی آزمائش", "ذوالقرنین کا عدل و انصاف")
        )
        36 -> listOf(
            SurahThemeInfo("رسالتِ محمدیؐ", "قرآن حکیم کی سچائی کی گواہی"),
            SurahThemeInfo("بعث بعد الموت", "دوبارہ زندہ کیے جانے کے دلائل"),
            SurahThemeInfo("کائنات میں نشانیاں", "سورج، چاند اور رات دن کا نظام"),
            SurahThemeInfo("قیامت کے مناظر", "اہلِ جنت اور اہل جہنم کا انجام")
        )
        else -> listOf(
            SurahThemeInfo("توحیدِ ربوبیت", "اللہ تعالیٰ کی وحدانیت و قدرت"),
            SurahThemeInfo("اخروی نجات", "تقویٰ اور عملِ صالح کی تلقین"),
            SurahThemeInfo("انسانی ہدایت", "اوامر و نواہی پر عمل کے احکام"),
            SurahThemeInfo("دعوت و نصیحت", "سچے راستے پر ثابت قدمی")
        )
    }
}

private fun getMoralLessonsForSurah(surahNum: Int): List<String> {
    return when (surahNum) {
        1 -> listOf(
            "ہر کام سے پہلے اللہ تعالیٰ کی حمد و ثناء بیان کرنا لازم ہے۔",
            "صرف اللہ کی ذات ہی عبادت اور مدد طلب کرنے کی حقدار ہے۔",
            "ہمہ وقت سیدھی راہ (صراطِ مستقیم) پر چلنے کی دعا مانگنی چاہیے۔"
        )
        2 -> listOf(
            "متقین کا سب سے بڑا وصف غیب پر ایمان اور نماز قائم کرنا ہے۔",
            "سود اور حرام خوری سے بچنا ایمان کی اولین شرط ہے۔",
            "ہر مشکل وقت میں صبر اور نماز سے مدد لینی چاہیے۔"
        )
        18 -> listOf(
            "فتنوں کے دور میں اپنے ایمان کی حفاظت اولین ترجیح ہے۔",
            "علم اور دانائی حاصل کرنے کے لیے عاجزی اور صبر ضروری ہے۔",
            "دنیا کی دولت اور طاقت کو ہمیشہ اللہ کی رضا کے ماتحت رکھنا چاہیے۔"
        )
        else -> listOf(
            "قرآن مجید کے احکامات کو اپنی روزمرہ زندگی کا حصہ بنائیں۔",
            "اللہ کے ذکر اور شکر گزاری کو معمول بنائیں۔",
            "حق کی راہ میں ثابت قدم رہیں اور باطل سے اجتناب کریں۔"
        )
    }
}

private data class SurahTafseerItem(
    val number: Int,
    val rawArabicName: String,
    val arabicName: String,
    val englishName: String,
    val revelationType: String,
    val ayahCount: Int
)

private fun getTafseerIntroduction(surahNum: Int): String {
    return when (surahNum) {
        1 -> "سورة الفاتحة قرآن کریم کی پہلی اور عظمت والی سورت ہے۔ اسے ام الکتاب، السبع المثانی اور الشفاء بھی کہا جاتا ہے۔ ابن کثیر کے مطابق اس سورت میں توحید، عبادات، دعا اور مستقیم راہ کی ہدایت تمام بنیادی مضامین جامع انداز میں موجود ہیں۔"
        2 -> "سورة البقرة قرآن کریم کی طویل ترین مکی/مدنی سورت ہے جس میں احکامِ شریعت، عبادات، معاملات، قصص انبیاء اور آیت الکرسی جیسی عظیم الشان آیات شامل ہیں۔ اس کی تلاوت سے گھر سے شیطان بھاگ جاتا ہے۔"
        3 -> "سورة آل عمران میں توحید و رسالت، بدر و احد کی عبرت ناک نصیحتیں، اور اہل کتاب کے ساتھ مکالمہ و دلائل تفصیل سے بیان کیے گئے ہیں۔"
        4 -> "سورة النساء میں خاندانی نظام، یتامیٰ کے حقوق، وراثت کے احکامات اور اسلامی معاشرت کے رہنما اصول بیان کیے گئے ہیں۔"
        18 -> "سورة الكهف کی تلاوت جمعہ کے دن کرنے سے دو جمعوں کے درمیان نور چمکتا رہتا ہے۔ اس میں اصحاب کہف، صاحب الجنتین، اور حضرت موسیٰ و خضر علیہ السلام کے سچے و عبرت ناک واقعات بیان ہیں۔"
        36 -> "سورة يس قرآن مجید کا دل کہلاتی ہے۔ اس میں توحید، رسالت، بعث بعد الموت اور روزِ قیامت کے دلائل بہت پرتاثیر انداز میں پیش کیے گئے ہیں۔"
        55 -> "سورة الرحمن قرآن مجید کی زینت ہے۔ اس میں اللہ تعالیٰ کی لاتعداد نعمتوں کا ذکر ہے اور بار بار یاد دہانی کرائی گئی ہے: 'فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ'۔"
        67 -> "سورة الملك عذابِ قبر سے نجات دہندہ سورت ہے۔ رسول اللہ صلی اللہ علیہ وسلم ہر رات اس کی تلاوت فرمایا کرتے تھے۔"
        112 -> "سورة الإخلاص توحیدِ باری تعالیٰ کا نچوڑ ہے اور ثلثِ قرآن (ایک تہائی قرآن) کے برابر ثواب رکھتی ہے۔"
        else -> "سورة نمبر $surahNum قرآن حکیم کی مبارک سورت ہے جس میں اللہ تعالیٰ کا پیغام، اوامر و نواہی، ہدایت و رہنمائی اور انسانی فلاح کا مکمل دستورِ حیات موجود ہے۔"
    }
}

private fun getTafseerDetailedText(surahNum: Int): String {
    return when (surahNum) {
        1 -> "بسم اللہ الرحمن الرحیم سے شروع ہونے والی یہ سورت بندے اور پروردگار کے درمیان راز و نیاز کی بہترین دعا ہے۔ حضرت ابن کثیرؒ بیان کرتے ہیں کہ جب بندہ 'الحمد لله رب العالمين' کہتا ہے تو اللہ تعالیٰ فرماتا ہے: 'میرا بندہ میری حمد بیان کر رہا ہے'۔ 'إياك نعبد وإياك نستعين' میں توحیدِ عبادت و توحیدِ استعانت کا عظیم سبق قائم ہے۔"
        2 -> "تفسیر ابن کثیر کے مطابق سورة البقرہ میں شریعتِ اسلامیہ کے تمام بنیادی قواعد و ضوابط، روزے کے احکام، حج و عمرہ، انفاق فی سبیل اللہ، سود کی حرمت اور قرض کے احکامات جامع اور مفصل انداز میں ہدایت فراہم کرتے ہیں۔"
        3 -> "تفسیر ابن کثیر میں سورة آل عمران کی تفسیر میں غزوہ بدر اور غزوہ احد کے اسباق، ثابت قدمی اور اللہ پر توکل کے اہم اصول ذکر کیے گئے ہیں۔"
        18 -> "اصحاب کہف کا واقعہ ایمانی ثابت قدمی کی بہترین مثال ہے۔ ابن کثیر فرماتے ہیں کہ جو شخص دجال کے فتنے سے محفوظ رہنا چاہتا ہے وہ سورة الکہف کی ابتدائی و آخری دس آیات کی تلاوت و فہم کو لازم پکڑے۔"
        else -> "علامہ ابن کثیرؒ نے اپنی مشہور تفسیر میں اس سورت کی ہر آیت کا احادیثِ مبارکہ، آثارِ صحابہؓ اور لغتِ عرب کے ساتھ تفصیلی احاطہ فرمایا ہے۔ یہ سورت انسان کو تقویٰ، توکل اور آخرت کی کامیابی کی راہ دکھاتی ہے۔"
    }
}

private fun getAyahTafseerDetails(
    surahNum: Int,
    ayahNum: Int,
    tafseerId: String = "ibn_kaseer",
    language: String = "URDU"
): AyahTafseerDetails {
    return TafseerDataStore.getAyahDetails(surahNum, ayahNum, tafseerId, language)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TafseerSettingsSheet(
    tafseer: TafseerBook,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val tafseerPrefs = remember(tafseer.id) { TafseerSettingsManager.getPrefs(context, tafseer.id) }

    var fontSizeSp by remember(tafseer.id) {
        mutableFloatStateOf(tafseerPrefs.getFloat(TafseerSettingsManager.KEY_FONT_SIZE, 28f))
    }
    var lineHeightMultiplier by remember(tafseer.id) {
        mutableFloatStateOf(tafseerPrefs.getFloat(TafseerSettingsManager.KEY_LINE_SPACING, 1.2f))
    }
    var selectedLanguage by remember(tafseer.id) {
        mutableStateOf(tafseerPrefs.getString(TafseerSettingsManager.KEY_TRANSLATION_LANG, "URDU") ?: "URDU")
    }
    var isDarkMode by remember(tafseer.id) {
        mutableStateOf(tafseerPrefs.getBoolean(TafseerSettingsManager.KEY_DARK_MODE, false))
    }
    var scriptStyle by remember(tafseer.id) {
        mutableStateOf(tafseerPrefs.getString(TafseerSettingsManager.KEY_SCRIPT_STYLE, "UTHMANI") ?: "UTHMANI")
    }

    val isDark = MaterialTheme.colorScheme.background.red * 0.299f +
                 MaterialTheme.colorScheme.background.green * 0.587f +
                 MaterialTheme.colorScheme.background.blue * 0.114f < 0.5f

    val modalBg = if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surface
    val containerBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    val containerBorder = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val controlCardBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
    val primaryGreen = MaterialTheme.colorScheme.primary
    val goldAccent = MaterialTheme.colorScheme.secondary
    val activeCardBg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.6f else 0.4f)
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val headerCardBg = if (isDark) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary
    val headerCardText = if (isDark) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = modalBg,
        dragHandle = { BottomSheetDefaults.DragHandle(color = goldAccent) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            
            // Header Hero Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = headerCardBg,
                border = BorderStroke(1.5.dp, goldAccent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${tafseer.name} Reading Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = headerCardText,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "تفسیری ترجیحات اور قراءت کی ترتیبات",
                        fontSize = 12.sp,
                        color = goldAccent,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 1. General Section Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = containerBg,
                border = BorderStroke(1.dp, containerBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            tint = primaryGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "General Settings",
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            fontSize = 16.sp
                        )
                    }
                    HorizontalDivider(color = containerBorder)
                    
                    // Theme Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Reading Theme",
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Text(
                                "Switch between Light and Dark parchment",
                                color = textSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = {
                                isDarkMode = it
                                TafseerSettingsManager.setDarkMode(context, tafseer.id, it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = primaryGreen,
                                uncheckedThumbColor = textSecondary,
                                uncheckedTrackColor = containerBorder
                            )
                        )
                    }
                }
            }

            // 2. Reading Options Section Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = containerBg,
                border = BorderStroke(1.dp, containerBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.FormatSize,
                            contentDescription = null,
                            tint = primaryGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Reading Options",
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            fontSize = 16.sp
                        )
                    }
                    HorizontalDivider(color = containerBorder)
                    
                    // Font Size Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Arabic Font Size", fontWeight = FontWeight.Bold, color = textPrimary)
                            Text("${fontSizeSp.toInt()} sp", fontWeight = FontWeight.Bold, color = goldAccent)
                        }
                        Slider(
                            value = fontSizeSp,
                            onValueChange = {
                                fontSizeSp = it
                                TafseerSettingsManager.setFontSize(context, tafseer.id, it)
                            },
                            valueRange = 18f..46f,
                            colors = SliderDefaults.colors(
                                thumbColor = goldAccent,
                                activeTrackColor = primaryGreen,
                                inactiveTrackColor = containerBorder
                            )
                        )
                    }

                    // Line Height Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Line Spacing", fontWeight = FontWeight.Bold, color = textPrimary)
                            Text(String.format(java.util.Locale.US, "%.1fx", lineHeightMultiplier), fontWeight = FontWeight.Bold, color = goldAccent)
                        }
                        Slider(
                            value = lineHeightMultiplier,
                            onValueChange = {
                                lineHeightMultiplier = it
                                TafseerSettingsManager.setLineSpacing(context, tafseer.id, it)
                            },
                            valueRange = 0.9f..2.2f,
                            colors = SliderDefaults.colors(
                                thumbColor = goldAccent,
                                activeTrackColor = primaryGreen,
                                inactiveTrackColor = containerBorder
                            )
                        )
                    }

                    // Script Style
                    Column {
                        Text(
                            "Script Style (Khatt)",
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        // Uthmani Permanent Active Badge / Card
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, goldAccent),
                            color = activeCardBg
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = goldAccent.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, goldAccent.copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = "عثماني",
                                            fontFamily = UthmaniFontFamily,
                                            fontSize = 22.sp,
                                            color = goldAccent,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Uthmani Script (عثماني)",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textPrimary
                                        )
                                        Text(
                                            text = "Active Default Arabic Font",
                                            fontSize = 12.sp,
                                            color = textSecondary
                                        )
                                    }
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = goldAccent
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Active",
                                        tint = MaterialTheme.colorScheme.onSecondary,
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Translation & Tafseer Section Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = containerBg,
                border = BorderStroke(1.dp, containerBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.AutoStories,
                            contentDescription = null,
                            tint = primaryGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Translation & Tafseer",
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            fontSize = 16.sp
                        )
                    }
                    HorizontalDivider(color = containerBorder)
                    
                    val languageOptions = listOf(
                        Triple("URDU", "Urdu Translation", "Abul A'la Maududi"),
                        Triple("ENGLISH", "English Translation", "Saheeh International"),
                        Triple("ARABIC", "Arabic Translation/Tafseer", "التفسير الميسر (Al-Muyassar)"),
                        Triple("HINDI", "Hindi Translation", "हिन्दी अनुवाद व सम्पूर्ण तफ़सीر (Devanagari)")
                    )

                    languageOptions.forEach { (langCode, title, subtitle) ->
                        val isSelected = selectedLanguage == langCode
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    selectedLanguage = langCode
                                    TafseerSettingsManager.setTranslationLanguage(context, tafseer.id, langCode)
                                },
                            color = if (isSelected) activeCardBg else controlCardBg,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) goldAccent else containerBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            selectedLanguage = langCode
                                            TafseerSettingsManager.setTranslationLanguage(context, tafseer.id, langCode)
                                        },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = goldAccent,
                                            unselectedColor = primaryGreen
                                        )
                                    )
                                    Column {
                                        Text(
                                            text = title,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            color = if (isSelected) goldAccent else textPrimary,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = subtitle,
                                            color = textSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                TextButton(
                                    onClick = {
                                        selectedLanguage = langCode
                                        TafseerSettingsManager.setTranslationLanguage(context, tafseer.id, langCode)
                                    }
                                ) {
                                    Text(
                                        text = if (isSelected) "ACTIVE" else "CHANGE",
                                        color = if (isSelected) goldAccent else primaryGreen,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

private fun toEasternArabicDigits(number: Int): String {
    val latin = number.toString()
    val digits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val sb = StringBuilder()
    for (ch in latin) {
        if (ch in '0'..'9') {
            sb.append(digits[ch - '0'])
        } else {
            sb.append(ch)
        }
    }
    return sb.toString()
}



