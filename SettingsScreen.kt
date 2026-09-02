package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.QuranViewModel
import com.example.ui.theme.AppThemeOption
import com.example.ui.theme.ArabicFontFamily
import com.example.ui.theme.HandmadeBrushesFontFamily
import kotlin.math.roundToInt

import com.example.ui.theme.UrduFontFamily

@Composable
fun SettingsScreen(
    viewModel: QuranViewModel,
    onNavigateBack: () -> Unit = {},
    onNavigateToQuranScripts: () -> Unit = {}
) {
    val themePreference by viewModel.themePreference.collectAsStateWithLifecycle()
    val brightnessLevel by viewModel.brightnessLevel.collectAsStateWithLifecycle()
    val keepScreenOn by viewModel.keepScreenOn.collectAsStateWithLifecycle()

    val isAutoBrightness = brightnessLevel < 0f
    // Current display brightness value between 0.1 and 1.0 (default 0.7 if auto)
    var sliderValue by remember(brightnessLevel) {
        mutableFloatStateOf(if (isAutoBrightness) 0.7f else brightnessLevel.coerceIn(0.1f, 1.0f))
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
            .padding(bottom = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val bgVal = MaterialTheme.colorScheme.background
        val isDark = (bgVal.red * 0.299f + bgVal.green * 0.587f + bgVal.blue * 0.114f) < 0.5f

        val containerBg = MaterialTheme.colorScheme.surfaceVariant
        val containerBorder = MaterialTheme.colorScheme.outlineVariant
        val controlCardBg = MaterialTheme.colorScheme.surface
        val primaryGreen = MaterialTheme.colorScheme.primary
        val goldAccent = MaterialTheme.colorScheme.secondary
        val activeCardBg = MaterialTheme.colorScheme.primaryContainer
        val textColor = MaterialTheme.colorScheme.onSurface
        val textSubColor = MaterialTheme.colorScheme.onSurfaceVariant

        // Top Header with Back Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = primaryGreen
                )
            }

            Text(
                text = "Al-Quran Majeed",
                fontFamily = HandmadeBrushesFontFamily,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = primaryGreen,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Prominent Header Hero Card
        val headerCardBg = if (isDark) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary
        val headerCardText = if (isDark) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            shape = RoundedCornerShape(20.dp),
            color = headerCardBg,
            border = BorderStroke(1.5.dp, goldAccent),
            shadowElevation = 4.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Subtle Settings gear icon watermark background effect
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = headerCardText.copy(alpha = 0.12f),
                    modifier = Modifier
                        .size(110.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 20.dp, y = (-20).dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "App Settings",
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = headerCardText,
                        letterSpacing = 0.8.sp,
                        textAlign = TextAlign.Center
                    )

                    // Decorative Gold Divider & Diamond Ornaments
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(1.5.dp)
                                .background(goldAccent)
                        )
                        Text(
                            text = "✦",
                            fontSize = 12.sp,
                            color = goldAccent
                        )
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(1.5.dp)
                                .background(goldAccent)
                        )
                    }

                    Text(
                        text = "Customize your reading & display preferences",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = goldAccent,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Select Favorite Theme Card (System, Light, Dark)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(20.dp),
            color = containerBg,
            border = BorderStroke(1.dp, containerBorder)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 18.dp)
            ) {
                // Header with title "Select Favorite Theme"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp)
                        .padding(bottom = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Theme",
                            tint = primaryGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Select Favorite Theme",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            )
                            Text(
                                text = "پسندیدہ مرکزی تھیم",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = textSubColor,
                                    fontFamily = UrduFontFamily
                                )
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = if (isDark) activeCardBg else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, if (isDark) goldAccent.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = AppThemeOption.fromId(themePreference).title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isDark) goldAccent else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // 3 Core Favorite Themes (System Default, Light Theme, Dark Theme)
                val coreThemes = remember {
                    listOf(
                        AppThemeOption.SYSTEM_DEFAULT,
                        AppThemeOption.SIMPLE_LIGHT,
                        AppThemeOption.SIMPLE_DARK
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    coreThemes.forEach { option ->
                        ThemeOptionCompactCard(
                            themeOption = option,
                            isSelected = themePreference == option.id,
                            onClick = { viewModel.setThemePreference(option.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1.2 Dark Theme Colors Card (Horizontal Scroll)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(20.dp),
            color = containerBg,
            border = BorderStroke(1.dp, containerBorder)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 18.dp)
            ) {
                // Header with title "Dark Theme Colors"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp)
                        .padding(bottom = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = "Dark Colors",
                            tint = if (isDark) goldAccent else primaryGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Dark Theme Colors",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            )
                            Text(
                                text = "ڈارک تھیم کے مختلف دلکش رنگ",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = textSubColor,
                                    fontFamily = UrduFontFamily
                                )
                            )
                        }
                    }
                }

                // Horizontal Scrollable Row of Dark Color Themes
                val darkColorThemes = remember {
                    listOf(
                        AppThemeOption.DARK_GREEN,
                        AppThemeOption.DARK_BLUE,
                        AppThemeOption.DARK_ORANGE,
                        AppThemeOption.DARK_RED,
                        AppThemeOption.DARK_PURPLE,
                        AppThemeOption.GOLDEN_AMBER,
                        AppThemeOption.TEAL_OCEAN
                    )
                }

                val darkListState = rememberLazyListState()
                LaunchedEffect(themePreference) {
                    val index = darkColorThemes.indexOfFirst { it.id == themePreference }
                    if (index >= 0) {
                        darkListState.animateScrollToItem(index)
                    }
                }

                LazyRow(
                    state = darkListState,
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(darkColorThemes, key = { it.id }) { option ->
                        ThemeOptionCompactCard(
                            themeOption = option,
                            isSelected = themePreference == option.id,
                            onClick = { viewModel.setThemePreference(option.id) },
                            modifier = Modifier.width(104.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Brightness & Screen Control Card (Design Polish)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(20.dp),
            color = containerBg,
            border = BorderStroke(1.dp, containerBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Card Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BrightnessMedium,
                            contentDescription = "Brightness",
                            tint = primaryGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Brightness & Display",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            )
                            Text(
                                text = "برائٹنس اور ڈسپلے سیٹنگز",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = textSubColor,
                                    fontFamily = UrduFontFamily
                                )
                            )
                        }
                    }

                    // Brightness Percentage Badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = activeCardBg,
                        border = BorderStroke(1.dp, goldAccent.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = if (isAutoBrightness) "Auto" else "${(sliderValue * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = goldAccent
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Auto / System Brightness Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isAutoBrightness) {
                                viewModel.setBrightnessLevel(sliderValue)
                            } else {
                                viewModel.setBrightnessLevel(-1f)
                            }
                        }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto / System Brightness",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = textColor
                            )
                        )
                        Text(
                            text = "Use device default brightness level",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = textSubColor
                            )
                        )
                    }
                    Switch(
                        checked = isAutoBrightness,
                        onCheckedChange = { auto ->
                            if (auto) {
                                viewModel.setBrightnessLevel(-1f)
                            } else {
                                viewModel.setBrightnessLevel(sliderValue)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = primaryGreen,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = containerBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = containerBorder)
                Spacer(modifier = Modifier.height(14.dp))

                // Brightness Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BrightnessLow,
                        contentDescription = "Low Brightness",
                        tint = if (isAutoBrightness) textSubColor.copy(alpha = 0.4f) else primaryGreen,
                        modifier = Modifier.size(20.dp)
                    )

                    Slider(
                        value = sliderValue,
                        onValueChange = { newValue ->
                            sliderValue = newValue
                            viewModel.setBrightnessLevel(newValue)
                        },
                        valueRange = 0.08f..1.0f,
                        enabled = true,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = goldAccent,
                            activeTrackColor = primaryGreen,
                            inactiveTrackColor = containerBorder
                        )
                    )

                    Icon(
                        imageVector = Icons.Default.BrightnessHigh,
                        contentDescription = "High Brightness",
                        tint = if (isAutoBrightness) primaryGreen.copy(alpha = 0.4f) else goldAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Quick Brightness Presets
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(
                        "25%" to 0.25f,
                        "50%" to 0.50f,
                        "75%" to 0.75f,
                        "100%" to 1.00f
                    )
                    presets.forEach { (label, value) ->
                        val isSelected = !isAutoBrightness && (sliderValue - value).let { kotlin.math.abs(it) < 0.08f }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    sliderValue = value
                                    viewModel.setBrightnessLevel(value)
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) activeCardBg else controlCardBg,
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) goldAccent else containerBorder
                            )
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) goldAccent else textColor
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = containerBorder)
                Spacer(modifier = Modifier.height(14.dp))

                // Keep Screen On Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setKeepScreenOn(!keepScreenOn) }
                        .padding(vertical = 4.dp),
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
                            color = if (keepScreenOn) activeCardBg else controlCardBg,
                            border = BorderStroke(1.dp, if (keepScreenOn) goldAccent else containerBorder),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (keepScreenOn) Icons.Default.WbSunny else Icons.Default.Bedtime,
                                    contentDescription = null,
                                    tint = if (keepScreenOn) goldAccent else primaryGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Keep Screen Awake",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor
                                )
                            )
                            Text(
                                text = "تلاوت کے دوران اسکرین بند نہیں ہوگی",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = textSubColor,
                                    fontFamily = UrduFontFamily
                                )
                            )
                        }
                    }

                    Switch(
                        checked = keepScreenOn,
                        onCheckedChange = { viewModel.setKeepScreenOn(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = primaryGreen,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = containerBorder
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionCompactCard(
    themeOption: AppThemeOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = MaterialTheme.colorScheme.background
    val isDark = bg.luminance() < 0.5f

    val highlightColor = if (isDark) MaterialTheme.colorScheme.secondary else Color(0xFF0A3324)
    val activeBgColor = if (isDark) MaterialTheme.colorScheme.primaryContainer else Color(0xFFF0FDF4)
    val cardBgColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White
    val cardBorderColor = if (isSelected) highlightColor else if (isDark) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f) else Color(0xFFE5E7EB)
    val titleTextColor = if (isSelected) highlightColor else if (isDark) MaterialTheme.colorScheme.onSurface else Color(0xFF374151)

    Surface(
        modifier = modifier
            .widthIn(min = 100.dp)
            .height(116.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) activeBgColor else cardBgColor,
        shadowElevation = if (isSelected) 2.dp else 0.dp,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = cardBorderColor
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Selected indicator badge in top right corner
            if (isSelected) {
                Surface(
                    shape = CircleShape,
                    color = highlightColor,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(8.dp)
                ) {}
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Colored badge preview container
                Surface(
                    shape = CircleShape,
                    color = themeOption.cardPreviewBg,
                    border = BorderStroke(1.5.dp, themeOption.badgeColor),
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = themeOption.icon,
                            contentDescription = themeOption.title,
                            tint = themeOption.badgeColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Theme Name
                Text(
                    text = themeOption.title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = titleTextColor,
                        lineHeight = 13.sp
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
