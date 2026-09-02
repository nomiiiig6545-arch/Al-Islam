package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.HandmadeBrushesFontFamily
import com.example.data.prayer.*
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimesScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { PrayerSettingsRepository(context) }

    val selectedCity by repository.selectedCity.collectAsState()
    val calculationMethod by repository.calculationMethod.collectAsState()
    val juristicSchool by repository.juristicSchool.collectAsState()
    val locationMode by repository.locationMode.collectAsState()
    val cacheStatus by repository.cacheStatus.collectAsState()
    val notificationStates by repository.notificationStates.collectAsState()

    var dayOffset by remember { mutableIntStateOf(0) }
    val currentDate = remember(dayOffset) { LocalDate.now().plusDays(dayOffset.toLong()) }

    var showMethodDialog by remember { mutableStateOf(false) }
    var showCityDialog by remember { mutableStateOf(false) }
    var citySearchQuery by remember { mutableStateOf("") }
    var isFetchingLocation by remember { mutableStateOf(false) }

    // Prayer calculation with dynamic inputs
    val prayerTimesResult = remember(currentDate, selectedCity, calculationMethod, juristicSchool) {
        PrayerTimeCalculator.calculate(
            date = currentDate,
            location = selectedCity,
            method = calculationMethod,
            school = juristicSchool
        )
    }

    // Function to trigger GPS Location Fetch
    val fetchGpsLocation = {
        isFetchingLocation = true
        coroutineScope.launch {
            val result = LocationHelper.getCurrentLocation(context)
            isFetchingLocation = false
            result.onSuccess { gpsLoc ->
                repository.saveCity(gpsLoc, "Live GPS Synced")
                Toast.makeText(context, "Location updated: ${gpsLoc.name}", Toast.LENGTH_SHORT).show()
            }.onFailure { err ->
                repository.setCacheStatus("Loaded from Cache (GPS Unavailable)")
                Toast.makeText(context, err.message ?: "Unable to fetch GPS location", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Permission launcher for location
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            repository.saveLocationMode(LocationMode.AUTO_GPS)
            fetchGpsLocation()
        } else {
            Toast.makeText(context, "Location permission denied. Using cached city.", Toast.LENGTH_SHORT).show()
            repository.setCacheStatus("Loaded from Cache (Permission Denied)")
        }
    }

    val requestLocationAndFetch = {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            fetchGpsLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Dark Green Theme Color Tokens with Gold Accents
    val bgVal = MaterialTheme.colorScheme.background
    val isDarkTheme = (bgVal.red * 0.299f + bgVal.green * 0.587f + bgVal.blue * 0.114f) < 0.5f

    val heroGreenDark = if (isDarkTheme) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary
    val heroGreenMain = if (isDarkTheme) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary
    val darkGreenPrimary = MaterialTheme.colorScheme.primary
    val goldAccent = MaterialTheme.colorScheme.secondary
    val goldSubtle = MaterialTheme.colorScheme.secondary
    val containerBg = if (isDarkTheme) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface
    val containerBorder = if (isDarkTheme) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.secondary
    val controlCardBg = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant

    val refreshRotation by animateFloatAsState(
        targetValue = if (isFetchingLocation) 360f else 0f,
        label = "refreshAnim"
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgVal)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(bottom = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ----------------------------------------------------
        // Top Bar: Clean "Al-Quran Majeed" Title
        // ----------------------------------------------------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 12.dp, start = 12.dp, end = 12.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = darkGreenPrimary
                )
            }

            Text(
                text = "Al-Quran Majeed",
                fontFamily = HandmadeBrushesFontFamily,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = darkGreenPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ----------------------------------------------------
            // 1. Dark Green Hero Banner Header (Upcoming Prayer)
            // ----------------------------------------------------
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = heroGreenDark,
                border = BorderStroke(1.5.dp, goldAccent),
                shadowElevation = 6.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(heroGreenMain, heroGreenDark)
                            )
                        )
                        .padding(20.dp)
                ) {
                    // Decorative Background Icon
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.06f),
                        modifier = Modifier
                            .size(140.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 30.dp, y = (-20).dp)
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Location Pill (Clickable to change city)
                        Surface(
                            onClick = {
                                citySearchQuery = ""
                                showCityDialog = true
                            },
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, goldAccent.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = goldAccent,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = selectedCity.name,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Change City",
                                    tint = goldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Combined Gregorian & Hijri Date (e.g. "05 Jul 2026 | 20 Muharram 1448 AH")
                        Text(
                            text = HijriCalendarUtils.getCombinedDateString(currentDate),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = goldSubtle,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.4.sp,
                                textAlign = TextAlign.Center
                            )
                        )

                        // Live City Time Badge (e.g. "Local Time: 02:32 pm")
                        if (prayerTimesResult.currentCityTime.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = darkGreenPrimary.copy(alpha = 0.4f),
                                border = BorderStroke(0.8.dp, goldAccent.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = goldAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "City Time: ${prayerTimesResult.currentCityTime.uppercase()}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                }
                            }
                        }

                        // Gold Ornamental Line
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(1.dp)
                                    .background(goldAccent.copy(alpha = 0.8f))
                            )
                            Text(
                                text = "✦",
                                fontSize = 11.sp,
                                color = goldAccent
                            )
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(1.dp)
                                    .background(goldAccent.copy(alpha = 0.8f))
                            )
                        }

                        // Upcoming Event Details
                        Text(
                            text = if (prayerTimesResult.activePrayerId != null) "ACTIVE: ${prayerTimesResult.activePrayerId?.uppercase()} • NEXT PRAYER" else "UPCOMING PRAYER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = goldAccent,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.4.sp,
                                fontSize = 10.sp
                            )
                        )
                        Text(
                            text = prayerTimesResult.upcomingEventName,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 38.sp
                            )
                        )
                        Text(
                            text = prayerTimesResult.upcomingEventTime,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = goldAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        )

                        // Status Badge / Description Pill
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.35f),
                            border = BorderStroke(0.8.dp, goldAccent.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(
                                            color = if (cacheStatus.contains("GPS")) Color(0xFF34D399) else Color(0xFFFBBF24),
                                            shape = CircleShape
                                        )
                                )
                                Text(
                                    text = cacheStatus,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // ----------------------------------------------------
            // 2. Main Prayer Timings List Container (Fajr -> Isha)
            // ----------------------------------------------------
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = containerBg,
                border = BorderStroke(1.dp, containerBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Daily Prayer Timings",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "Adhan Alerts",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    val mainPrayersList = listOf(
                        PrayerTimeItem("fajr", "Fajr", prayerTimesResult.fajr, 0),
                        PrayerTimeItem("dhuhr", "Dhuhr", prayerTimesResult.dhuhr, 0),
                        PrayerTimeItem("asr", "Asr (${juristicSchool.shortName})", prayerTimesResult.asr, 0),
                        PrayerTimeItem("maghrib", "Maghrib", prayerTimesResult.maghrib, 0),
                        PrayerTimeItem("isha", "Isha", prayerTimesResult.isha, 0)
                    )

                    mainPrayersList.forEach { prayer ->
                        val baseId = prayer.id.substringBefore(" ")
                        val isActive = prayerTimesResult.activePrayerId == baseId
                        val isNotifEnabled = notificationStates[baseId] ?: true

                        PrayerRowItem(
                            prayer = prayer,
                            isActive = isActive,
                            isNotifEnabled = isNotifEnabled,
                            onToggleNotif = { enabled ->
                                repository.setNotificationEnabled(baseId, enabled)
                                Toast.makeText(
                                    context,
                                    "${prayer.name} reminder ${if (enabled) "enabled" else "disabled"}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                }
            }

            // ----------------------------------------------------
            // 3. Calculation & Location Controls (Moved below Isha)
            // ----------------------------------------------------
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = containerBg,
                border = BorderStroke(1.dp, containerBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Calculation & Location Controls",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    // Calculation Method Selector Dropdown Card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showMethodDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        color = controlCardBg,
                        border = BorderStroke(1.dp, containerBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = darkGreenPrimary.copy(alpha = 0.15f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Tune,
                                            contentDescription = null,
                                            tint = if (isDarkTheme) goldAccent else darkGreenPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "CALCULATION METHOD",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            letterSpacing = 0.8.sp,
                                            fontSize = 10.sp
                                        )
                                    )
                                    Text(
                                        text = calculationMethod.displayName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = darkGreenPrimary.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, goldAccent.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Select",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDarkTheme) goldAccent else darkGreenPrimary
                                        )
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Dropdown",
                                        tint = if (isDarkTheme) goldAccent else darkGreenPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Location Controls (Mode Toggle & Refresh / Change City)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = controlCardBg,
                        border = BorderStroke(1.dp, containerBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Mode Toggles & Refresh Action
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Location Mode Segmented Pill
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(3.dp),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        LocationMode.entries.forEach { mode ->
                                            val isSelected = locationMode == mode
                                            Surface(
                                                modifier = Modifier.clickable {
                                                    repository.saveLocationMode(mode)
                                                    if (mode == LocationMode.AUTO_GPS) {
                                                        requestLocationAndFetch()
                                                    }
                                                },
                                                shape = RoundedCornerShape(16.dp),
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                border = if (isSelected) BorderStroke(0.8.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)) else null
                                            ) {
                                                Text(
                                                    text = mode.displayName,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontSize = 11.sp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                // Fetch / Refresh Button
                                FilledTonalButton(
                                    onClick = {
                                        if (locationMode == LocationMode.AUTO_GPS) {
                                            requestLocationAndFetch()
                                        } else {
                                            repository.saveCity(selectedCity, "Live Recalculated")
                                            Toast.makeText(context, "Prayer timings refreshed", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (locationMode == LocationMode.AUTO_GPS) Icons.Default.MyLocation else Icons.Default.Refresh,
                                            contentDescription = "Refresh",
                                            modifier = Modifier
                                                .size(16.dp)
                                                .rotate(refreshRotation),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = if (locationMode == LocationMode.AUTO_GPS) "FETCH (AUTO)" else "REFRESH",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                    }
                                }
                            }

                            // Location Name and Selection trigger for Manual City
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (locationMode == LocationMode.MANUAL_CITY) {
                                            citySearchQuery = ""
                                            showCityDialog = true
                                        } else {
                                            requestLocationAndFetch()
                                        }
                                    }
                                    .padding(top = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (selectedCity.isGpsLocation) Icons.Default.GpsFixed else Icons.Default.LocationCity,
                                        contentDescription = null,
                                        tint = if (isDarkTheme) goldAccent else darkGreenPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column {
                                        Text(
                                            text = selectedCity.fullDisplayName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (selectedCity.isGpsLocation) "GPS Lat: ${String.format("%.2f", selectedCity.latitude)}, Lng: ${String.format("%.2f", selectedCity.longitude)}" else "Manual city profile",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }

                                if (locationMode == LocationMode.MANUAL_CITY) {
                                    Text(
                                        text = "CHANGE CITY",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isDarkTheme) goldAccent else darkGreenPrimary,
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                }
                            }

                            // Quick Select Popular Cities Scrollable Row
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "QUICK PICK CITIES",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 0.6.sp
                                    )
                                )
                                androidx.compose.foundation.lazy.LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val quickCities = listOf(
                                        "Sargodha", "Lahore", "Faisalabad", "Islamabad", "Rawalpindi",
                                        "Karachi", "Multan", "Gujranwala", "Sialkot", "Peshawar", "Makkah", "Madinah"
                                    )
                                    items(quickCities) { cityName ->
                                        val isCurrentCity = selectedCity.name.equals(cityName, ignoreCase = true)
                                        Surface(
                                            modifier = Modifier.clickable {
                                                val found = DEFAULT_CITIES.find { it.name.equals(cityName, ignoreCase = true) }
                                                if (found != null) {
                                                    repository.saveLocationMode(LocationMode.MANUAL_CITY)
                                                    repository.saveCity(found, "Loaded from Cache")
                                                    Toast.makeText(context, "Location set to ${found.name}", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isCurrentCity) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                            border = BorderStroke(1.dp, if (isCurrentCity) MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                                        ) {
                                            Text(
                                                text = cityName,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (isCurrentCity) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isCurrentCity) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Jurisprudence / School Selection (Shafi'i vs Hanafi)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = controlCardBg,
                        border = BorderStroke(1.dp, containerBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "CALCULATION SCHOOL (ASR)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 0.8.sp,
                                        fontSize = 10.sp
                                    )
                                )
                                Text(
                                    text = "Asr Shadow Factor",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isDarkTheme) goldAccent else darkGreenPrimary,
                                        fontSize = 10.sp
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                JuristicSchool.entries.forEach { school ->
                                    val isSelected = juristicSchool == school
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                repository.saveJuristicSchool(school)
                                                Toast.makeText(
                                                    context,
                                                    "Asr school set to ${school.displayName}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 10.dp, horizontal = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                contentDescription = null,
                                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = school.displayName,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ----------------------------------------------------
            // 4. Extended Astronomical Timings Section
            // ----------------------------------------------------
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = containerBg,
                border = BorderStroke(1.dp, containerBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Extended Astronomical Timings",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = if (isDarkTheme) goldAccent else darkGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Sunrise Status Card
                    ExtendedTimingCard(
                        title = "Sunrise Time",
                        subtitle = "End of Fajr time / Ishraq window",
                        time = prayerTimesResult.sunrise,
                        icon = Icons.Default.WbSunny,
                        accentColor = Color(0xFFF59E0B),
                        isDarkTheme = isDarkTheme
                    )

                    // Zawal Starts (Makruh Time) Card
                    ExtendedTimingCard(
                        title = "Zawal Starts",
                        subtitle = "Zenith approaching • Praying Salah is Makruh",
                        time = prayerTimesResult.zawalStart,
                        icon = Icons.Default.WarningAmber,
                        accentColor = Color(0xFFF43F5E),
                        isDarkTheme = isDarkTheme
                    )

                    // Zawal Ends (Dhuhr Begins / Solar Noon) Card
                    ExtendedTimingCard(
                        title = "Zawal Ends (Dhuhr Begins)",
                        subtitle = "Sun passes the meridian • Dhuhr time starts",
                        time = prayerTimesResult.zawalEnd,
                        icon = Icons.Default.LightMode,
                        accentColor = if (isDarkTheme) Color(0xFF34D399) else Color(0xFF059669),
                        isDarkTheme = isDarkTheme
                    )

                    // Sehar / Tahajjud Auxiliary Card
                    ExtendedTimingCard(
                        title = "Sehar End (Fasting Cut-off)",
                        subtitle = "10 mins safety buffer before Fajr",
                        time = prayerTimesResult.sehar,
                        icon = Icons.Default.NightlightRound,
                        accentColor = Color(0xFF818CF8),
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }
    }

    // ----------------------------------------------------
    // Calculation Method Selection Dialog
    // ----------------------------------------------------
    if (showMethodDialog) {
        AlertDialog(
            onDismissRequest = { showMethodDialog = false },
            title = {
                Text(
                    text = "Select Calculation Method",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(CalculationMethod.entries) { method ->
                        val isSelected = method == calculationMethod
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    repository.saveCalculationMethod(method)
                                    showMethodDialog = false
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) darkGreenPrimary.copy(alpha = 0.15f) else Color.Transparent,
                            border = if (isSelected) BorderStroke(1.dp, goldAccent) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        repository.saveCalculationMethod(method)
                                        showMethodDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = if (isDarkTheme) goldAccent else darkGreenPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = method.displayName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) (if (isDarkTheme) goldAccent else darkGreenPrimary) else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Text(
                                        text = "Fajr: ${method.fajrAngle}° | Isha: ${if (method.ishaAngle >= 60) "${method.ishaAngle.toInt()}m" else "${method.ishaAngle}°"}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMethodDialog = false }) {
                    Text("Close", color = if (isDarkTheme) goldAccent else darkGreenPrimary)
                }
            }
        )
    }

    // ----------------------------------------------------
    // City Selection Dialog (With Search Bar)
    // ----------------------------------------------------
    if (showCityDialog) {
        val filteredCities = remember(citySearchQuery) {
            if (citySearchQuery.isBlank()) {
                DEFAULT_CITIES
            } else {
                DEFAULT_CITIES.filter {
                    it.name.contains(citySearchQuery, ignoreCase = true) ||
                            it.country.contains(citySearchQuery, ignoreCase = true)
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showCityDialog = false },
            title = {
                Text(
                    text = "Select City Location",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = citySearchQuery,
                        onValueChange = { citySearchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search Sargodha, Lahore, Karachi...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredCities) { city ->
                            val isSelected = city.name.equals(selectedCity.name, ignoreCase = true)
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        repository.saveCity(city, "Loaded from Cache")
                                        showCityDialog = false
                                        Toast.makeText(context, "Location set to ${city.name}", Toast.LENGTH_SHORT).show()
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) darkGreenPrimary.copy(alpha = 0.15f) else Color.Transparent,
                                border = if (isSelected) BorderStroke(1.dp, goldAccent) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = city.name,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) (if (isDarkTheme) goldAccent else darkGreenPrimary) else MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        Text(
                                            text = city.country,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = if (isDarkTheme) goldAccent else darkGreenPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCityDialog = false }) {
                    Text("Close", color = if (isDarkTheme) goldAccent else darkGreenPrimary)
                }
            }
        )
    }
}

@Composable
private fun ExtendedTimingCard(
    title: String,
    subtitle: String,
    time: String,
    icon: ImageVector,
    accentColor: Color,
    isDarkTheme: Boolean
) {
    val cardBg = if (isDarkTheme) Color(0xFF10281E) else Color(0xFFF4F9F6)
    val cardBorder = if (isDarkTheme) Color(0xFF1E4232) else Color(0xFFDCECE2)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Text(
                text = time,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDarkTheme) Color(0xFFD4AF37) else accentColor,
                    fontSize = 17.sp
                )
            )
        }
    }
}

@Composable
private fun PrayerRowItem(
    prayer: PrayerTimeItem,
    isActive: Boolean,
    isNotifEnabled: Boolean,
    onToggleNotif: (Boolean) -> Unit
) {
    val icon = when (prayer.id.lowercase().substringBefore(" ")) {
        "fajr" -> Icons.Default.WbTwilight
        "sunrise" -> Icons.Default.WbSunny
        "dhuhr" -> Icons.Default.LightMode
        "asr" -> Icons.Default.WbSunny
        "maghrib" -> Icons.Default.NightsStay
        else -> Icons.Default.Bedtime
    }

    val bgVal = MaterialTheme.colorScheme.background
    val isDarkTheme = (bgVal.red * 0.299f + bgVal.green * 0.587f + bgVal.blue * 0.114f) < 0.5f

    val activeBg = if (isDarkTheme) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary
    val activeBorderColor = MaterialTheme.colorScheme.secondary
    val activeTextColor = if (isDarkTheme) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary
    val accentSecondary = MaterialTheme.colorScheme.secondary

    val inactiveBg = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
    val inactiveBorderColor = MaterialTheme.colorScheme.outlineVariant
    val primaryAccent = MaterialTheme.colorScheme.primary

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isActive) activeBg else inactiveBg,
        border = if (isActive) BorderStroke(1.2.dp, activeBorderColor) else BorderStroke(1.dp, inactiveBorderColor),
        shadowElevation = if (isActive) 3.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isActive) Color.Black.copy(alpha = 0.2f) else primaryAccent.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = prayer.name,
                            tint = if (isActive) accentSecondary else primaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = prayer.name,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                            color = if (isActive) activeTextColor else MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        )
                    )
                    if (isActive) {
                        Text(
                            text = "Active Salah Window",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = accentSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = prayer.formattedTime,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold,
                        color = if (isActive) accentSecondary else primaryAccent,
                        fontSize = 17.sp
                    )
                )

                IconButton(
                    onClick = { onToggleNotif(!isNotifEnabled) },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = if (isNotifEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                        contentDescription = "Notification",
                        tint = if (isActive) accentSecondary else if (isNotifEnabled) primaryAccent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
