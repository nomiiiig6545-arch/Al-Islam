package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.mushaf.IndoPakMushafData
import com.example.data.mushaf.QcfDataManager
import com.example.data.mushaf.QcfLine
import com.example.data.mushaf.QcfPageData
import com.example.data.mushaf.QcfWord
import com.example.ui.theme.ArabicFontFamily
import com.example.ui.theme.IndoPakFontFamily
import com.example.ui.theme.getQuranFontFamily

@Composable
fun QcfMushafPageView(
    pageNumber: Int,
    isDark: Boolean,
    onWordClick: ((QcfWord) -> Unit)? = null,
    scriptStyle: String = "INDO_PAK"
) {
    val context = LocalContext.current
    var pageData by remember(pageNumber) { mutableStateOf<QcfPageData?>(null) }
    var isLoading by remember(pageNumber) { mutableStateOf(true) }

    LaunchedEffect(pageNumber) {
        isLoading = true
        pageData = QcfDataManager.getPageData(context, pageNumber)
        isLoading = false
    }

    val pageInfo = remember(pageNumber) { IndoPakMushafData.getPageInfo(pageNumber) }
    val isFirstTwoPages = pageNumber == 1 || pageNumber == 2
    val pageBackground = if (isDark) Color(0xFF161917) else Color(0xFFFCFAF4)
    val headerGold = if (isDark) Color(0xFFFCD400) else Color(0xFFC59B27)
    val textColor = if (isDark) Color(0xFFF3F4F6) else Color(0xFF111827)
    val goldAccent = if (isDark) Color(0xFFFCD400) else Color(0xFFB8860B)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        color = pageBackground,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = if (isDark) 2.dp else 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            // Authentic Madina / Indo-Pak Double Ruled Frame
            IndoPakMushafFrame(
                isNightMode = isDark,
                isFirstPages = isFirstTwoPages
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header (Surah name, Page number, Juz name)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 1.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${pageInfo.surahNameArabic} ${IndoPakMushafData.toArabicDigits(pageInfo.surahNumber)}",
                        fontFamily = getQuranFontFamily(scriptStyle),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFBAEED9) else MaterialTheme.colorScheme.primary
                    )

                    Surface(
                        shape = CircleShape,
                        color = if (isDark) Color(0xFF221B00) else Color(0xFFFFF9E6),
                        border = BorderStroke(1.dp, headerGold.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = IndoPakMushafData.toArabicDigits(pageNumber),
                            fontFamily = getQuranFontFamily(scriptStyle),
                            color = headerGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }

                    Text(
                        text = pageInfo.juzNameArabic,
                        fontFamily = getQuranFontFamily(scriptStyle),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFBAEED9) else MaterialTheme.colorScheme.primary
                    )
                }

                // Main Page Lines - Dynamically scaled to fit entire page on 1 screen without scrolling
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = headerGold,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                } else if (pageData != null) {
                    val data = pageData!!
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        val availableHeight = maxHeight
                        val lineCount = data.lines.size.coerceAtLeast(1)
                        // Calculate optimal font size and line height to guarantee no vertical scrolling
                        val fontSp = (availableHeight.value / lineCount * 0.58f).coerceIn(11f, 22f).sp

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            data.lines.forEach { line ->
                                QcfLineRow(
                                    line = line,
                                    isDark = isDark,
                                    textColor = textColor,
                                    goldColor = goldAccent,
                                    fontSize = fontSp,
                                    onWordClick = onWordClick
                                )
                            }
                        }
                    }
                } else {
                    // Fallback state if page could not be loaded
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Page $pageNumber",
                            color = textColor,
                            fontFamily = getQuranFontFamily(scriptStyle)
                        )
                    }
                }

                // Bottom Guide
                TajweedRulesFooter(isDark = isDark)
            }
        }
    }
}

@Composable
fun QcfLineRow(
    line: QcfLine,
    isDark: Boolean,
    textColor: Color,
    goldColor: Color,
    fontSize: TextUnit,
    onWordClick: ((QcfWord) -> Unit)?
) {
    val context = LocalContext.current

    // Detect if this line is a Surah Header or Bismillah line
    val firstWord = line.words.firstOrNull()
    val isSurahHeader = firstWord?.type == "surah_header"
    val isBismillah = firstWord?.type == "bismillah"

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        if (isSurahHeader) {
            // Authentic Surah Header Banner with QCF QBSML Calligraphy
            val headerFont = QcfDataManager.getFontFamily(context, firstWord?.font ?: "QCF4_QBSML")
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 1.dp),
                shape = RoundedCornerShape(4.dp),
                color = if (isDark) Color(0xFF1B3D2F) else Color(0xFFE8F5E9),
                border = BorderStroke(1.dp, goldColor)
            ) {
                Text(
                    text = firstWord?.char ?: "",
                    fontFamily = headerFont,
                    fontSize = (fontSize.value * 1.25f).sp,
                    color = goldColor,
                    textAlign = TextAlign.Center,
                    softWrap = false,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                )
            }
        } else if (isBismillah) {
            // Bismillah Calligraphy Line
            val bismillahFont = QcfDataManager.getFontFamily(context, firstWord?.font ?: "QCF4_Hafs_01")
            Text(
                text = firstWord?.char ?: "",
                fontFamily = bismillahFont,
                fontSize = (fontSize.value * 1.2f).sp,
                color = if (isDark) Color(0xFFBAEED9) else MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                softWrap = false,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp)
            )
        } else {
            // Quranic Verses Line with pixel-perfect glyphs justified across full width
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 1.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                line.words.forEach { word ->
                    val wordFont = QcfDataManager.getFontFamily(context, word.font)
                    val isVerseEnd = word.type == "end"
                    val wordColor = when {
                        isVerseEnd -> goldColor
                        else -> textColor
                    }

                    Text(
                        text = word.char,
                        fontFamily = wordFont,
                        fontSize = fontSize,
                        color = wordColor,
                        softWrap = false,
                        maxLines = 1,
                        modifier = Modifier
                            .clickable(enabled = onWordClick != null) {
                                onWordClick?.invoke(word)
                            }
                    )
                }
            }
        }
    }
}
