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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AVAILABLE_RECITERS
import com.example.data.model.Reciter
import com.example.ui.QuranViewModel
import com.example.ui.components.ReciterImage
import com.example.ui.theme.ArabicFontFamily
import com.example.ui.theme.HandmadeBrushesFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecitersScreen(
    viewModel: QuranViewModel,
    onReciterSelected: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val downloadStates by viewModel.audioDownloadStates.collectAsStateWithLifecycle()

    val containerBg = Color(0xFF0B2219)
    val cardBg = Color(0xFF0E241B)
    val cardBorder = Color(0xFF1A4030)
    val primaryGreen = Color(0xFF10B981)
    val goldYellow = Color(0xFFD4AF37)
    val activeCardBg = Color(0xFF0F5233)

    val filteredReciters = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            AVAILABLE_RECITERS
        } else {
            AVAILABLE_RECITERS.filter {
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
                            color = Color.White
                        )
                        Text(
                            text = "Famous Quran Reciters (مشاهير القراء)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = goldYellow
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("reciters_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = goldYellow
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = containerBg,
                    titleContentColor = Color.White,
                    navigationIconContentColor = goldYellow
                )
            )
        },
        containerColor = containerBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 14.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reciters_search_input"),
                placeholder = {
                    Text(
                        "Search Reciter (e.g., Sudais, Alafasy)...",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.6f))
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = primaryGreen
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
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = goldYellow,
                    unfocusedBorderColor = cardBorder,
                    focusedContainerColor = cardBg,
                    unfocusedContainerColor = cardBg,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Information Banner
            Surface(
                color = activeCardBg,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, goldYellow.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = null,
                        tint = goldYellow,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Tap on any Qari to view 114 Surahs, download offline audio, and start playing.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reciters Grid strictly enforcing 2 columns
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredReciters, key = { it.id }) { reciter ->
                    val downloadedCount = remember(reciter.id, downloadStates) {
                        viewModel.getDownloadedSurahsCount(reciter.id)
                    }

                    ReciterCard(
                        reciter = reciter,
                        downloadedCount = downloadedCount,
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        primaryGreen = primaryGreen,
                        goldYellow = goldYellow,
                        isDark = true,
                        onClick = { onReciterSelected(reciter.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReciterCard(
    reciter: Reciter,
    downloadedCount: Int,
    cardBg: Color,
    cardBorder: Color,
    primaryGreen: Color,
    goldYellow: Color,
    isDark: Boolean,
    onClick: () -> Unit
) {
    // Generate an attractive Islamic avatar gradient
    val avatarGradients = remember(reciter.id) {
        when ((reciter.id.hashCode() % 4 + 4) % 4) {
            0 -> listOf(Color(0xFF1B4D3E), Color(0xFF0F3026))
            1 -> listOf(Color(0xFF2C5E4E), Color(0xFF143B2E))
            2 -> listOf(Color(0xFF2E6152), Color(0xFF1B3D33))
            else -> listOf(Color(0xFF234B3D), Color(0xFF0C241C))
        }
    }

    val initials = remember(reciter.name) {
        reciter.name.split(" ")
            .filter { it.isNotBlank() && !it.equals("Ibn", ignoreCase = true) && !it.equals("Al-", ignoreCase = true) }
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .ifEmpty { "QR" }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("reciter_card_${reciter.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circular Photo / Portrait with Gold Ring
            ReciterImage(
                reciter = reciter,
                size = 76.dp,
                borderWidth = 2.dp,
                borderColor = goldYellow.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Arabic Name
            if (reciter.nameArabic.isNotBlank()) {
                Text(
                    text = reciter.nameArabic,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = ArabicFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFBAEED9) else primaryGreen
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // English Name
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

            // Subtext / Country
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

            // Offline Download Count Badge
            Surface(
                color = if (downloadedCount > 0) primaryGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(
                    0.5.dp,
                    if (downloadedCount > 0) primaryGreen.copy(alpha = 0.5f) else Color.Transparent
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (downloadedCount > 0) Icons.Default.OfflinePin else Icons.Default.FileDownload,
                        contentDescription = null,
                        tint = if (downloadedCount > 0) primaryGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (downloadedCount > 0) "$downloadedCount Downloaded" else "114 Surahs",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (downloadedCount > 0) primaryGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontSize = 10.5.sp
                        )
                    )
                }
            }
        }
    }
}
