package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.mushaf.IndoPakMushafData
import com.example.ui.theme.ArabicFontFamily
import com.example.ui.theme.HandmadeBrushesFontFamily
import com.example.ui.theme.IndoPakFontFamily
import com.example.ui.theme.UthmaniFontFamily
import com.example.ui.theme.getQuranFontFamily

@Composable
fun JuzIndexScreen(
    onNavigateToPage: (Int) -> Unit,
    onBackClick: () -> Unit = {}
) {
    val JUZ_START_PAGES = IndoPakMushafData.JUZ_START_PAGES

    val JUZ_DESCRIPTIONS = listOf(
        "Al-Fatiha 1 - Al-Baqarah 141",
        "Al-Baqarah 142 - Al-Baqarah 252",
        "Al-Baqarah 253 - Ali 'Imran 92",
        "Ali 'Imran 93 - An-Nisa 23",
        "An-Nisa 24 - An-Nisa 147",
        "An-Nisa 148 - Al-Ma'idah 81",
        "Al-Ma'idah 82 - Al-An'am 110",
        "Al-An'am 111 - Al-A'raf 87",
        "Al-A'raf 88 - Al-Anfal 40",
        "Al-Anfal 41 - At-Tawbah 92",
        "At-Tawbah 93 - Hud 5",
        "Hud 6 - Yusuf 52",
        "Yusuf 53 - Ibrahim 52",
        "Al-Hijr 1 - An-Nahl 128",
        "Al-Isra 1 - Al-Kahf 74",
        "Al-Kahf 75 - Ta-Ha 135",
        "Al-Anbiya 1 - Al-Hajj 78",
        "Al-Mu'minun 1 - Al-Furqan 20",
        "Al-Furqan 21 - An-Naml 55",
        "An-Naml 56 - Al-Ankabut 45",
        "Al-Ankabut 46 - Al-Ahzab 30",
        "Al-Ahzab 31 - Ya-Sin 27",
        "Ya-Sin 28 - Az-Zumar 31",
        "Az-Zumar 32 - Fussilat 46",
        "Fussilat 47 - Al-Jathiyah 37",
        "Al-Ahqaf 1 - Qaf 45",
        "Adh-Dhariyat 1 - Al-Hadid 29",
        "Al-Mujadila 1 - At-Tahrim 12",
        "Al-Mulk 1 - Al-Mursalat 50",
        "An-Naba 1 - An-Nas 6"
    )

    val cardBgColor = MaterialTheme.colorScheme.surface
    val cardBorderColor = MaterialTheme.colorScheme.outlineVariant
    val badgeBgColor = MaterialTheme.colorScheme.primaryContainer
    val badgeTextColor = MaterialTheme.colorScheme.onPrimaryContainer
    val primaryGreen = MaterialTheme.colorScheme.primary
    val goldYellow = MaterialTheme.colorScheme.secondary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = primaryGreen
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Al-Quran Majeed",
                    fontFamily = HandmadeBrushesFontFamily,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryGreen,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Juz Index (فهرس الأجزاء)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = goldYellow,
                        fontFamily = UthmaniFontFamily
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }

        val listState = rememberLazyListState()
        LaunchedEffect(Unit) {
            listState.scrollToItem(0, 0)
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(30) { index ->
                val juzNumber = index + 1
                val pageNum = JUZ_START_PAGES.getOrNull(index) ?: ((juzNumber - 1) * 20 + 2)
                val description = JUZ_DESCRIPTIONS.getOrElse(index) { "" }
                val arabicJuzName = IndoPakMushafData.JUZ_NAMES_ARABIC.getOrElse(index) { "جزء $juzNumber" }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToPage(pageNum) },
                    shape = RoundedCornerShape(20.dp),
                    color = cardBgColor,
                    border = BorderStroke(1.dp, cardBorderColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                modifier = Modifier.size(42.dp),
                                shape = CircleShape,
                                color = badgeBgColor,
                                border = BorderStroke(1.dp, primaryGreen)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$juzNumber",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = badgeTextColor
                                        )
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Juz $juzNumber",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = arabicJuzName,
                                fontFamily = UthmaniFontFamily,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryGreen
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Page $pageNum",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = goldYellow,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

