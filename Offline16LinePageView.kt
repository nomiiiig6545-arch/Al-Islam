package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.mushaf.IndoPakMushafData
import com.example.data.mushaf.MushafPageLineManager
import com.example.data.mushaf.OfflineQuranDataProvider
import com.example.ui.theme.IndoPakFontFamily
import com.example.ui.theme.UrduFontFamily
import com.example.ui.theme.getQuranFontFamily

@Composable
fun Offline16LinePageView(
    pageNumber: Int,
    isDark: Boolean = false,
    scriptStyle: String = "INDO_PAK"
) {
    val context = LocalContext.current
    val pageInfo = remember(pageNumber) { IndoPakMushafData.getPageInfo(pageNumber) }
    val ayahs = remember(pageNumber) { OfflineQuranDataProvider.getOfflineAyahsForPage(context, pageNumber) }
    val pageContent = remember(pageNumber, ayahs) { MushafPageLineManager.build16LinePage(pageNumber, ayahs) }

    val pageBackground = if (isDark) Color(0xFF121413) else Color(0xFFFCFAF4)
    val headerGold = if (isDark) Color(0xFFFCD400) else Color(0xFFC59B27)
    val textColor = if (isDark) Color(0xFFF3F4F6) else Color(0xFF111827)
    val primaryGreen = if (isDark) Color(0xFFBAEED9) else Color(0xFF0F3E28)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp),
        color = pageBackground,
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(4.dp)) {
            // Authentic 16-line Mushaf frame
            IndoPakMushafFrame(
                isNightMode = isDark,
                isFirstPages = pageNumber == 1 || pageNumber == 2
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header (Surah name, Page number, Juz name)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${pageInfo.surahNameArabic} ${IndoPakMushafData.toArabicDigits(pageInfo.surahNumber)}",
                        fontFamily = getQuranFontFamily(scriptStyle),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryGreen
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
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp)
                        )
                    }

                    Text(
                        text = pageInfo.juzNameArabic,
                        fontFamily = getQuranFontFamily(scriptStyle),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryGreen
                    )
                }

                // 16 Lines Content
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    val availableHeight = maxHeight
                    val lineCount = pageContent.lines.size.coerceIn(1, 16)
                    val fontSp = (availableHeight.value / lineCount * 0.52f).coerceIn(12f, 20f).sp

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        pageContent.lines.forEach { line ->
                            if (line.isHeader) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 1.dp),
                                    color = if (isDark) Color(0xFF1B3D2F) else Color(0xFFE8F5E9),
                                    border = BorderStroke(1.dp, headerGold)
                                ) {
                                    Text(
                                        text = line.headerTitle,
                                        fontFamily = getQuranFontFamily(scriptStyle),
                                        fontSize = (fontSp.value * 1.15f).sp,
                                        fontWeight = FontWeight.Bold,
                                        color = headerGold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            } else if (line.isBismillah) {
                                Text(
                                    text = line.text,
                                    fontFamily = getQuranFontFamily(scriptStyle),
                                    fontSize = (fontSp.value * 1.1f).sp,
                                    color = primaryGreen,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text(
                                    text = line.text,
                                    fontFamily = getQuranFontFamily(scriptStyle),
                                    fontSize = fontSp,
                                    color = textColor,
                                    textAlign = TextAlign.Center,
                                    lineHeight = (fontSp.value * 1.3f).sp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // Bottom Footer: Page number indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "صفحہ ${IndoPakMushafData.toArabicDigits(pageNumber)} • آف لائن موڈ",
                        fontFamily = UrduFontFamily,
                        fontSize = 11.sp,
                        color = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}
