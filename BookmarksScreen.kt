package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.BookmarkEntity
import com.example.data.mushaf.IndoPakMushafData
import com.example.ui.QuranViewModel
import com.example.ui.theme.HandmadeBrushesFontFamily
import com.example.ui.theme.IndoPakFontFamily
import com.example.ui.theme.UrduFontFamily

@Composable
fun BookmarksScreen(
    viewModel: QuranViewModel,
    onBackClick: () -> Unit = {},
    onBookmarkClick: (Int) -> Unit = {},
    onPageBookmarkClick: (Int) -> Unit = {}
) {
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    var selectedFilterIndex by remember { mutableIntStateOf(0) } // 0: All, 1: Pages (16 Line), 2: Ayahs

    val filteredBookmarks = remember(bookmarks, selectedFilterIndex) {
        when (selectedFilterIndex) {
            1 -> bookmarks.filter { it.pageNumber > 0 }
            2 -> bookmarks.filter { it.pageNumber == 0 }
            else -> bookmarks
        }
    }

    val pageBookmarksCount = remember(bookmarks) { bookmarks.count { it.pageNumber > 0 } }
    val ayahBookmarksCount = remember(bookmarks) { bookmarks.count { it.pageNumber == 0 } }

    val bgVal = MaterialTheme.colorScheme.background
    val isDark = (bgVal.red * 0.299f + bgVal.green * 0.587f + bgVal.blue * 0.114f) < 0.5f

    val cardBgColor = if (isDark) Color(0xFF13221C) else MaterialTheme.colorScheme.surface
    val cardBorderColor = if (isDark) Color(0xFF1E3A2E) else Color(0xFFD0E8DC)
    val badgeBgColor = if (isDark) Color(0xFF1B382C) else Color(0xFFE8F5EE)
    val badgeTextColor = if (isDark) Color(0xFF81E6BB) else Color(0xFF046A38)
    val primaryGreen = if (isDark) Color(0xFF6EE7B7) else Color(0xFF046A38)
    val goldYellow = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgVal)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Top Header Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = primaryGreen
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Al-Quran Majeed",
                    fontFamily = HandmadeBrushesFontFamily,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryGreen,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Bookmarks (محفوظ شدہ)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = goldYellow,
                        fontFamily = UrduFontFamily
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Filter Tabs / Chips
        if (bookmarks.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilterIndex == 0,
                    onClick = { selectedFilterIndex = 0 },
                    label = { Text("All (${bookmarks.size})") }
                )
                FilterChip(
                    selected = selectedFilterIndex == 1,
                    onClick = { selectedFilterIndex = 1 },
                    label = { Text("16-Line Pages ($pageBookmarksCount)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
                FilterChip(
                    selected = selectedFilterIndex == 2,
                    onClick = { selectedFilterIndex = 2 },
                    label = { Text("Ayahs ($ayahBookmarksCount)") }
                )
            }
        }

        if (filteredBookmarks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.widthIn(max = 320.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(88.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.BookmarkAdd,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = if (bookmarks.isEmpty()) "No bookmarks yet" else "No matching bookmarks",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "16-Line Mushaf par parhte waqt neechay bookmark button dabayein taake page yahan save ho jaye.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { onPageBookmarkClick(1) },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Open 16-Line Quran",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }
        } else {
            val listState = rememberLazyListState()
            LaunchedEffect(Unit) {
                listState.scrollToItem(0, 0)
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredBookmarks, key = { it.id }) { bookmark ->
                    if (bookmark.pageNumber > 0) {
                        // 16-Line Page Bookmark Item
                        PageBookmarkItem(
                            bookmark = bookmark,
                            onClick = { onPageBookmarkClick(bookmark.pageNumber.coerceIn(1, IndoPakMushafData.TOTAL_PAGES)) },
                            onDelete = { viewModel.deleteBookmark(bookmark) }
                        )
                    } else {
                        // Ayah Bookmark Item
                        AyahBookmarkItem(
                            bookmark = bookmark,
                            onClick = { onBookmarkClick(bookmark.surahNumber) },
                            onDelete = { viewModel.deleteBookmark(bookmark) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PageBookmarkItem(
    bookmark: BookmarkEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val bgVal = MaterialTheme.colorScheme.background
    val isDark = (bgVal.red * 0.299f + bgVal.green * 0.587f + bgVal.blue * 0.114f) < 0.5f

    val cardBgColor = if (isDark) Color(0xFF13221C) else MaterialTheme.colorScheme.surface
    val cardBorderColor = if (isDark) Color(0xFF1E3A2E) else Color(0xFFD0E8DC)
    val badgeBgColor = if (isDark) Color(0xFF1B382C) else Color(0xFFE8F5EE)
    val primaryGreen = if (isDark) Color(0xFF6EE7B7) else Color(0xFF046A38)
    val goldYellow = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)

    val arabicPageDigits = IndoPakMushafData.toArabicDigits(bookmark.pageNumber)
    val pageInfo = remember(bookmark.pageNumber) { IndoPakMushafData.getPageInfo(bookmark.pageNumber) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = cardBgColor,
        border = BorderStroke(1.dp, cardBorderColor),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Icon + Page badge + Subtitle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = badgeBgColor,
                    border = BorderStroke(1.dp, primaryGreen),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = "16-Line Page",
                            tint = primaryGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = goldYellow.copy(alpha = 0.15f),
                            border = BorderStroke(0.8.dp, goldYellow)
                        ) {
                            Text(
                                text = "16-Line Page ${bookmark.pageNumber}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = goldYellow
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${pageInfo.juzNameArabic} • Surah ${bookmark.surahNumber}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            // Right: Arabic Calligraphy Title & Delete action
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "صفحہ $arabicPageDigits",
                        fontFamily = IndoPakFontFamily,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryGreen
                    )
                    Text(
                        text = pageInfo.surahNameArabic,
                        fontFamily = IndoPakFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldYellow
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Bookmark",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AyahBookmarkItem(
    bookmark: BookmarkEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val bgVal = MaterialTheme.colorScheme.background
    val isDark = (bgVal.red * 0.299f + bgVal.green * 0.587f + bgVal.blue * 0.114f) < 0.5f

    val cardBgColor = if (isDark) Color(0xFF13221C) else MaterialTheme.colorScheme.surface
    val cardBorderColor = if (isDark) Color(0xFF1E3A2E) else Color(0xFFD0E8DC)
    val primaryGreen = if (isDark) Color(0xFF6EE7B7) else Color(0xFF046A38)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = cardBgColor,
        border = BorderStroke(1.dp, cardBorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${bookmark.surahName} • Ayah ${bookmark.ayahNumberInSurah}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = primaryGreen
                    )
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Bookmark",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Text(
                    text = com.example.util.QuranSanitizer.cleanAyahArabic(
                        bookmark.arabicText,
                        bookmark.surahNumber,
                        bookmark.ayahNumberInSurah
                    ),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = IndoPakFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        lineHeight = 36.sp
                    ),
                    maxLines = 2,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (bookmark.urduTranslation.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = com.example.util.QuranSanitizer.cleanAyahUrdu(
                            bookmark.urduTranslation,
                            bookmark.surahNumber,
                            bookmark.ayahNumberInSurah
                        ),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = UrduFontFamily,
                            fontSize = 15.sp,
                            lineHeight = 26.sp
                        ),
                        maxLines = 2,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
