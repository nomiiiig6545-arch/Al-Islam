package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.adhkar.AdhkarItem
import com.example.data.adhkar.AdhkarRepository
import com.example.ui.theme.HandmadeBrushesFontFamily
import com.example.ui.theme.UrduNaskhFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdhkarScreen(
    onBackClick: () -> Unit,
    onDhikrClick: (String) -> Unit,
    onFreeTasbeehClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Sabah (Morning), 1 = Sham (Evening)
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val darkGreen = Color(0xFF004D40)
    val gold = Color(0xFFD4AF37)

    val primaryColor = if (!isDark) darkGreen else MaterialTheme.colorScheme.primary
    val cardBg = if (!isDark) darkGreen else MaterialTheme.colorScheme.surfaceVariant
    val accentColor = if (!isDark) gold else MaterialTheme.colorScheme.secondary
    val cardBorder = if (!isDark) gold.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Al-Quran Majeed",
                        fontFamily = HandmadeBrushesFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("adhkar_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onFreeTasbeehClick,
                        modifier = Modifier.testTag("adhkar_open_counter_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = "Open Tasbeeh Counter",
                            tint = accentColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header Section
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Morning & Evening Adhkar",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = primaryColor
                    ),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Start and end your day with remembrance of Allah",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(bottom = 18.dp)
                )

                // Sabah / Sham Tabs
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(30.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        // Sabah Tab
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = 0 },
                            shape = RoundedCornerShape(26.dp),
                            color = if (selectedTab == 0) accentColor else Color.Transparent
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Sabah (Morning)",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp
                                    ),
                                    color = if (selectedTab == 0) {
                                        if (!isDark) Color(0xFF111111) else MaterialTheme.colorScheme.onSecondary
                                    } else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Sham Tab
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = 1 },
                            shape = RoundedCornerShape(26.dp),
                            color = if (selectedTab == 1) accentColor else Color.Transparent
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Sham (Evening)",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp
                                    ),
                                    color = if (selectedTab == 1) {
                                        if (!isDark) Color(0xFF111111) else MaterialTheme.colorScheme.onSecondary
                                    } else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Adhkar List
            val currentList = if (selectedTab == 0) AdhkarRepository.sabahAdhkar else AdhkarRepository.shamAdhkar

            items(currentList, key = { it.id }) { item ->
                AdhkarCardItem(
                    item = item,
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    accentColor = accentColor,
                    primaryColor = primaryColor,
                    isDark = isDark,
                    onClick = { onDhikrClick(item.id) }
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Decorative Divider at end of list
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .height(1.dp)
                            .width(60.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color.Transparent, accentColor.copy(alpha = 0.6f))
                                )
                            )
                    )
                    Icon(
                        imageVector = Icons.Default.Diamond,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .size(16.dp)
                    )
                    Box(
                        modifier = Modifier
                            .height(1.dp)
                            .width(60.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(accentColor.copy(alpha = 0.6f), Color.Transparent)
                                )
                            )
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun AdhkarCardItem(
    item: AdhkarItem,
    cardBg: Color,
    cardBorder: Color,
    accentColor: Color,
    primaryColor: Color,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val darkGreen = Color(0xFF004D40)
    val gold = Color(0xFFD4AF37)
    val lightGreen = Color(0xFFE0F2F1)

    val cardContentTitleColor = if (!isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val cardContentArabicColor = if (!isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val cardContentBenefitColor = if (!isDark) lightGreen else MaterialTheme.colorScheme.onSurfaceVariant
    val badgeBg = if (!isDark) darkGreen else MaterialTheme.colorScheme.surface
    val badgeBorder = if (!isDark) gold else accentColor.copy(alpha = 0.6f)
    val badgeTextColor = if (!isDark) gold else accentColor

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("adhkar_card_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Number Circle Badge
                Surface(
                    shape = CircleShape,
                    color = badgeBg,
                    border = BorderStroke(1.dp, badgeBorder),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = item.number.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = badgeTextColor
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Transliteration/Title
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = cardContentTitleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Repetition Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = badgeBg,
                    border = BorderStroke(1.dp, badgeBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "x${item.targetCount}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = badgeTextColor
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Count with Tasbeeh",
                            tint = badgeTextColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Arabic Text Display (RTL)
            Text(
                text = item.arabicText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = UrduNaskhFontFamily,
                    fontSize = 22.sp,
                    lineHeight = 36.sp,
                    textAlign = TextAlign.Right,
                    textDirection = TextDirection.Rtl
                ),
                color = cardContentArabicColor,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Benefit / Meaning caption
            Text(
                text = item.benefit,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    color = cardContentBenefitColor
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

