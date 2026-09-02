package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.ui.theme.QuranYellow

data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home, "landing"),
        BottomNavItem("Quran", Icons.AutoMirrored.Filled.MenuBook, "mushaf_home"),
        BottomNavItem("Hadith", Icons.Default.AutoStories, "hadith_books"),
        BottomNavItem("Tasbeeh", Icons.Default.AutoAwesome, "adhkar"),
        BottomNavItem("Settings", Icons.Default.Settings, "settings")
    )

    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry.value?.destination?.route

    val isDarkTheme = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f
    val navBg = if (isDarkTheme) {
        androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
    } else {
        androidx.compose.material3.MaterialTheme.colorScheme.primary
    }

    val goldAccent = androidx.compose.material3.MaterialTheme.colorScheme.secondary

    Surface(
        color = navBg,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            color = navBg,
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
            items.forEach { item ->
                val selected = when (item.route) {
                    "landing" -> currentRoute == "landing"
                    "mushaf_home" -> currentRoute?.startsWith("mushaf") == true || currentRoute == "juz_index" || currentRoute == "bookmarks"
                    "hadith_books" -> currentRoute?.startsWith("hadith_") == true || currentRoute == "daily_hadith"
                    "tafseer" -> currentRoute?.startsWith("tafseer") == true
                    "adhkar" -> currentRoute?.startsWith("adhkar") == true || currentRoute == "prayer_times" || currentRoute == "qibla"
                    "settings" -> currentRoute?.startsWith("settings") == true
                    else -> currentRoute == item.route
                }
                val activeBg = goldAccent
                val activeContentColor = if (isDarkTheme) androidx.compose.material3.MaterialTheme.colorScheme.onSecondary else Color(0xFF061E16)
                val inactiveContentColor = if (isDarkTheme) {
                    androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                } else {
                    androidx.compose.material3.MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                }

                Column(
                    modifier = Modifier
                        .size(width = 70.dp, height = 68.dp)
                        .clip(CircleShape)
                        .background(if (selected) activeBg else Color.Transparent)
                        .clickable {
                            if (item.route == "landing") {
                                if (currentRoute != "landing") {
                                    navController.navigate("landing") {
                                        popUpTo("landing") {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            } else {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo("landing") {
                                            saveState = false
                                        }
                                        launchSingleTop = true
                                        restoreState = false
                                    }
                                }
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (selected) activeContentColor else inactiveContentColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.title,
                        fontSize = 10.5.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) activeContentColor else inactiveContentColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
}

