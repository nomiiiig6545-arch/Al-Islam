package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.BookmarkEntity
import com.example.data.mushaf.IndoPakMushafData
import com.example.data.mushaf.MushafPageCacheManager
import com.example.ui.QuranViewModel
import com.example.ui.components.MushafPageCurlReader
import com.example.ui.components.MushafPageImageView
import com.example.ui.components.PageTurnSoundManager
import com.example.ui.components.rememberPageCurlState
import com.example.ui.theme.ArabicFontFamily
import com.example.ui.theme.HandmadeBrushesFontFamily
import com.example.ui.theme.UrduFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MushafScreen(
    surahNumber: Int = 1,
    startPageNumber: Int? = null,
    viewModel: QuranViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToBookmarks: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val downloadManager = remember { com.example.data.mushaf.MushafPageDownloadManager.getInstance(context) }
    val downloadProgress by downloadManager.downloadProgress.collectAsStateWithLifecycle()

    val lastReadPage by viewModel.lastReadMushafPage.collectAsStateWithLifecycle()
    val bookmarkedPages by viewModel.bookmarkedPageNumbers.collectAsStateWithLifecycle()
    val allBookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val isPlayingAudio by viewModel.isSurahAudioPlaying.collectAsStateWithLifecycle()

    // Page numbers are 1..549 (0-indexed pager state: 0..548)
    val initialPage = remember {
        val target = startPageNumber ?: IndoPakMushafData.getPageForSurah(surahNumber)
        (target - 1).coerceIn(0, IndoPakMushafData.TOTAL_PAGES - 1)
    }

    // Use a state-driven approach where CurrentPage index is observed via collectAsState
    val activePageIndexFlow = remember { kotlinx.coroutines.flow.MutableStateFlow(initialPage) }
    val activePageIndex by activePageIndexFlow.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { IndoPakMushafData.TOTAL_PAGES }
    )

    fun navigateToPage(pageIndex: Int) {
        val target = pageIndex.coerceIn(0, IndoPakMushafData.TOTAL_PAGES - 1)
        activePageIndexFlow.value = target
        coroutineScope.launch { pagerState.scrollToPage(target) }
    }

    fun animateToPage(pageIndex: Int) {
        val target = pageIndex.coerceIn(0, IndoPakMushafData.TOTAL_PAGES - 1)
        activePageIndexFlow.value = target
        coroutineScope.launch { pagerState.animateScrollToPage(target) }
    }

    // Ensure the pager container forces a re-composition when the index changes
    LaunchedEffect(pagerState.currentPage) {
        if (activePageIndexFlow.value != pagerState.currentPage) {
            activePageIndexFlow.value = pagerState.currentPage
        }
    }

    var isNightMode by remember { mutableStateOf(false) }
    var isPageCurlEnabled by remember { mutableStateOf(true) }
    var isPageTurnSoundEnabled by remember { mutableStateOf(PageTurnSoundManager.isSoundEnabled()) }
    val pageCurlState = rememberPageCurlState()

    // Page Overlay Settings
    var showOverlaySheet by remember { mutableStateOf(false) }
    var overlayColor by remember { mutableStateOf(Color(0xFFE6D7B8)) } // Warm Sepia
    var overlayAlpha by remember { mutableFloatStateOf(0.12f) }
    var isOverlayEnabled by remember { mutableStateOf(false) }

    // Index & Jump Sheets
    var showIndexSheet by remember { mutableStateOf(false) }
    var selectedIndexTab by remember { mutableIntStateOf(0) } // 0: Surahs, 1: Juz, 2: Go to Page, 3: Bookmarks, 4: Offline

    // Add / Edit Bookmark Dialog
    var showAddBookmarkDialog by remember { mutableStateOf(false) }
    var bookmarkTitleInput by remember { mutableStateOf("") }

    val currentPageNum = activePageIndex + 1
    val currentPageInfo = remember(currentPageNum) { IndoPakMushafData.getPageInfo(currentPageNum) }
    val isCurrentPageBookmarked = bookmarkedPages.contains(currentPageNum)

    var isInitialPageLoad by remember { mutableStateOf(true) }

    // Save last read page automatically & aggressively preload adjacent pages
    LaunchedEffect(currentPageNum) {
        if (!isInitialPageLoad) {
            PageTurnSoundManager.play(context)
        } else {
            isInitialPageLoad = false
            PageTurnSoundManager.init(context)
        }
        viewModel.setLastReadMushafPage(currentPageNum)
        MushafPageCacheManager.initializeIndex(context)
        MushafPageCacheManager.preloadAdjacentPages(context, currentPageNum)
    }

    val isSysDark = MaterialTheme.colorScheme.background.red * 0.299f +
                    MaterialTheme.colorScheme.background.green * 0.587f +
                    MaterialTheme.colorScheme.background.blue * 0.114f < 0.5f

    val isDark = isNightMode || isSysDark

    val barBgColor = if (isNightMode) {
        Color(0xFF0D241C)
    } else if (isSysDark) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.primary
    }

    val barTitleColor = if (isNightMode) {
        Color(0xFFFFD700)
    } else if (isSysDark) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onPrimary
    }

    val barSubtitleColor = if (isNightMode) {
        Color(0xFFD4EBDC)
    } else if (isSysDark) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    val barIconColor = if (isNightMode) {
        Color.White
    } else if (isSysDark) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onPrimary
    }

    val goldAccent = MaterialTheme.colorScheme.secondary

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = barBgColor,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = barIconColor
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedIndexTab = 0
                                    showIndexSheet = true
                                }
                        ) {
                            Text(
                                text = "Al-Quran Majeed",
                                fontFamily = HandmadeBrushesFontFamily,
                                color = barTitleColor,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "Juz ${currentPageInfo.juzNumber} (${currentPageInfo.juzNameArabic})  •  صفحة ${IndoPakMushafData.toArabicDigits(currentPageNum)} (Page $currentPageNum)",
                                color = barSubtitleColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }

                        // Top Bar Bookmark Button (Opens Add/Edit Bookmark Dialog for Current Page)
                        IconButton(
                            onClick = {
                                val existing = allBookmarks.find { it.pageNumber == currentPageNum }
                                bookmarkTitleInput = existing?.surahName?.takeIf { it.isNotBlank() }
                                    ?: "Surah ${currentPageInfo.surahNameEnglish}"
                                showAddBookmarkDialog = true
                            },
                            modifier = Modifier.testTag("top_bar_bookmark_button")
                        ) {
                            Icon(
                                imageVector = if (isCurrentPageBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (isCurrentPageBookmarked) "Edit Bookmark" else "Add Bookmark",
                                tint = if (isCurrentPageBookmarked) goldAccent else barIconColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    if (downloadProgress.isDownloading) {
                        LinearProgressIndicator(
                            progress = { downloadProgress.progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp),
                            color = goldAccent,
                            trackColor = Color(0x33000000)
                        )
                    } else {
                        HorizontalDivider(color = goldAccent.copy(alpha = 0.4f), thickness = 1.dp)
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = barBgColor,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    HorizontalDivider(color = goldAccent.copy(alpha = 0.4f), thickness = 1.dp)
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Back Button (Bottom toolbar corner)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onNavigateBack() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = barIconColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Back",
                                    color = barIconColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Surah Index & List
                            IconButton(onClick = {
                                selectedIndexTab = 0
                                showIndexSheet = true
                            }) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = "Surah Index",
                                    tint = barIconColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Night Mode Toggle
                            IconButton(onClick = { isNightMode = !isNightMode }) {
                                Icon(
                                    imageVector = if (isNightMode) Icons.Default.LightMode else Icons.Default.Nightlight,
                                    contentDescription = "Toggle Night Mode",
                                    tint = if (isNightMode) goldAccent else barIconColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Eye Comfort Overlay Settings Button
                            IconButton(onClick = { showOverlaySheet = true }) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "Eye Comfort Overlay",
                                    tint = if (isOverlayEnabled) goldAccent else barIconColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = if (isDark || isNightMode) Color(0xFF000000) else Color(0xFFFAF6EE),
        contentWindowInsets = WindowInsets(0.dp)
    ) { innerPadding ->
        val pageThemeBg = if (isDark || isNightMode) Color(0xFF000000) else Color(0xFFFAF6EE)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(pageThemeBg)
        ) {
            // ALWAYS attach HorizontalPager so pagerState works for programmatic scrolling
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                HorizontalPager(
                    state = pagerState,
                    beyondViewportPageCount = 5,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(pageThemeBg),
                    userScrollEnabled = !isPageCurlEnabled,
                    key = { it }
                ) { pageIndex ->
                    if (!isPageCurlEnabled) {
                        val pageNumber = pageIndex + 1
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(pageThemeBg)
                        ) {
                            MushafPageImageView(
                                pageNumber = pageNumber,
                                isDark = isDark,
                                isNightMode = isNightMode,
                                overlayColor = if (isOverlayEnabled) overlayColor else Color.Transparent,
                                overlayAlpha = if (isOverlayEnabled) overlayAlpha else 0f,
                                onTapPage = {
                                    // User can tap page to interact with zoom or controls
                                }
                            )
                        }
                    }
                }
            }

            if (isPageCurlEnabled) {
                // Authentic Book Page-Curl Reader (Top page peels to reveal stationary bottom page)
                MushafPageCurlReader(
                    currentPage = currentPageNum,
                    isDark = isDark,
                    isNightMode = isNightMode,
                    overlayColor = if (isOverlayEnabled) overlayColor else Color.Transparent,
                    overlayAlpha = if (isOverlayEnabled) overlayAlpha else 0f,
                    pageCurlState = pageCurlState,
                    onPageChanged = { newPage ->
                        coroutineScope.launch {
                            navigateToPage(newPage - 1)
                        }
                    },
                    onTapPage = {}
                )
            }
        }
    }

    // --- Bottom Sheet: Page Overlay & Display Settings ---
    if (showOverlaySheet) {
        ModalBottomSheet(
            onDismissRequest = { showOverlaySheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Page Overlay & Reading Mode",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Adjust reading comfort, page turn animation, and night mode.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )



                // Night Mode Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Nightlight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Night Mode (Inverted)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                    Switch(
                        checked = isNightMode,
                        onCheckedChange = { isNightMode = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                // Overlay Toggle Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Eye Comfort Overlay",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                    Switch(
                        checked = isOverlayEnabled,
                        onCheckedChange = { isOverlayEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }

                // Overlay Color Presets
                Text(
                    text = "Overlay Tone",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 10.dp, bottom = 8.dp)
                )

                val overlayTones = listOf(
                    "Sepia Warm" to Color(0xFFE6D7B8),
                    "Soft Green" to Color(0xFFD6EBD9),
                    "Pale Yellow" to Color(0xFFFFF3CD),
                    "Amber Warm" to Color(0xFFFFE0B2),
                    "Cool Tint" to Color(0xFFDDEBFF)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    overlayTones.forEach { (_, color) ->
                        val isSelected = isOverlayEnabled && overlayColor == color
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = color,
                            border = if (isSelected) BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, Color.LightGray),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clickable {
                                    overlayColor = color
                                    isOverlayEnabled = true
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Overlay Opacity / Strength Slider
                Text(
                    text = "Overlay Intensity: ${(overlayAlpha * 100).toInt()}%",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )

                Slider(
                    value = overlayAlpha,
                    onValueChange = {
                        overlayAlpha = it
                        if (!isOverlayEnabled && it > 0.05f) {
                            isOverlayEnabled = true
                        }
                    },
                    valueRange = 0.05f..0.50f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        showOverlaySheet = false
                        onNavigateToSettings()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("App General Settings")
                }
            }
        }
    }

    // --- Bottom Sheet: Surahs, Juz, Go To Page & Bookmarks ---
    if (showIndexSheet) {
        ModalBottomSheet(
            onDismissRequest = { showIndexSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Tab Row
                ScrollableTabRow(
                    selectedTabIndex = selectedIndexTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 0.dp
                ) {
                    Tab(
                        selected = selectedIndexTab == 0,
                        onClick = { selectedIndexTab = 0 },
                        text = { Text("Surahs", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    )
                    Tab(
                        selected = selectedIndexTab == 1,
                        onClick = { selectedIndexTab = 1 },
                        text = { Text("Juz (30)", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    )
                    Tab(
                        selected = selectedIndexTab == 2,
                        onClick = { selectedIndexTab = 2 },
                        text = { Text("Go To Page", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    )
                    Tab(
                        selected = selectedIndexTab == 3,
                        onClick = { selectedIndexTab = 3 },
                        text = { Text("Bookmarks", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    )
                    Tab(
                        selected = selectedIndexTab == 4,
                        onClick = { selectedIndexTab = 4 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (downloadProgress.isCompleted) Icons.Default.CheckCircle else Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (downloadProgress.isCompleted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("آف لائن (Offline)", fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = UrduFontFamily)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (selectedIndexTab) {
                    // Tab 0: Surah Index
                    0 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxHeight(0.65f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(114) { index ->
                                val surahNo = index + 1
                                val startPage = IndoPakMushafData.getPageForSurah(surahNo)
                                val arabicName = IndoPakMushafData.SURAH_NAMES_ARABIC[index]
                                val englishName = IndoPakMushafData.SURAH_NAMES_ENGLISH[index]

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (currentPageInfo.surahNumber == surahNo) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val target = (startPage - 1).coerceIn(0, IndoPakMushafData.TOTAL_PAGES - 1)
                                            coroutineScope.launch {
                                                navigateToPage(target)
                                            }
                                            showIndexSheet = false
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = surahNo.toString(),
                                                        color = Color.White,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(text = englishName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                Text(text = "Page $startPage", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }

                                        Text(
                                            text = arabicName,
                                            fontFamily = ArabicFontFamily,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Tab 1: Juz Index
                    1 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxHeight(0.65f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(30) { index ->
                                val juzNo = index + 1
                                val startPage = IndoPakMushafData.JUZ_START_PAGES[index]
                                val arabicName = IndoPakMushafData.JUZ_NAMES_ARABIC[index]

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (currentPageInfo.juzNumber == juzNo) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val target = (startPage - 1).coerceIn(0, IndoPakMushafData.TOTAL_PAGES - 1)
                                            coroutineScope.launch {
                                                navigateToPage(target)
                                            }
                                            showIndexSheet = false
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = juzNo.toString(),
                                                        color = Color.White,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(text = "Juz $juzNo (Para $juzNo)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                Text(text = "Page $startPage", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }

                                        Text(
                                            text = arabicName,
                                            fontFamily = ArabicFontFamily,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Tab 2: Go to Page
                    2 -> {
                        var pageInput by remember { mutableStateOf(currentPageNum.toString()) }
                        var sliderValue by remember { mutableFloatStateOf(currentPageNum.toFloat()) }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val previewPage = sliderValue.toInt().coerceIn(1, IndoPakMushafData.TOTAL_PAGES)
                            val previewInfo = IndoPakMushafData.getPageInfo(previewPage)

                            Text(
                                text = "Page $previewPage • ${previewInfo.surahNameArabic} (${previewInfo.surahNameEnglish}) • Juz ${previewInfo.juzNumber}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Slider(
                                value = sliderValue,
                                onValueChange = {
                                    sliderValue = it
                                    pageInput = it.toInt().toString()
                                },
                                valueRange = 1f..IndoPakMushafData.TOTAL_PAGES.toFloat(),
                                steps = IndoPakMushafData.TOTAL_PAGES - 1,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = pageInput,
                                onValueChange = { input ->
                                    pageInput = input
                                    val parsed = input.toIntOrNull()
                                    if (parsed != null && parsed in 1..IndoPakMushafData.TOTAL_PAGES) {
                                        sliderValue = parsed.toFloat()
                                    }
                                },
                                label = { Text("Page Number (1 - ${IndoPakMushafData.TOTAL_PAGES})") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val targetPage = pageInput.toIntOrNull()?.coerceIn(1, IndoPakMushafData.TOTAL_PAGES) ?: sliderValue.toInt()
                                    coroutineScope.launch {
                                        navigateToPage(targetPage - 1)
                                    }
                                    showIndexSheet = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Open Page $previewPage")
                            }
                        }
                    }

                    // Tab 3: Saved Bookmarks
                    3 -> {
                        val pageBookmarks = remember(allBookmarks) { allBookmarks.filter { it.pageNumber > 0 } }

                        if (pageBookmarks.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No saved page bookmarks yet.\nTap the bookmark icon at the top to save any page.",
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxHeight(0.65f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(pageBookmarks) { bookmark: BookmarkEntity ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val target = (bookmark.pageNumber - 1).coerceIn(0, IndoPakMushafData.TOTAL_PAGES - 1)
                                                coroutineScope.launch {
                                                    navigateToPage(target)
                                                }
                                                showIndexSheet = false
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Page ${bookmark.pageNumber} • ${bookmark.surahName}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp
                                                )
                                                Text(
                                                    text = bookmark.urduTranslation,
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            IconButton(onClick = {
                                                viewModel.deleteBookmark(bookmark)
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete Bookmark",
                                                    tint = MaterialTheme.colorScheme.error
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

    // Redesigned Add/Edit Bookmark Dialog
    if (showAddBookmarkDialog) {
        val isSysDarkMode = MaterialTheme.colorScheme.background.luminance() < 0.5f
        val isDialogDark = isNightMode || isSysDarkMode

        val dialogBg = if (isDialogDark) MaterialTheme.colorScheme.surface else Color(0xFFFAF8F5)
        val headerBg = if (isDialogDark) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary
        val goldAccent = MaterialTheme.colorScheme.secondary
        val cardBg = if (isDialogDark) Color(0xFF1E2A23) else Color.White
        val cardBorderColor = if (isDialogDark) goldAccent.copy(alpha = 0.5f) else goldAccent.copy(alpha = 0.8f)
        val primaryText = if (isDialogDark) Color.White else Color(0xFF1B3123)
        val secondaryText = if (isDialogDark) Color(0xFFB0C4B8) else Color(0xFF4A6353)

        val juzDisplayName = if (currentPageInfo.juzNameEnglish.isNotBlank()) {
            "Juz ${currentPageInfo.juzNumber}: ${currentPageInfo.juzNameEnglish} (${currentPageInfo.juzNameArabic})"
        } else {
            "Juz ${currentPageInfo.juzNumber}: ${currentPageInfo.juzNameArabic}"
        }

        Dialog(
            onDismissRequest = { showAddBookmarkDialog = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = dialogBg,
                        border = BorderStroke(1.5.dp, goldAccent),
                        shadowElevation = 16.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // 1. Header Banner with Gold Accents
                            Surface(
                                color = headerBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .background(goldAccent.copy(alpha = 0.2f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Bookmark,
                                                contentDescription = null,
                                                tint = goldAccent,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = if (isCurrentPageBookmarked) "Edit Bookmark" else "Add New Bookmark",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "علامة مرجعية جديدة",
                                                fontFamily = ArabicFontFamily,
                                                fontSize = 14.sp,
                                                color = goldAccent
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { showAddBookmarkDialog = false },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close",
                                            tint = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }

                            // Gold Decorative Separator Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .background(goldAccent)
                            )

                            // 2. Dialog Body Content
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Surah, Juz & Page Info Card
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = cardBg,
                                    border = BorderStroke(1.2.dp, cardBorderColor),
                                    shadowElevation = 2.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Surah ${currentPageInfo.surahNameEnglish}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 17.sp,
                                                    color = primaryText
                                                )
                                                Text(
                                                    text = juzDisplayName,
                                                    fontSize = 13.sp,
                                                    color = secondaryText,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }

                                            // Arabic Surah Calligraphy & Page Badge
                                            Column(
                                                horizontalAlignment = Alignment.End
                                            ) {
                                                Text(
                                                    text = currentPageInfo.surahNameArabic,
                                                    fontFamily = ArabicFontFamily,
                                                    fontSize = 20.sp,
                                                    color = goldAccent,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = goldAccent.copy(alpha = 0.15f),
                                                    border = BorderStroke(0.8.dp, goldAccent.copy(alpha = 0.4f))
                                                ) {
                                                    Text(
                                                        text = "Page $currentPageNum",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isDialogDark) goldAccent else Color(0xFF0E4A32),
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Bookmark Title Field Label & Input
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Bookmark Title / Label (عنوان العلامة)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = secondaryText
                                    )

                                    OutlinedTextField(
                                        value = bookmarkTitleInput,
                                        onValueChange = { bookmarkTitleInput = it },
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = primaryText
                                        ),
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Label,
                                                contentDescription = null,
                                                tint = goldAccent,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        trailingIcon = {
                                            if (bookmarkTitleInput.isNotEmpty()) {
                                                IconButton(onClick = { bookmarkTitleInput = "" }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Clear,
                                                        contentDescription = "Clear",
                                                        tint = secondaryText,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        },
                                        placeholder = {
                                            Text(
                                                text = "Surah ${currentPageInfo.surahNameEnglish}",
                                                color = secondaryText.copy(alpha = 0.5f),
                                                fontSize = 14.sp
                                            )
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = cardBg,
                                            unfocusedContainerColor = cardBg,
                                            focusedBorderColor = goldAccent,
                                            unfocusedBorderColor = cardBorderColor,
                                            cursorColor = goldAccent
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                // Quick Suggestion Chips
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Quick Labels:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = secondaryText
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val suggestions = listOf(
                                            "Daily Recitation",
                                            "Last Read",
                                            "Important",
                                            "Revision"
                                        )

                                        suggestions.forEach { tag ->
                                            val isSelected = bookmarkTitleInput == tag
                                            Surface(
                                                shape = RoundedCornerShape(20.dp),
                                                color = if (isSelected) goldAccent else cardBg,
                                                border = BorderStroke(1.dp, if (isSelected) goldAccent else cardBorderColor),
                                                modifier = Modifier.clickable { bookmarkTitleInput = tag }
                                            ) {
                                                Text(
                                                    text = tag,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) Color.White else primaryText,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Bottom Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isCurrentPageBookmarked) {
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.removePageBookmark(currentPageNum)
                                                showAddBookmarkDialog = false
                                                Toast.makeText(context, "Bookmark Removed", Toast.LENGTH_SHORT).show()
                                            },
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                            shape = RoundedCornerShape(14.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
                                            modifier = Modifier.weight(0.32f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Delete",
                                                color = MaterialTheme.colorScheme.error,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = { showAddBookmarkDialog = false },
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, cardBorderColor),
                                        contentPadding = PaddingValues(vertical = 12.dp),
                                        modifier = Modifier.weight(if (isCurrentPageBookmarked) 0.32f else 0.4f)
                                    ) {
                                        Text(
                                            text = "Cancel",
                                            color = primaryText,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.saveOrUpdatePageBookmark(
                                                pageNumber = currentPageNum,
                                                surahNumber = currentPageInfo.surahNumber,
                                                title = bookmarkTitleInput.ifBlank { "Surah ${currentPageInfo.surahNameEnglish}" },
                                                juzText = "Juz ${currentPageInfo.juzNumber} (${currentPageInfo.juzNameArabic})",
                                                arabicSurahName = currentPageInfo.surahNameArabic
                                            )
                                            showAddBookmarkDialog = false
                                            val toastMsg = if (isCurrentPageBookmarked) "Bookmark Updated" else "Page $currentPageNum Bookmarked"
                                            Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isDialogDark) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary,
                                            contentColor = Color.White
                                        ),
                                        contentPadding = PaddingValues(vertical = 12.dp),
                                        modifier = Modifier.weight(if (isCurrentPageBookmarked) 0.48f else 0.6f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bookmark,
                                            contentDescription = null,
                                            tint = goldAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Save",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
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