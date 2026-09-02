package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArabicFontFamily

data class HadithBookItem(
    val id: String,
    val title: String,
    val arabicTitle: String,
    val totalChapters: Int,
    val isDownloaded: Boolean
)

val sampleHadithBooks = listOf(
    HadithBookItem("bukhari", "Al-Bukhari", "صحيح البخاري", 97, true),
    HadithBookItem("muslim", "Al-Muslim", "صحيح مسلم", 56, true),
    HadithBookItem("tirmidhi", "Al-Tirmazi", "جامع الترمذي", 50, false),
    HadithBookItem("abudawud", "Abu Dawood", "سنن أبي داود", 43, true),
    HadithBookItem("nasai", "Al-Nasai", "سنن النسائي", 52, true),
    HadithBookItem("ibnmajah", "Sunnan e Ibn e Maja", "سنن ابن ماجه", 37, false)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HadithBooksScreen(
    onBookClick: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var booksList by remember { mutableStateOf(sampleHadithBooks) }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bg = MaterialTheme.colorScheme.background
    val cardBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White
    val cardBorder = if (isDark) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f) else Color(0xFFD0C5AF).copy(alpha = 0.5f)
    val goldColor = MaterialTheme.colorScheme.secondary
    val primaryText = MaterialTheme.colorScheme.onBackground
    val mutedText = MaterialTheme.colorScheme.onSurfaceVariant
    val emeraldContainer = MaterialTheme.colorScheme.primaryContainer

    val filteredBooks = booksList.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                it.arabicTitle.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        containerColor = bg,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                // Support Banner
                Surface(
                    color = emeraldContainer.copy(alpha = if (isDark) 0.3f else 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = goldColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Support the spread of Quranic knowledge.",
                                fontSize = 12.5.sp,
                                color = mutedText,
                                maxLines = 1
                            )
                        }
                        Surface(
                            onClick = {
                                Toast.makeText(context, "JazakAllah Khair for your support!", Toast.LENGTH_SHORT).show()
                            },
                            shape = CircleShape,
                            color = goldColor
                        ) {
                            Text(
                                text = "Support",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFF131410) else Color.White,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = cardBorder)

                // Main Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = goldColor
                        )
                    }

                    Text(
                        text = "Al-Hadith",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldColor
                    )

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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Hadith books...", color = mutedText, fontSize = 14.sp) },
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

            // Books Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 90.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredBooks) { book ->
                    HadithBookCard(
                        book = book,
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        goldColor = goldColor,
                        primaryText = primaryText,
                        mutedText = mutedText,
                        onCardClick = { onBookClick(book.id) },
                        onToggleDownload = {
                            booksList = booksList.map {
                                if (it.id == book.id) it.copy(isDownloaded = !it.isDownloaded) else it
                            }
                            val msg = if (!book.isDownloaded) "${book.title} downloaded" else "Data removed for ${book.title}"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HadithBookCard(
    book: HadithBookItem,
    cardBg: Color,
    cardBorder: Color,
    goldColor: Color,
    primaryText: Color,
    mutedText: Color,
    onCardClick: () -> Unit,
    onToggleDownload: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder),
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Calligraphy Graphic Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(goldColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = null,
                        tint = goldColor,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = book.arabicTitle,
                        fontSize = 17.sp,
                        fontFamily = ArabicFontFamily,
                        color = goldColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = book.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = primaryText,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Button
            Row(
                modifier = Modifier
                    .clickable { onToggleDownload() }
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (book.isDownloaded) Icons.Default.Delete else Icons.Default.Download,
                    contentDescription = null,
                    tint = if (book.isDownloaded) Color(0xFFEF4444) else goldColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (book.isDownloaded) "Remove data" else "Download",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (book.isDownloaded) Color(0xFFEF4444) else goldColor
                )
            }
        }
    }
}
