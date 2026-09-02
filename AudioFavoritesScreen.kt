package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.api.Surah
import com.example.data.model.AudioFavorite
import com.example.data.model.Reciter
import com.example.data.mushaf.IndoPakMushafData
import com.example.ui.QuranViewModel
import com.example.ui.components.ReciterImage
import com.example.ui.theme.ArabicFontFamily
import com.example.ui.theme.QuranGreen
import com.example.ui.theme.QuranYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioFavoritesScreen(
    viewModel: QuranViewModel,
    onPlaySurah: (reciterId: String, surahNumber: Int) -> Unit,
    onNavigateToReciters: () -> Unit = {},
    onBackClick: () -> Unit
) {
    val audioFavorites by viewModel.audioFavorites.collectAsStateWithLifecycle()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // Design Tokens
    val bgColor = MaterialTheme.colorScheme.background
    val topBarBg = MaterialTheme.colorScheme.surface
    val cardBg = if (isDark) Color(0xFF1E242B) else MaterialTheme.colorScheme.surface
    val cardBorder = if (isDark) Color(0xFF2A3642) else Color(0xFFE2E8F0)
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val accentGold = QuranYellow
    val primaryGreen = QuranGreen

    // Group favorites by reciterId
    val groupedFavorites = remember(audioFavorites) {
        audioFavorites.groupBy { it.reciterId }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Favorite Recitations",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                        )
                        Text(
                            text = "پسندیدہ تلاوتیں",
                            fontFamily = ArabicFontFamily,
                            fontSize = 14.sp,
                            color = primaryGreen
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("audio_favorites_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textPrimary
                        )
                    }
                },
                actions = {
                    if (audioFavorites.isNotEmpty()) {
                        Surface(
                            shape = CircleShape,
                            color = accentGold.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, accentGold),
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Text(
                                text = "${audioFavorites.size}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) accentGold else Color(0xFF92400E)
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = topBarBg
                )
            )
        },
        containerColor = bgColor
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (audioFavorites.isEmpty()) {
                // Empty State
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        modifier = Modifier.size(96.dp),
                        shape = CircleShape,
                        color = primaryGreen.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(2.dp, primaryGreen.copy(alpha = 0.3f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.StarBorder,
                                contentDescription = "No Favorites",
                                tint = primaryGreen,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "ابھی کوئی سورت پسندیدہ نہیں",
                        fontFamily = ArabicFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "No favorite recitations saved yet. Tap the ⭐ star icon in the audio player while listening to bookmark your beloved Surahs.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onNavigateToReciters,
                        colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("browse_reciters_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headset,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Browse Reciters / قراء",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                val listState = rememberLazyListState()
                LaunchedEffect(Unit) {
                    listState.scrollToItem(0, 0)
                }

                // List of Reciters with their favorited surahs
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    item {
                        // Summary Banner
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = primaryGreen.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, primaryGreen.copy(alpha = 0.25f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(40.dp),
                                    shape = CircleShape,
                                    color = primaryGreen
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${audioFavorites.size} Saved Recitation${if (audioFavorites.size > 1) "s" else ""}",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = textPrimary
                                    )
                                    Text(
                                        text = "Across ${groupedFavorites.size} Qari${if (groupedFavorites.size > 1) "s" else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = textSecondary
                                    )
                                }
                            }
                        }
                    }

                    // For each Reciter group
                    groupedFavorites.forEach { (reciterId, favoritesForReciter) ->
                        val reciter = viewModel.getReciterById(reciterId)
                        val surahCount = favoritesForReciter.size
                        val countText = "$surahCount Surah${if (surahCount > 1) "s" else ""} Favorited"

                        item(key = "reciter_$reciterId") {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp)
                                ) {
                                    // Reciter Header: Avatar, Name, Count & Play All Button
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        ReciterImage(
                                            reciter = reciter,
                                            size = 54.dp,
                                            borderWidth = 2.dp,
                                            showBorder = true
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = reciter.name,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = textPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (reciter.nameArabic.isNotBlank()) {
                                                Text(
                                                    text = reciter.nameArabic,
                                                    fontFamily = ArabicFontFamily,
                                                    fontSize = 13.sp,
                                                    color = primaryGreen,
                                                    maxLines = 1
                                                )
                                            }
                                            Text(
                                                text = "${reciter.name} — $countText",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = if (isDark) accentGold else Color(0xFF92400E),
                                                    fontWeight = FontWeight.Medium
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        // Play All Button for this reciter
                                        Surface(
                                            onClick = {
                                                val firstFav = favoritesForReciter.firstOrNull()
                                                if (firstFav != null) {
                                                    viewModel.setSelectedReciter(reciterId)
                                                    onPlaySurah(reciterId, firstFav.surahNumber)
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            color = primaryGreen,
                                            modifier = Modifier.testTag("play_all_${reciterId}")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = "Play All",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Play",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = cardBorder.copy(alpha = 0.6f))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // List of Favorited Surahs
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        favoritesForReciter.forEach { fav ->
                                            val surahNum = fav.surahNumber
                                            val surahIdx = (surahNum - 1).coerceIn(0, 113)
                                            val surahArabic = fav.surahNameArabic.ifBlank {
                                                IndoPakMushafData.SURAH_NAMES_ARABIC.getOrElse(surahIdx) { "سورة رقم $surahNum" }
                                            }
                                            val surahEnglish = fav.surahNameEnglish.ifBlank {
                                                IndoPakMushafData.SURAH_NAMES_ENGLISH.getOrElse(surahIdx) { "Surah $surahNum" }
                                            }

                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        viewModel.setSelectedReciter(reciterId)
                                                        onPlaySurah(reciterId, surahNum)
                                                    },
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (isDark) Color(0xFF13181E) else Color(0xFFF8FAFC),
                                                border = androidx.compose.foundation.BorderStroke(
                                                    1.dp,
                                                    if (isDark) Color(0xFF222B36) else Color(0xFFE2E8F0)
                                                )
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Surah Number Badge
                                                    Surface(
                                                        modifier = Modifier.size(34.dp),
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = primaryGreen.copy(alpha = 0.15f),
                                                        border = androidx.compose.foundation.BorderStroke(1.dp, primaryGreen.copy(alpha = 0.3f))
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text(
                                                                text = "$surahNum",
                                                                style = MaterialTheme.typography.labelMedium.copy(
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = primaryGreen
                                                                )
                                                            )
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.width(12.dp))

                                                    // English & Metadata
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = surahEnglish,
                                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                            color = textPrimary,
                                                            maxLines = 1
                                                        )
                                                        Text(
                                                            text = "Surah #$surahNum • ${reciter.name}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = textSecondary,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }

                                                    // Arabic Calligraphy Name
                                                    Text(
                                                        text = surahArabic,
                                                        fontFamily = ArabicFontFamily,
                                                        fontSize = 18.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = primaryGreen,
                                                        modifier = Modifier.padding(horizontal = 6.dp)
                                                    )

                                                    // Play Icon Button
                                                    IconButton(
                                                        onClick = {
                                                            viewModel.setSelectedReciter(reciterId)
                                                            onPlaySurah(reciterId, surahNum)
                                                        },
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .testTag("play_fav_${reciterId}_${surahNum}")
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.PlayCircleFilled,
                                                            contentDescription = "Play",
                                                            tint = primaryGreen,
                                                            modifier = Modifier.size(28.dp)
                                                        )
                                                    }

                                                    // Delete Favorite Button
                                                    IconButton(
                                                        onClick = {
                                                            viewModel.removeAudioFavorite(reciterId, surahNum)
                                                        },
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .testTag("remove_fav_${reciterId}_${surahNum}")
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Star,
                                                            contentDescription = "Remove Favorite",
                                                            tint = accentGold,
                                                            modifier = Modifier.size(24.dp)
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
        }
    }
}
