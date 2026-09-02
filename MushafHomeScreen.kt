package com.example.ui.screens

import androidx.activity.compose.BackHandler
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.mushaf.IndoPakMushafData
import com.example.ui.QuranViewModel
import com.example.ui.theme.ArabicFontFamily
import com.example.ui.theme.HandmadeBrushesFontFamily
import com.example.ui.theme.IndoPakFontFamily
import com.example.ui.theme.UrduFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MushafHomeScreen(
    viewModel: QuranViewModel,
    onNavigateToMushafPage: (Int) -> Unit,
    onNavigateToJuzIndex: () -> Unit,
    onNavigateToSurahIndex: () -> Unit,
    onNavigateToBookmarks: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var showJumpSheet by remember { mutableStateOf(false) }
    var jumpPageInput by remember { mutableStateOf("") }
    
    val lastReadPage by viewModel.lastReadMushafPage.collectAsState(initial = 1)

    BackHandler {
        onBackClick()
    }

    val scrollState = rememberScrollState()
    LaunchedEffect(Unit) {
        scrollState.scrollTo(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Design System Theme Tokens (Matching Audio Quran with Tarjama screen)
        val headerBgColor = MaterialTheme.colorScheme.primary
        val headerBorderColor = MaterialTheme.colorScheme.secondary
        val cardBgColor = MaterialTheme.colorScheme.surface
        val cardBorderColor = MaterialTheme.colorScheme.outlineVariant
        val badgeBgColor = MaterialTheme.colorScheme.primaryContainer
        val primaryGreen = MaterialTheme.colorScheme.primary
        val titleColor = MaterialTheme.colorScheme.onSurface
        val goldYellow = MaterialTheme.colorScheme.secondary

        // Top Header Bar with Back Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = primaryGreen,
                    modifier = Modifier.size(24.dp)
                )
            }

            Box(modifier = Modifier.size(44.dp)) // Spacer to keep title perfectly centered
        }

        // Grand Calligraphic Header Section
        val isDark = MaterialTheme.colorScheme.background.red * 0.299f +
                     MaterialTheme.colorScheme.background.green * 0.587f +
                     MaterialTheme.colorScheme.background.blue * 0.114f < 0.5f

        val headerBg = if (isDark) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary
        val headerTitleColor = if (isDark) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary
        val headerAccentColor = MaterialTheme.colorScheme.secondary

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(20.dp),
            color = headerBg,
            border = BorderStroke(1.5.dp, headerAccentColor),
            shadowElevation = 4.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Decorative Background Watermark Icon
                Icon(
                    imageVector = Icons.Default.AutoStories,
                    contentDescription = null,
                    tint = headerTitleColor.copy(alpha = 0.08f),
                    modifier = Modifier
                        .size(110.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 20.dp, y = (-20).dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Al-Quran Majeed",
                        fontFamily = HandmadeBrushesFontFamily,
                        fontSize = 35.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = headerTitleColor,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )

                    // Decorative Gold Divider & Star Ornaments
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(1.5.dp)
                                .background(headerAccentColor)
                        )
                        Text(
                            text = "✦",
                            fontSize = 12.sp,
                            color = headerAccentColor
                        )
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(1.5.dp)
                                .background(headerAccentColor)
                        )
                    }

                    Text(
                        text = "16 Line Tajweed Colour-Coded Mushaf",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = headerAccentColor
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Text(
            text = "Continue your spiritual journey",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Resume Button Card matching Home Screen card design
        val resumeCardBg = if (isDark) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary
        val resumeCardBorder = if (isDark) MaterialTheme.colorScheme.outlineVariant else headerAccentColor
        val resumeTitleColor = if (isDark) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary
        val resumeIconBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.Black.copy(alpha = 0.15f)
        val resumeIconTint = if (isDark) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToMushafPage(lastReadPage.coerceIn(1, IndoPakMushafData.TOTAL_PAGES)) },
            shape = RoundedCornerShape(20.dp),
            color = resumeCardBg,
            border = BorderStroke(1.5.dp, resumeCardBorder),
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = resumeIconBg,
                    border = BorderStroke(1.dp, resumeIconTint.copy(alpha = 0.6f)),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Resume",
                            tint = resumeIconTint,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Resume Reading",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = resumeTitleColor
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = resumeIconTint.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, resumeIconTint)
                ) {
                    Text(
                        text = "Page $lastReadPage",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = resumeIconTint
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Bento Grid for Navigation Options
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Juz Index
                BentoNavCard(
                    title = "Juz Index",
                    icon = Icons.Default.FormatListNumbered,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToJuzIndex
                )

                // Surah Index
                BentoNavCard(
                    title = "Surah Index",
                    icon = Icons.Default.ViewList,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToSurahIndex
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Go to page #
                BentoNavCard(
                    title = "Go to page #",
                    icon = Icons.Default.FindInPage,
                    modifier = Modifier.weight(1f),
                    onClick = { showJumpSheet = true }
                )

                // Bookmarks
                BentoNavCard(
                    title = "Bookmarks",
                    icon = Icons.Default.Bookmark,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToBookmarks
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // WorkManager-based Background Offline Storage Card
            val downloadManager = remember { com.example.data.mushaf.MushafPageDownloadManager.getInstance(context) }
            val downloadProgress by downloadManager.downloadProgress.collectAsState()
            val downloadedPagesCount by viewModel.downloadedMushafPagesCount.collectAsState()
            val totalStorageBytes by viewModel.totalMushafStorageBytes.collectAsState()
            
            val effectiveDownloadedCount = IndoPakMushafData.TOTAL_PAGES
            val isFullOffline = true

            val coroutineScope = rememberCoroutineScope()

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isFullOffline) Color(0xFF2E7D32).copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isFullOffline) Icons.Default.CheckCircle else Icons.Default.AirplanemodeActive,
                                        contentDescription = "Offline Status",
                                        tint = if (isFullOffline) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = if (isFullOffline) "16-Line Mushaf Saved" else "Offline Storage (549 Pages)",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isFullOffline) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                                Text(
                                    text = if (isFullOffline) {
                                        "All ${IndoPakMushafData.TOTAL_PAGES} pages stored locally (${downloadProgress.formattedStorageSize})"
                                    } else {
                                        "$effectiveDownloadedCount / ${IndoPakMushafData.TOTAL_PAGES} pages saved locally (${downloadProgress.formattedStorageSize})"
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        if (!isFullOffline && !downloadProgress.isDownloading) {
                            val bg = MaterialTheme.colorScheme.background
                            val isDark = (bg.red * 0.299f + bg.green * 0.587f + bg.blue * 0.114f) < 0.5f
                            Button(
                                onClick = {
                                    downloadManager.startDownloadAll()
                                    Toast.makeText(context, "Started background download for all 549 pages...", Toast.LENGTH_SHORT).show()
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDark) Color(0xFF0F5238) else MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White
                                ),
                                border = BorderStroke(1.dp, if (isDark) Color(0xFF34D399) else Color(0xFF046A38)),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = "Download All",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Download All",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    if (downloadProgress.isDownloading || downloadProgress.isPaused) {
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { downloadProgress.progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = if (downloadProgress.isPaused) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (downloadProgress.isPaused) {
                                    "Paused at page ${downloadProgress.currentDownloadingPage} (${downloadProgress.progressPercentage}%)"
                                } else {
                                    "Saving page ${downloadProgress.currentDownloadingPage} (${downloadProgress.progressPercentage}%)"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (downloadProgress.isPaused) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (downloadProgress.isPaused) {
                                    TextButton(
                                        onClick = { downloadManager.resumeDownload() },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                    ) {
                                        Text("Resume", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                } else {
                                    TextButton(
                                        onClick = { downloadManager.pauseDownload() },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                    ) {
                                        Text("Pause", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                    }
                                }
                                TextButton(
                                    onClick = { downloadManager.cancelDownload() },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) {
                                    Text("Cancel", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showJumpSheet) {
        ModalBottomSheet(
            onDismissRequest = { showJumpSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.FindInPage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Jump to Page",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Enter a page number between 1 and 549 to quickly navigate to specific verses in the Mushaf.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = jumpPageInput,
                    onValueChange = { jumpPageInput = it },
                    placeholder = { Text("Page Number (1-549)", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            jumpPageInput = ""
                            showJumpSheet = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            val target = jumpPageInput.toIntOrNull()?.coerceIn(1, 549)
                            if (target != null) {
                                showJumpSheet = false
                                onNavigateToMushafPage(target)
                            } else {
                                Toast.makeText(context, "Enter valid page between 1 and 549", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Jump", fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "QUICK JUMP",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickJumpChip(title = "Page 1", subtitle = "Al-Fatihah", page = 1, modifier = Modifier.weight(1f)) {
                            showJumpSheet = false
                            onNavigateToMushafPage(1)
                        }
                        QuickJumpChip(title = "Page 265", subtitle = "Al-Kahf", page = 265, modifier = Modifier.weight(1f)) {
                            showJumpSheet = false
                            onNavigateToMushafPage(265)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickJumpChip(title = "Page 529", subtitle = "Juz 30", page = 529, modifier = Modifier.weight(1f)) {
                            showJumpSheet = false
                            onNavigateToMushafPage(529)
                        }
                        QuickJumpChip(title = "Page 549", subtitle = "An-Nas", page = 549, modifier = Modifier.weight(1f)) {
                            showJumpSheet = false
                            onNavigateToMushafPage(549)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BentoNavCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.red * 0.299f +
                 MaterialTheme.colorScheme.background.green * 0.587f +
                 MaterialTheme.colorScheme.background.blue * 0.114f < 0.5f

    val cardBg = if (isDark) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary
    val cardBorder = if (isDark) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.secondary
    val iconCircleBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.Black.copy(alpha = 0.15f)
    val iconTint = if (isDark) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    val textColor = if (isDark) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary

    Surface(
        modifier = modifier
            .height(110.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder),
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = iconCircleBg,
                border = BorderStroke(1.dp, iconTint.copy(alpha = 0.6f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontSize = 13.5.sp
                )
            )
        }
    }
}

@Composable
private fun QuickJumpChip(
    title: String,
    subtitle: String,
    page: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "($subtitle)",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}



