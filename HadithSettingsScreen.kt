package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArabicFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HadithSettingsScreen(
    onBackClick: () -> Unit
) {
    var showArabic by remember { mutableStateOf(true) }
    var showTranslation by remember { mutableStateOf(true) }
    var arabicFontSize by remember { mutableFloatStateOf(24f) }
    var translationFontSize by remember { mutableFloatStateOf(16f) }
    var selectedLanguage by remember { mutableStateOf("English") }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bg = MaterialTheme.colorScheme.background
    val cardBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White
    val innerBoxBg = if (isDark) MaterialTheme.colorScheme.surface else Color(0xFFF8F6F0)
    val cardBorder = if (isDark) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f) else Color(0xFFD0C5AF).copy(alpha = 0.5f)
    val goldColor = MaterialTheme.colorScheme.secondary
    val primaryText = MaterialTheme.colorScheme.onBackground
    val mutedText = MaterialTheme.colorScheme.onSurfaceVariant

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
                        text = "Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldColor
                    )

                    Spacer(modifier = Modifier.width(48.dp))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // VIEW SETTING CARD
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "VIEW SETTING",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldColor,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Choose Language Item
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedLanguage = if (selectedLanguage == "English") "Urdu" else "English"
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = innerBoxBg
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = goldColor
                                )
                                Text(
                                    text = "Choose Language",
                                    fontSize = 15.sp,
                                    color = primaryText
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = selectedLanguage,
                                    fontSize = 14.sp,
                                    color = mutedText
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                                    contentDescription = null,
                                    tint = mutedText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(thickness = 1.dp, color = cardBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "SHOW",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldColor,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Arabic Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Arabic",
                            fontSize = 15.sp,
                            color = primaryText
                        )
                        Switch(
                            checked = showArabic,
                            onCheckedChange = { showArabic = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = goldColor
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Translation Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Translation",
                            fontSize = 15.sp,
                            color = primaryText
                        )
                        Switch(
                            checked = showTranslation,
                            onCheckedChange = { showTranslation = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = goldColor
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // FONT SETTING CARD
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "FONT SETTING",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldColor,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Preview Box
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = innerBoxBg,
                        border = BorderStroke(1.dp, cardBorder.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (showArabic) {
                                Text(
                                    text = "عَنْ عَائِشَةَ ، قَالَتْ",
                                    fontSize = arabicFontSize.sp,
                                    fontFamily = ArabicFontFamily,
                                    color = primaryText,
                                    textAlign = TextAlign.Center,
                                    lineHeight = (arabicFontSize * 1.5f).sp
                                )
                            }
                            if (showTranslation) {
                                Text(
                                    text = "وہ عائشہ رضی اللہ عنہا سے نقل کرتے ہیں",
                                    fontSize = translationFontSize.sp,
                                    fontFamily = ArabicFontFamily,
                                    color = mutedText,
                                    textAlign = TextAlign.Center,
                                    lineHeight = (translationFontSize * 1.5f).sp
                                )
                                Text(
                                    text = "Narrated `Aisha",
                                    fontSize = (translationFontSize * 0.9f).sp,
                                    fontStyle = FontStyle.Italic,
                                    color = mutedText.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Arabic Font Size Slider
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Arabic Font Size",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryText
                            )
                            Text(
                                text = "${arabicFontSize.toInt()}px",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = goldColor
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "A", fontSize = 12.sp, color = mutedText)
                            Slider(
                                value = arabicFontSize,
                                onValueChange = { arabicFontSize = it },
                                valueRange = 16f..48f,
                                colors = SliderDefaults.colors(
                                    thumbColor = goldColor,
                                    activeTrackColor = goldColor,
                                    inactiveTrackColor = cardBorder
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Text(text = "A", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = primaryText)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Translation Font Size Slider
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Translation Font Size",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryText
                            )
                            Text(
                                text = "${translationFontSize.toInt()}px",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = goldColor
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "A", fontSize = 12.sp, color = mutedText)
                            Slider(
                                value = translationFontSize,
                                onValueChange = { translationFontSize = it },
                                valueRange = 12f..32f,
                                colors = SliderDefaults.colors(
                                    thumbColor = goldColor,
                                    activeTrackColor = goldColor,
                                    inactiveTrackColor = cardBorder
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Text(text = "A", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = primaryText)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
