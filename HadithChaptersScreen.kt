package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArabicFontFamily

data class HadithChapterItem(
    val id: Int,
    val arabicTitle: String,
    val startHadith: Int,
    val endHadith: Int
)

val sampleBukhariChapters = listOf(
    HadithChapterItem(1, "كتاب بدء الوحى", 1, 7),
    HadithChapterItem(2, "كتاب الإيمان", 8, 58),
    HadithChapterItem(3, "كتاب العلم", 59, 134),
    HadithChapterItem(4, "كتاب الوضوء", 135, 247),
    HadithChapterItem(5, "كتاب الغسل", 248, 293),
    HadithChapterItem(6, "كتاب الحيض", 294, 333),
    HadithChapterItem(7, "كتاب التيمم", 334, 348),
    HadithChapterItem(8, "كتاب الصلاة", 349, 520),
    HadithChapterItem(9, "كتاب مواقيت الصلاة", 521, 602),
    HadithChapterItem(10, "كتاب الأذان", 603, 875)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HadithChaptersScreen(
    bookId: String,
    onChapterClick: (Int) -> Unit,
    onNavigateToSettings: () -> Unit,
    onBackClick: () -> Unit
) {
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val bookTitle = when (bookId) {
        "muslim" -> "Sahih Muslim"
        "tirmidhi" -> "Jami at-Tirmidhi"
        "abudawud" -> "Sunan Abi Dawud"
        "nasai" -> "Sunan an-Nasa'i"
        "ibnmajah" -> "Sunan Ibn Majah"
        else -> "Sahih al-Bukhari"
    }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bg = MaterialTheme.colorScheme.background
    val cardBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White
    val cardBorder = if (isDark) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f) else Color(0xFFD0C5AF).copy(alpha = 0.5f)
    val goldColor = MaterialTheme.colorScheme.secondary
    val primaryText = MaterialTheme.colorScheme.onBackground
    val mutedText = MaterialTheme.colorScheme.onSurfaceVariant

    val filteredChapters = sampleBukhariChapters.filter {
        it.arabicTitle.contains(searchQuery, ignoreCase = true) ||
                it.id.toString().contains(searchQuery)
    }

    Scaffold(
        containerColor = bg,
        topBar = {
            Surface(
                color = bg.copy(alpha = 0.95f),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = goldColor
                        )
                    }

                    Text(
                        text = "Kitab",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldColor
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { isSearching = !isSearching }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = goldColor
                            )
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = goldColor
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            if (isSearching) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search chapters...", color = mutedText, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = goldColor)
                    },
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = goldColor,
                        unfocusedBorderColor = cardBorder,
                        focusedContainerColor = cardBg,
                        unfocusedContainerColor = cardBg
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Chapters",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = bookTitle,
                        fontSize = 15.sp,
                        color = mutedText
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = goldColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, goldColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "${sampleBukhariChapters.size} Chapters",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // List of Chapters
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 90.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(filteredChapters) { _, chapter ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChapterClick(chapter.id) },
                        shape = RoundedCornerShape(16.dp),
                        color = cardBg,
                        border = BorderStroke(1.dp, cardBorder),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Chapter Number Box
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        color = goldColor.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = chapter.id.toString(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = goldColor
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Arabic Title and Hadith Count (RTL Layout)
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = chapter.arabicTitle,
                                    fontSize = 22.sp,
                                    fontFamily = ArabicFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryText,
                                    textAlign = TextAlign.End
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoStories,
                                        contentDescription = null,
                                        tint = mutedText,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "[${chapter.startHadith} - ${chapter.endHadith}]",
                                        fontSize = 13.sp,
                                        color = mutedText
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
