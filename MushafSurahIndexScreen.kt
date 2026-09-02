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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.mushaf.IndoPakMushafData
import com.example.ui.QuranViewModel
import com.example.ui.SurahsState
import com.example.ui.theme.ArabicFontFamily
import com.example.ui.theme.HandmadeBrushesFontFamily

data class SurahIndexItem(
    val number: Int,
    val englishName: String,
    val arabicName: String,
    val pageNumber: Int,
    val ayahCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MushafSurahIndexScreen(
    viewModel: QuranViewModel,
    onNavigateToPage: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    val surahsState by viewModel.surahsState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // Prepare complete, reliable 114 Surahs dataset
    val allSurahs = remember(surahsState) {
        val apiSurahs = (surahsState as? SurahsState.Success)?.surahs.orEmpty().associateBy { it.number }
        (1..114).map { surahNo ->
            val apiSurah = apiSurahs[surahNo]
            val engName = apiSurah?.englishName ?: IndoPakMushafData.SURAH_NAMES_ENGLISH[surahNo - 1]
            val arabName = IndoPakMushafData.SURAH_NAMES_ARABIC[surahNo - 1]
            val startPage = IndoPakMushafData.getPageForSurah(surahNo)
            val ayahs = apiSurah?.numberOfAyahs ?: IndoPakMushafData.SURAH_AYAH_COUNTS[surahNo - 1]
            SurahIndexItem(
                number = surahNo,
                englishName = engName,
                arabicName = arabName,
                pageNumber = startPage,
                ayahCount = ayahs
            )
        }
    }

    val filteredSurahs = remember(searchQuery, allSurahs) {
        if (searchQuery.isBlank()) {
            allSurahs
        } else {
            val q = searchQuery.trim().lowercase()
            allSurahs.filter { item ->
                item.englishName.lowercase().contains(q) ||
                item.arabicName.contains(q) ||
                item.number.toString() == q ||
                "page ${item.pageNumber}".contains(q) ||
                item.pageNumber.toString() == q
            }
        }
    }

    val cardBgColor = MaterialTheme.colorScheme.surface
    val cardBorderColor = MaterialTheme.colorScheme.outlineVariant
    val badgeBgColor = MaterialTheme.colorScheme.primaryContainer
    val badgeTextColor = MaterialTheme.colorScheme.onPrimaryContainer
    val arabicTitleColor = MaterialTheme.colorScheme.primary
    val primaryGreen = MaterialTheme.colorScheme.primary
    val goldYellow = MaterialTheme.colorScheme.secondary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = primaryGreen
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Al-Quran Majeed",
                    fontFamily = HandmadeBrushesFontFamily,
                    color = primaryGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                Text(
                    text = "Mushaf Surah Index (114)",
                    color = goldYellow,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.size(48.dp))
        }

        // Search TextField Capsule
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            placeholder = { Text("Search Surah by name, page or number...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = primaryGreen)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = cardBgColor,
                unfocusedContainerColor = cardBgColor,
                focusedBorderColor = primaryGreen,
                unfocusedBorderColor = cardBorderColor
            )
        )

        val listState = rememberLazyListState()

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredSurahs, key = { it.number }) { item ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToPage(item.pageNumber) },
                    shape = RoundedCornerShape(16.dp),
                    color = cardBgColor,
                    border = BorderStroke(1.dp, cardBorderColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Number Badge
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = badgeBgColor,
                            border = BorderStroke(1.dp, primaryGreen)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = item.number.toString(),
                                    color = badgeTextColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.englishName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "${item.ayahCount} Ayahs",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "•",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Page ${item.pageNumber}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = goldYellow
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = item.arabicName,
                            fontFamily = ArabicFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = arabicTitleColor
                        )
                    }
                }
            }
        }
    }
}
