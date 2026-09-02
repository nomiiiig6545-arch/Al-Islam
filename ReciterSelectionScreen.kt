package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ALL_RECITERS
import com.example.data.model.AVAILABLE_RECITERS
import com.example.data.model.TRANSLATION_RECITERS
import com.example.data.model.Reciter
import com.example.ui.QuranViewModel
import com.example.ui.components.ReciterImage
import com.example.ui.theme.ArabicFontFamily
import com.example.ui.theme.HandmadeBrushesFontFamily
import com.example.ui.theme.UrduFontFamily
import com.example.ui.theme.UthmaniFontFamily

/**
 * ReciterSelectionScreen displays a Grid layout of popular Qaris/Reciters.
 * Clicking on any Qari navigates to the Surah selection screen for that reciter.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReciterSelectionScreen(
    viewModel: QuranViewModel,
    initialIsTranslation: Boolean = false,
    onReciterSelected: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var isTranslationTab by remember(initialIsTranslation) { mutableStateOf(initialIsTranslation) }
    var searchQuery by remember { mutableStateOf("") }
    val downloadStates by viewModel.audioDownloadStates.collectAsStateWithLifecycle()

    val isDark = MaterialTheme.colorScheme.background.red * 0.299f +
                 MaterialTheme.colorScheme.background.green * 0.587f +
                 MaterialTheme.colorScheme.background.blue * 0.114f < 0.5f

    // Active Theme Contrast Colors for Light & Dark Mode
    val activeAccent = if (isTranslationTab) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
    val activeTitleColor = if (isTranslationTab) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
    val activeBannerBg = if (isTranslationTab) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    val cardBg = MaterialTheme.colorScheme.surface
    val defaultBorder = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.6f else 0.4f)

    val filteredReciters = remember(searchQuery, isTranslationTab) {
        val baseList = ALL_RECITERS.filter { if (isTranslationTab) it.isTranslation else !it.isTranslation }
        if (searchQuery.isBlank()) {
            baseList
        } else {
            baseList.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.nameArabic.contains(searchQuery, ignoreCase = true) ||
                it.subtext.contains(searchQuery, ignoreCase = true) ||
                it.country.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Al-Quran Majeed",
                            fontFamily = HandmadeBrushesFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Famous Quran Reciters (مشاهير القراء)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("reciter_selection_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.shadow(2.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Toggle Segmented Control: Left: Arabic-only Reciters vs Right: Urdu-with-Translation Reciters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Tab 1: Arabic Only Reciters (Left)
                val isArabicActive = !isTranslationTab
                val tab1BgColor = if (isArabicActive) {
                    if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.Black.copy(alpha = 0.2f)
                } else {
                    if (isDark) MaterialTheme.colorScheme.surface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                }
                val tab1ContentColor = if (isArabicActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                val tab1Border = if (isArabicActive) {
                    BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                } else {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                }

                Surface(
                    onClick = { isTranslationTab = false },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("arabic_reciters_tab"),
                    shape = RoundedCornerShape(16.dp),
                    color = tab1BgColor,
                    border = tab1Border,
                    contentColor = tab1ContentColor
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = tab1ContentColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Arabic-only Reciters",
                                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                                color = tab1ContentColor,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "تلاوت بغیر ترجمہ",
                            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = UrduFontFamily),
                            color = tab1ContentColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Tab 2: Urdu Translation Reciters (Right)
                val isTranslationActive = isTranslationTab
                val tab2BgColor = if (isTranslationActive) {
                    if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.Black.copy(alpha = 0.2f)
                } else {
                    if (isDark) MaterialTheme.colorScheme.surface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                }
                val tab2ContentColor = if (isTranslationActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                val tab2Border = if (isTranslationActive) {
                    BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary)
                } else {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                }

                Surface(
                    onClick = { isTranslationTab = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("translation_reciters_tab"),
                    shape = RoundedCornerShape(16.dp),
                    color = tab2BgColor,
                    border = tab2Border,
                    contentColor = tab2ContentColor
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = tab2ContentColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Urdu-with-Translation",
                                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                                color = tab2ContentColor,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "تلاوت مع اردو ترجمہ",
                            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = UrduFontFamily),
                            color = tab2ContentColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar for Qaris
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                textStyle = LocalTextStyle.current.copy(fontFamily = UrduFontFamily),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reciter_selection_search_input"),
                placeholder = {
                    Text(
                        "Search Reciter (e.g. Sudais, Alafasy, Ghamdi)...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = activeAccent
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = activeAccent,
                    unfocusedBorderColor = defaultBorder,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Information Card Banner
            Surface(
                color = activeBannerBg,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = null,
                        tint = activeAccent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Choose your favorite Qari to explore their complete 114 Surahs recitation and download for offline listening.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = activeTitleColor,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Reciters Grid View
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("reciters_grid")
            ) {
                items(filteredReciters, key = { it.id }) { reciter ->
                    val downloadedCount = remember(reciter.id, downloadStates) {
                        viewModel.getDownloadedSurahsCount(reciter.id)
                    }

                    ReciterProfileCard(
                        reciter = reciter,
                        downloadedCount = downloadedCount,
                        cardBg = cardBg,
                        defaultBorder = defaultBorder,
                        isDark = isDark,
                        onClick = { onReciterSelected(reciter.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReciterProfileCard(
    reciter: Reciter,
    downloadedCount: Int,
    cardBg: Color,
    defaultBorder: Color,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val reciterBorderColor = if (reciter.isTranslation) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
    val reciterTitleColor = if (reciter.isTranslation) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
    val reciterBadgeBg = if (reciter.isTranslation) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    val reciterBadgeContentColor = if (reciter.isTranslation) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("reciter_profile_card_${reciter.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.2.dp, reciterBorderColor.copy(alpha = if (isDark) 0.6f else 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Reciter Circular Photo with Dynamic Ring Accent
            ReciterImage(
                reciter = reciter,
                size = 76.dp,
                borderWidth = 2.dp,
                borderColor = reciterBorderColor
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Arabic / Urdu Calligraphy Name
            if (reciter.nameArabic.isNotBlank()) {
                Text(
                    text = reciter.nameArabic,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = ArabicFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = reciterTitleColor
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Reciter Name
            Text(
                text = reciter.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.heightIn(min = 38.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Title & Country
            Text(
                text = "${reciter.subtext} • ${reciter.country}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    fontSize = 10.sp
                ),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Download Status Badge
            Surface(
                color = reciterBadgeBg,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(
                    0.5.dp,
                    reciterBorderColor.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (downloadedCount > 0) Icons.Default.OfflinePin else Icons.Default.Audiotrack,
                        contentDescription = null,
                        tint = reciterBadgeContentColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (downloadedCount > 0) "$downloadedCount Downloaded" else "114 Surahs",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = reciterBadgeContentColor,
                            fontSize = 10.5.sp
                        )
                    )
                }
            }
        }
    }
}
