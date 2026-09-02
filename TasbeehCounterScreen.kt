package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.adhkar.AdhkarItem
import com.example.data.adhkar.AdhkarRepository
import com.example.ui.theme.HandmadeBrushesFontFamily
import com.example.ui.theme.UrduNaskhFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbeehCounterScreen(
    initialDhikrId: String? = null,
    onBackClick: () -> Unit,
    onNavigateToAdhkarList: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // Find current dhikr or default
    val allAdhkar = remember { AdhkarRepository.getAll() }
    var currentDhikrIndex by remember {
        val idx = if (initialDhikrId != null) {
            allAdhkar.indexOfFirst { it.id == initialDhikrId }.takeIf { it >= 0 } ?: 0
        } else 0
        mutableStateOf(idx)
    }

    val currentDhikr = allAdhkar.getOrNull(currentDhikrIndex)

    var count by remember { mutableIntStateOf(0) }
    var targetCount by remember(currentDhikrIndex) {
        mutableIntStateOf(currentDhikr?.targetCount ?: 33)
    }
    var completedLaps by remember { mutableIntStateOf(0) }
    var isSoundEnabled by remember { mutableStateOf(true) }
    var isVibrationEnabled by remember { mutableStateOf(true) }
    var showTargetMenu by remember { mutableStateOf(false) }

    fun vibrate() {
        if (!isVibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(35)
                }
            }
        } catch (_: Exception) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    fun incrementCount() {
        vibrate()
        if (targetCount > 0 && count >= targetCount) {
            // Start next lap
            count = 1
            if (targetCount == 1) {
                completedLaps++
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                        vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 70, 70, 70), -1))
                    }
                } catch (_: Exception) {}
            }
        } else {
            count++
            if (targetCount > 0 && count == targetCount) {
                completedLaps++
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                        vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 70, 70, 70), -1))
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun resetCount() {
        count = 0
        completedLaps = 0
    }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val darkGreen = Color(0xFF004D40)
    val gold = Color(0xFFD4AF37)
    val lightGreen = Color(0xFFE0F2F1)
    val darkScreenBg = Color(0xFF111111)

    val primaryColor = if (!isDark) darkGreen else MaterialTheme.colorScheme.primary
    val accentColor = if (!isDark) gold else MaterialTheme.colorScheme.secondary
    val cardBg = if (!isDark) darkGreen else MaterialTheme.colorScheme.surfaceVariant
    val cardBorder = if (!isDark) gold.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val displayBg = if (!isDark) darkScreenBg else MaterialTheme.colorScheme.surface
    val displayBorder = if (!isDark) darkScreenBg else accentColor

    val cardArabicColor = if (!isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val cardBenefitColor = if (!isDark) lightGreen else MaterialTheme.colorScheme.onSurfaceVariant

    val countBtnBg = if (!isDark) gold else accentColor
    val countBtnText = if (!isDark) darkScreenBg else MaterialTheme.colorScheme.onSecondary

    val resetBtnBg = if (!isDark) darkScreenBg else Color.Transparent
    val resetBtnText = if (!isDark) gold else accentColor
    val resetBtnBorder = if (!isDark) gold.copy(alpha = 0.4f) else accentColor

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                        modifier = Modifier.testTag("tasbeeh_back_button")
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
                        onClick = onNavigateToAdhkarList,
                        modifier = Modifier.testTag("tasbeeh_list_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = "Adhkar List",
                            tint = accentColor
                        )
                    }
                    IconButton(
                        onClick = { isVibrationEnabled = !isVibrationEnabled },
                        modifier = Modifier.testTag("tasbeeh_vibrate_toggle")
                    ) {
                        Icon(
                            imageVector = if (isVibrationEnabled) Icons.Default.Vibration else Icons.Default.Smartphone,
                            contentDescription = "Toggle Vibration",
                            tint = if (isVibrationEnabled) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Script Subtitle & Heading
            Text(
                text = "Unlimited Hasanaat",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Cursive,
                    fontSize = 28.sp,
                    color = accentColor
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = "TASBIH",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 36.sp,
                    letterSpacing = 2.sp,
                    color = primaryColor
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Main Tasbeeh Card Container
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Navigation between Adhkar (Prev / Next)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (currentDhikrIndex > 0) {
                                    currentDhikrIndex--
                                    count = 0
                                }
                            },
                            enabled = currentDhikrIndex > 0
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous Dhikr",
                                tint = if (currentDhikrIndex > 0) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }

                        // Target Selector Pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (!isDark) darkGreen else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.6f)),
                            modifier = Modifier.clickable { showTargetMenu = true }
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (targetCount > 0) "Target: $targetCount" else "Target: Unlimited",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = accentColor
                                    )
                                )

                                DropdownMenu(
                                    expanded = showTargetMenu,
                                    onDismissRequest = { showTargetMenu = false }
                                ) {
                                    listOf(3, 10, 33, 100, 0).forEach { target ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(if (target == 0) "Unlimited (∞)" else "$target Count")
                                            },
                                            onClick = {
                                                targetCount = target
                                                showTargetMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        IconButton(
                            onClick = {
                                if (currentDhikrIndex < allAdhkar.size - 1) {
                                    currentDhikrIndex++
                                    count = 0
                                }
                            },
                            enabled = currentDhikrIndex < allAdhkar.size - 1
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Dhikr",
                                tint = if (currentDhikrIndex < allAdhkar.size - 1) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Arabic Text of Dhikr (Using Tafseer Amiri Font)
                    val arabicText = currentDhikr?.arabicText ?: "سُبْحَانَ اللهِ وَبِحَمْدِهِ سُبْحَانَ اللهِ الْعَظِيْمِ"
                    Text(
                        text = "“ $arabicText ”",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = UrduNaskhFontFamily,
                            fontSize = 22.sp,
                            lineHeight = 36.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            textDirection = TextDirection.Rtl
                        ),
                        color = cardArabicColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dhikr Benefit / Meaning
                    Text(
                        text = currentDhikr?.benefit ?: currentDhikr?.title ?: "Glorification of Allah",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = cardBenefitColor
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Digital Counter Display Device
                    Surface(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .clickable { incrementCount() }
                            .testTag("tasbeeh_digital_display"),
                        shape = RoundedCornerShape(28.dp),
                        color = displayBg,
                        border = BorderStroke(2.5.dp, displayBorder),
                        shadowElevation = 6.dp
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (completedLaps > 0) {
                                Text(
                                    text = "Lap $completedLaps",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = accentColor
                                    )
                                )
                            }

                            Text(
                                text = count.toString(),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = if (count >= 100) 54.sp else 62.sp,
                                    letterSpacing = (-1).sp,
                                    color = if (!isDark) Color.White else accentColor
                                ),
                                textAlign = TextAlign.Center
                            )

                            if (targetCount > 0) {
                                Text(
                                    text = "/ $targetCount",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontSize = 13.sp,
                                        color = if (!isDark) lightGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Big COUNT Button
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val buttonScale by animateFloatAsState(
                        targetValue = if (isPressed) 0.94f else 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessHigh),
                        label = "scale"
                    )

                    Button(
                        onClick = { incrementCount() },
                        interactionSource = interactionSource,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .scale(buttonScale)
                            .testTag("tasbeeh_count_button"),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = countBtnBg, contentColor = countBtnText
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        Text(
                            text = "COUNT",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                letterSpacing = 3.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // RESET Button & Completed Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (targetCount > 0 && count > 0) {
                                "${targetCount - count} remaining"
                            } else "",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                color = accentColor
                            )
                        )

                        OutlinedButton(
                            onClick = { resetCount() },
                            modifier = Modifier
                                .testTag("tasbeeh_reset_button"),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, resetBtnBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = resetBtnText, containerColor = resetBtnBg),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset",
                                modifier = Modifier.size(16.dp),
                                tint = resetBtnText
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "RESET",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

