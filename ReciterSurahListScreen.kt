package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.api.Surah
import com.example.data.audio.AudioDownloadStatus
import com.example.data.model.AVAILABLE_RECITERS
import com.example.data.model.Reciter
import com.example.data.mushaf.IndoPakMushafData
import com.example.ui.QuranViewModel
import com.example.ui.SurahsState
import com.example.ui.components.ReciterImage
import com.example.ui.theme.ArabicFontFamily
import com.example.ui.theme.HandmadeBrushesFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReciterSurahListScreen(
    reciterId: String,
    viewModel: QuranViewModel,
    onSurahSelected: (Int) -> Unit,
    onNavigateToDownloadedSurahs: ((String) -> Unit)? = null,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val reciter = remember(reciterId) {
        viewModel.getReciterById(reciterId)
    }

    LaunchedEffect(reciterId) {
        viewModel.setSelectedReciter(reciterId)
    }

    val surahsState by viewModel.surahsState.collectAsStateWithLifecycle()
    val downloadStates by viewModel.audioDownloadStates.collectAsStateWithLifecycle()
    val currentPlayingSurah by viewModel.currentOfflineSurahNumber.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlayerPlaying.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f


    // Dynamic Theme Tokens
    val containerBg = MaterialTheme.colorScheme.background
    val cardBg = MaterialTheme.colorScheme.surface
    val cardBorder = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val primaryGreen = MaterialTheme.colorScheme.primary
    val goldYellow = MaterialTheme.colorScheme.secondary

    // Fallback standard 114 Surah list if API state is loading
    val allSurahs = remember(surahsState) {
        when (val state = surahsState) {
            is SurahsState.Success -> state.surahs
            else -> (1..114).map { number ->
                val idx = number - 1
                val arabicName = IndoPakMushafData.SURAH_NAMES_ARABIC.getOrElse(idx) { "سورة" }
                val englishName = IndoPakMushafData.SURAH_NAMES_ENGLISH.getOrElse(idx) { "Surah $number" }
                val ayahCount = IndoPakMushafData.SURAH_AYAH_COUNTS.getOrElse(idx) { 7 }
                val revType = if (number in listOf(2, 3, 4, 5, 8, 9, 22, 24, 33, 47, 48, 49, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 76, 98, 110)) "Madani" else "Makki"
                Surah(
                    number = number,
                    name = arabicName,
                    englishName = englishName,
                    englishNameTranslation = revType,
                    numberOfAyahs = ayahCount,
                    revelationType = revType
                )
            }
        }
    }

    val filteredSurahs = remember(searchQuery, allSurahs) {
        if (searchQuery.isBlank()) {
            allSurahs
        } else {
            allSurahs.filter { surah ->
                surah.number.toString().contains(searchQuery) ||
                surah.englishName.contains(searchQuery, ignoreCase = true) ||
                surah.name.contains(searchQuery, ignoreCase = true) ||
                surah.englishNameTranslation.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val downloadedCount = remember(reciterId, downloadStates) {
        viewModel.getDownloadedSurahsCount(reciterId)
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
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${reciter.name} • $downloadedCount/114 Offline",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = goldYellow
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("reciter_surah_list_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = goldYellow
                        )
                    }
                },
                actions = {
                    // Quick Download All Button
                    FilledTonalButton(
                        onClick = {
                            viewModel.downloadAllSurahsForReciter(reciterId) {
                                Toast.makeText(
                                    context,
                                    "Downloading all 114 Surahs for ${reciter.name}...",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("download_all_for_reciter_button"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        border = BorderStroke(1.dp, goldYellow)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DownloadForOffline,
                            contentDescription = null,
                            tint = goldYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Download All",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = containerBg,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = goldYellow
                )
            )
        },
        containerColor = containerBg
    ) { paddingValues ->
        val listState = rememberLazyListState()
        LaunchedEffect(reciterId) {
            listState.scrollToItem(0, 0)
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Reciter Highlight Header Card (Clickable to view downloaded Surahs)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            onNavigateToDownloadedSurahs?.invoke(reciterId)
                        }
                        .testTag("reciter_header_downloaded_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = cardBg
                    ),
                    border = BorderStroke(1.dp, goldYellow.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ReciterImage(
                                reciter = reciter,
                                size = 56.dp,
                                borderWidth = 2.dp,
                                borderColor = goldYellow
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                if (reciter.nameArabic.isNotBlank()) {
                                    Text(
                                        text = reciter.nameArabic,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = ArabicFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                                Text(
                                    text = reciter.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "${reciter.country} • High Quality Audio (MP3)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = goldYellow
                                    )
                                )
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "View Downloaded Surahs",
                                tint = goldYellow
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = goldYellow.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Download Vault Status Strip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (downloadedCount > 0) Icons.Default.CheckCircle else Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = if (downloadedCount > 0) primaryGreen else goldYellow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (downloadedCount > 0) "$downloadedCount Downloaded Surahs (Offline Ready)" else "0 Offline Surahs Saved",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (downloadedCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }

                            Text(
                                text = "View Downloads ›",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = goldYellow
                                )
                            )
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reciter_surahs_search_input"),
                    placeholder = {
                        Text(
                            "Search Surah by name or number (1-114)...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = goldYellow
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = goldYellow
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = goldYellow,
                        unfocusedBorderColor = cardBorder,
                        focusedContainerColor = cardBg,
                        unfocusedContainerColor = cardBg,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            // Surahs Count Summary
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Surahs List (${filteredSurahs.size})",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = goldYellow
                        )
                    )
                    Text(
                        text = "Tap to listen or download",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            // Surah Items
            items(filteredSurahs, key = { it.number }) { surah ->
                val downloadStatus = remember(reciterId, surah.number, downloadStates) {
                    viewModel.getAudioDownloadStatusFor(reciterId, surah.number)
                }
                val isCurrentlyPlaying = currentPlayingSurah == surah.number && isPlaying

                SurahDownloadItemCard(
                    surah = surah,
                    reciterId = reciterId,
                    downloadStatus = downloadStatus,
                    isCurrentlyPlaying = isCurrentlyPlaying,
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    primaryGreen = primaryGreen,
                    goldYellow = goldYellow,
                    isDark = isDark,
                    onItemClick = { onSurahSelected(surah.number) },
                    onDownloadClick = {
                        viewModel.handleAudioDownloadForReciter(reciterId, surah.number)
                    }
                )
            }
        }
    }
}

@Composable
private fun SurahDownloadItemCard(
    surah: Surah,
    reciterId: String,
    downloadStatus: AudioDownloadStatus,
    isCurrentlyPlaying: Boolean,
    cardBg: Color,
    cardBorder: Color,
    primaryGreen: Color,
    goldYellow: Color,
    isDark: Boolean,
    onItemClick: () -> Unit,
    onDownloadClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
            .testTag("reciter_surah_item_${surah.number}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentlyPlaying) MaterialTheme.colorScheme.primaryContainer else cardBg
        ),
        border = BorderStroke(
            if (isCurrentlyPlaying) 1.5.dp else 1.dp,
            if (isCurrentlyPlaying) goldYellow else cardBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentlyPlaying) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Surah Number Diamond/Octagon
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isCurrentlyPlaying) goldYellow else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${surah.number}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrentlyPlaying) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Surah Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = surah.englishName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isCurrentlyPlaying) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Playing",
                            tint = goldYellow,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${surah.numberOfAyahs} Verses • ${surah.revelationType}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            // Arabic Name
            Text(
                text = surah.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = ArabicFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.primary
                ),
                textAlign = TextAlign.End,
                modifier = Modifier.padding(end = 12.dp)
            )

            // Download / Play Action Button
            when (downloadStatus) {
                is AudioDownloadStatus.Downloaded -> {
                    Surface(
                        color = primaryGreen.copy(alpha = 0.2f),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, primaryGreen),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.OfflinePin,
                                contentDescription = "Downloaded Offline",
                                tint = primaryGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                is AudioDownloadStatus.Downloading -> {
                    Surface(
                        onClick = onDownloadClick,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = BorderStroke(1.dp, goldYellow),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { downloadStatus.progress },
                                modifier = Modifier.size(30.dp),
                                color = goldYellow,
                                strokeWidth = 2.5.dp
                            )
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel Download",
                                tint = goldYellow,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                is AudioDownloadStatus.Paused -> {
                    Surface(
                        onClick = onDownloadClick,
                        shape = CircleShape,
                        color = goldYellow.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, goldYellow),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Resume Download",
                                tint = goldYellow,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
                is AudioDownloadStatus.NotDownloaded -> {
                    Surface(
                        onClick = onDownloadClick,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = BorderStroke(1.dp, primaryGreen),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download Surah",
                                tint = primaryGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Navigation Arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
