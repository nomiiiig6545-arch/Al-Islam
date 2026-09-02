package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class IslamicEvent(
    val day: String,
    val month: String,
    val title: String,
    val description: String,
    val isPrimary: Boolean = false
)

val islamicEventsList = listOf(
    IslamicEvent("10", "Muharram", "Ashura", "10th of Muharram", isPrimary = true),
    IslamicEvent("12", "Rabi I", "Milad-un-Nabi", "12th of Rabi-ul-Awwal", isPrimary = true),
    IslamicEvent("15", "Shaban", "Shab-e-Barat", "15th of Shaban"),
    IslamicEvent("27", "Ramadan", "Shab-e-Qadr", "27th of Ramadan", isPrimary = true),
    IslamicEvent("01", "Shawwal", "Eid-ul-Fitr", "1st of Shawwal", isPrimary = true),
    IslamicEvent("10", "Dhul Hijjah", "Eid-ul-Adha", "10th of Dhul Hijjah", isPrimary = true)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IslamicCalendarScreen(
    onBackClick: () -> Unit
) {
    var monthIndex by remember { mutableIntStateOf(2) } // Rabi I

    val monthNames = listOf("Muharram", "Safar", "Rabi I", "Rabi II", "Jumada I", "Jumada II", "Rajab", "Shaban", "Ramadan", "Shawwal", "Dhul Qadah", "Dhul Hijjah")
    val currentMonthName = monthNames[monthIndex % monthNames.size]

    val darkBg = MaterialTheme.colorScheme.background
    val goldColor = MaterialTheme.colorScheme.secondary
    val cardBg = MaterialTheme.colorScheme.surfaceVariant
    val cardBgHigh = MaterialTheme.colorScheme.surface
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant

    Scaffold(
        containerColor = darkBg,
        topBar = {
            Surface(
                color = darkBg.copy(alpha = 0.9f),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
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
                        text = "Islamic Calendar",
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Month Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { if (monthIndex > 0) monthIndex-- }) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous Month",
                        tint = goldColor,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = "$currentMonthName, 1448",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldColor
                    )
                    Text(
                        text = "September 2026",
                        fontSize = 14.sp,
                        color = textMuted
                    )
                }

                IconButton(onClick = { monthIndex++ }) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next Month",
                        tint = goldColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Calendar Card Grid
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = cardBg,
                border = BorderStroke(1.dp, goldColor.copy(alpha = 0.25f)),
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    // Days of Week Header
                    val daysOfWeek = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        daysOfWeek.forEach { day ->
                            Text(
                                text = day,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (day == "FRI") goldColor else textMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Calendar Grid Days (30 Days)
                    val totalDays = 30
                    val offset = 2 // Starts on Tuesday

                    var dayCounter = 1
                    for (week in 0..4) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            for (dayOfWeek in 0..6) {
                                val cellIndex = week * 7 + dayOfWeek
                                if (cellIndex < offset || dayCounter > totalDays) {
                                    Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                                } else {
                                    val dayNum = dayCounter
                                    val isToday = (dayNum == 18)
                                    val gregorianDay = (13 + dayNum) % 30 + 1

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isToday) Color(0xFF0B5345) else Color.Transparent)
                                            .border(
                                                width = if (isToday) 1.5.dp else 0.5.dp,
                                                color = if (isToday) goldColor else goldColor.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(8.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "$dayNum",
                                                fontSize = 15.sp,
                                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isToday) goldColor else textPrimary
                                            )
                                            Text(
                                                text = "$gregorianDay",
                                                fontSize = 9.sp,
                                                color = if (isToday) goldColor.copy(alpha = 0.8f) else textMuted.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                    dayCounter++
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Islamic Events Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.Default.EventNote,
                    contentDescription = null,
                    tint = goldColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Islamic Events",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Events List
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                islamicEventsList.forEach { event ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = cardBg,
                        border = BorderStroke(1.dp, if (event.isPrimary) goldColor.copy(alpha = 0.3f) else goldColor.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (event.isPrimary) Color(0xFF0B5345) else cardBgHigh)
                                        .border(1.dp, goldColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = event.day,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = goldColor
                                        )
                                        Text(
                                            text = event.month,
                                            fontSize = 9.sp,
                                            color = textMuted
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = event.title,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = event.description,
                                        fontSize = 12.sp,
                                        color = textMuted
                                    )
                                }
                            }

                            IconButton(onClick = {}) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsNone,
                                    contentDescription = "Remind",
                                    tint = textMuted
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
