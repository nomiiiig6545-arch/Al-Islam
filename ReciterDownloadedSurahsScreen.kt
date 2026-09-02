package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
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
import com.example.data.mushaf.IndoPakMushafData
import com.example.ui.QuranViewModel
import com.example.ui.SurahsState
import com.example.ui.components.ReciterImage
import com.example.ui.theme.ArabicFontFamily
import com.example.ui.theme.HandmadeBrushesFontFamily
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReciterDownloadedSurahsScreen(
    reciterId: String,
    viewModel: QuranViewModel,
    onSurahSelected: (Int) -> Unit,
    onNavigateToBrowseAll: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val reciter = remember(reciterId) {
        viewModel.getReciterById(reciterId)
    }

    val surahsState by viewModel.surahsState.collectAsStateWithLifecycle()
    val downloadStates by viewModel.audioDownloadStates.collectAsStateWithLifecycle()
    val currentPlayingSurah by viewModel.currentOfflineSurahNumber.collectAsStateWithLifecycle()
    val currentReciterId by viewModel.currentOfflineReciterId.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlayerPlaying.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var surahToDelete by remember { mutableStateOf<Int?>(null) }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // Dynamic Theme Tokens
    val containerBg = MaterialTheme.colorScheme.background
    val cardBg = MaterialTheme.colorScheme.surface
    val cardBorder = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val primaryGreen = MaterialTheme.colorScheme.primary
    val goldYellow = MaterialTheme.colorScheme.secondary

    // Query downloaded surah numbers
    val downloadedSurahNumbers = remember(reciterId, downloadStates) {
        viewModel.getDownloadedSurahs(reciterId)
    }

    val totalDownloadedBytes = remember(reciterId, downloadStates) {
        viewModel.getTotalDownloadedAudioBytes(reciterId)
    }

    val formattedTotalSize = remember(totalDownloadedBytes) {
        val mb = totalDownloadedBytes.toDouble() / (1024 * 1024)
        if (mb >= 1024) {
            String.format(Locale.US, "%.2f GB", mb / 1024)
        } else {
            String.format(Locale.US, "%.1f MB", mb)
        }
    }

    // Map numbers to Surah objects
    val allSurahsMap = remember(surahsState) {
        val list = when (val state = surahsState) {
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
        list.associateBy { it.number }
    }

    val downloadedSurahsList = remember(downloadedSurahNumbers, allSurahsMap) {
        downloadedSurahNumbers.mapNotNull { num -> allSurahsMap[num] }
    }

    val filteredSurahs = remember(searchQuery, downloadedSurahsList) {
        if (searchQuery.isBlank()) {
            downloadedSurahsList
        } else {
            downloadedSurahsList.filter { surah ->
                surah.number.toString().contains(searchQuery) ||
                surah.englishName.contains(searchQuery, ignoreCase = true) ||
                surah.name.contains(searchQuery, ignoreCase = true) ||
                surah.englishNameTranslation.contains(searchQuery, ignoreCase = true)
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
                            text = "Downloaded Surahs • ${reciter.name} (${downloadedSurahsList.size}/114)",
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
                        modifier = Modifier.testTag("reciter_downloaded_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = goldYellow
                        )
                    }
                },
                actions = {
                    if (downloadedSurahsList.isNotEmpty()) {
                        IconButton(
                            onClick = { showDeleteAllDialog = true },
                            modifier = Modifier.testTag("delete_all_reciter_downloads_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete all downloads",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
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
            // Reciter Profile Header & Download Statistics Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = cardBg
                    ),
                    border = BorderStroke(1.dp, goldYellow.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ReciterImage(
                                reciter = reciter,
                                size = 64.dp,
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
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "${reciter.country} • Offline Audio Vault",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = goldYellow
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = primaryGreen.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Stats Highlights Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Downloaded Surahs Count
                            Column {
                                Text(
                                    text = "DOWNLOADED",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Text(
                                    text = "${downloadedSurahsList.size} / 114 Surahs",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = primaryGreen
                                    )
                                )
                            }

                            // Storage Footprint
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "OFFLINE STORAGE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Text(
                                    text = formattedTotalSize,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = goldYellow
                                    )
                                )
                            }

                            // Status Tag
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (downloadedSurahsList.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, if (downloadedSurahsList.isNotEmpty()) goldYellow.copy(alpha = 0.5f) else cardBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (downloadedSurahsList.isNotEmpty()) Icons.Default.CheckCircle else Icons.Default.CloudDownload,
                                        contentDescription = null,
                                        tint = if (downloadedSurahsList.isNotEmpty()) primaryGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (downloadedSurahsList.size == 114) "Full Quran" else if (downloadedSurahsList.isNotEmpty()) "Offline Ready" else "No Downloads",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (downloadedSurahsList.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }

                        if (downloadedSurahsList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Play All Button
                                Button(
                                    onClick = {
                                        downloadedSurahsList.firstOrNull()?.let { firstSurah ->
                                            onSurahSelected(firstSurah.number)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("play_all_downloaded_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    border = BorderStroke(1.dp, goldYellow),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = goldYellow,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Play Offline",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    )
                                }

                                // Download Remaining Button
                                if (downloadedSurahsList.size < 114) {
                                    Button(
                                        onClick = {
                                            viewModel.downloadAllSurahsForReciter(reciterId) {
                                                Toast.makeText(
                                                    context,
                                                    "Downloading remaining Surahs for ${reciter.name}...",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        border = BorderStroke(1.dp, goldYellow),
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = goldYellow
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Download All",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // If no downloaded Surahs yet
            if (downloadedSurahsList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(1.dp, cardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                border = BorderStroke(1.dp, goldYellow),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDownload,
                                        contentDescription = null,
                                        tint = goldYellow,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "No Surahs Downloaded Yet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Download Surahs for ${reciter.name} to listen offline anytime in Airplane Mode without internet.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = onNavigateToBrowseAll,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                border = BorderStroke(1.dp, goldYellow)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FormatListNumbered,
                                    contentDescription = null,
                                    tint = goldYellow,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Browse & Download 114 Surahs", color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            } else {
                // Search Bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("downloaded_surahs_search_input"),
                        placeholder = {
                            Text(
                                "Search in downloaded Surahs (${downloadedSurahsList.size})...",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
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

                // Section Title
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Offline Available (${filteredSurahs.size})",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = goldYellow
                            )
                        )
                        TextButton(
                            onClick = onNavigateToBrowseAll,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = "View All 114 Surahs ›",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = goldYellow,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                // Downloaded Surahs List
                items(filteredSurahs, key = { it.number }) { surah ->
                    val isCurrentTrack = currentPlayingSurah == surah.number && currentReciterId == reciterId
                    val fileSize = remember(reciterId, surah.number) {
                        viewModel.getDownloadedSurahFileSize(reciterId, surah.number)
                    }
                    val formattedFileSize = remember(fileSize) {
                        val mb = fileSize.toDouble() / (1024 * 1024)
                        String.format(Locale.US, "%.1f MB", mb)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSurahSelected(surah.number) }
                            .testTag("downloaded_surah_item_${surah.number}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrentTrack) MaterialTheme.colorScheme.primaryContainer else cardBg
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isCurrentTrack) goldYellow else cardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Surah Number Badge
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isCurrentTrack) goldYellow else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isCurrentTrack && isPlaying) {
                                        Icon(
                                            imageVector = Icons.Default.GraphicEq,
                                            contentDescription = "Playing",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else {
                                        Text(
                                            text = surah.number.toString(),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCurrentTrack) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // English & Revelation Details
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = surah.englishName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "${surah.numberOfAyahs} Verses • ${surah.revelationType}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Text(
                                        text = "•",
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                    Text(
                                        text = formattedFileSize,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = goldYellow,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }

                            // Arabic Name
                            Text(
                                text = surah.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = ArabicFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            // Play / Delete Actions
                            IconButton(
                                onClick = { onSurahSelected(surah.number) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isCurrentTrack && isPlaying) Icons.Default.PauseCircleFilled else Icons.Default.PlayCircleFilled,
                                    contentDescription = "Play",
                                    tint = goldYellow,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            IconButton(
                                onClick = { surahToDelete = surah.number },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete downloaded Surah",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog to Delete a Single Surah
    surahToDelete?.let { surahNum ->
        val surahName = IndoPakMushafData.SURAH_NAMES_ENGLISH.getOrElse(surahNum - 1) { "Surah $surahNum" }
        AlertDialog(
            onDismissRequest = { surahToDelete = null },
            containerColor = cardBg,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Delete Downloaded Audio?") },
            text = {
                Text("Do you want to remove the offline audio file for $surahName (${reciter.name})? You can re-download it anytime.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteDownloadedSurah(reciterId, surahNum)
                        Toast.makeText(context, "Deleted offline audio for $surahName", Toast.LENGTH_SHORT).show()
                        surahToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { surahToDelete = null }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // Confirmation Dialog to Delete All Surahs for this Reciter
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            containerColor = cardBg,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Delete All Offline Surahs?") },
            text = {
                Text("Are you sure you want to remove all ${downloadedSurahsList.size} downloaded Surahs for ${reciter.name} ($formattedTotalSize)?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAllDownloadedSurahsForReciter(reciterId)
                        Toast.makeText(context, "All downloaded audio cleared for ${reciter.name}", Toast.LENGTH_SHORT).show()
                        showDeleteAllDialog = false
                    }
                ) {
                    Text("Delete All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}
