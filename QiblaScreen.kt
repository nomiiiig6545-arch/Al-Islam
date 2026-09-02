package com.example.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HandmadeBrushesFontFamily
import com.example.ui.theme.UrduFontFamily
import kotlin.math.*

@Composable
fun QiblaScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    val magnetometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) }
    val locationManager = remember { context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager }

    var azimuth by remember { mutableFloatStateOf(0f) }
    var qiblaBearing by remember { mutableFloatStateOf(260f) } // Default South Asia/Middle East bearing

    // Auto-detect user GPS location to compute exact Makkah Qibla angle
    LaunchedEffect(Unit) {
        try {
            val providers = locationManager?.getProviders(true) ?: emptyList()
            for (provider in providers) {
                val loc = locationManager?.getLastKnownLocation(provider)
                if (loc != null && loc.latitude != 0.0 && loc.longitude != 0.0) {
                    qiblaBearing = calculateQiblaBearing(loc.latitude, loc.longitude)
                    break
                }
            }
        } catch (_: SecurityException) {
            // Default bearing maintained safely
        }
    }

    DisposableEffect(Unit) {
        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        var hasGravity = false
        var hasGeomagnetic = false

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    // Low-pass filter for smooth movement
                    gravity[0] = gravity[0] * 0.85f + event.values[0] * 0.15f
                    gravity[1] = gravity[1] * 0.85f + event.values[1] * 0.15f
                    gravity[2] = gravity[2] * 0.85f + event.values[2] * 0.15f
                    hasGravity = true
                } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    geomagnetic[0] = geomagnetic[0] * 0.85f + event.values[0] * 0.15f
                    geomagnetic[1] = geomagnetic[1] * 0.85f + event.values[1] * 0.15f
                    geomagnetic[2] = geomagnetic[2] * 0.85f + event.values[2] * 0.15f
                    hasGeomagnetic = true
                }

                if (hasGravity && hasGeomagnetic) {
                    val r = FloatArray(9)
                    val i = FloatArray(9)
                    if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(r, orientation)
                        val rawAzimuth = (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360) % 360
                        azimuth = rawAzimuth
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_GAME)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // Relative angle between device heading and Makkah
    val relativeAngle = remember(azimuth, qiblaBearing) {
        val diff = (qiblaBearing - azimuth + 360) % 360
        diff
    }

    // Angle distance from pointing straight at Kaaba
    val angleDistance = remember(relativeAngle) {
        if (relativeAngle > 180) 360 - relativeAngle else relativeAngle
    }
    val isAligned = angleDistance <= 5f

    // Trigger haptic click when locking into Qibla alignment
    var wasAligned by remember { mutableStateOf(false) }
    LaunchedEffect(isAligned) {
        if (isAligned && !wasAligned) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        wasAligned = isAligned
    }

    val animatedRotation by animateFloatAsState(
        targetValue = -azimuth + qiblaBearing,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "qibla_rotation"
    )

    val isDarkTheme = MaterialTheme.colorScheme.background.red * 0.299f +
                      MaterialTheme.colorScheme.background.green * 0.587f +
                      MaterialTheme.colorScheme.background.blue * 0.114f < 0.5f

    val qiblaHeaderBg = if (isDarkTheme) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary
    val qiblaHeaderTitle = if (isDarkTheme) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary
    val qiblaHeaderAccent = MaterialTheme.colorScheme.secondary

    val dialBgColor = if (isDarkTheme) Color(0xFF131A16) else Color(0xFFFFFFFF)
    val inkColor = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF000000)
    val accentAlignedColor = Color(0xFF10B981) // Emerald Green

    val activeInkColor by animateColorAsState(
        targetValue = if (isAligned) accentAlignedColor else inkColor,
        animationSpec = tween(300),
        label = "ink_color"
    )

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
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar with Back Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "Al-Quran Majeed",
                fontFamily = HandmadeBrushesFontFamily,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Prominent Header Hero Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(20.dp),
            color = qiblaHeaderBg,
            border = BorderStroke(1.5.dp, qiblaHeaderAccent),
            shadowElevation = 4.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Decorative Background Watermark Icon
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = null,
                    tint = qiblaHeaderTitle.copy(alpha = 0.08f),
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
                        text = "Qibla Direction",
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = qiblaHeaderTitle,
                        letterSpacing = 0.8.sp,
                        textAlign = TextAlign.Center
                    )

                    // Decorative Gold Divider & Star Ornaments
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(1.5.dp)
                                .background(qiblaHeaderAccent)
                        )
                        Text(
                            text = "✦",
                            fontSize = 12.sp,
                            color = qiblaHeaderAccent
                        )
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(1.5.dp)
                                .background(qiblaHeaderAccent)
                        )
                    }

                    // Alignment Status Badge inside Hero Card
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.35f),
                        border = BorderStroke(
                            1.dp,
                            if (isAligned) accentAlignedColor else Color(0xFFD4AF37).copy(alpha = 0.7f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isAligned) Icons.Default.CheckCircle else Icons.Default.NearMe,
                                contentDescription = null,
                                tint = if (isAligned) accentAlignedColor else Color(0xFFD4AF37),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isAligned) "Aligned with Qibla" else "Point phone towards Qibla",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAligned) accentAlignedColor else Color(0xFFD4AF37),
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Main Circular Qibla Compass matching IMG-20260821-WA0000.jpg
        Box(
            modifier = Modifier
                .size(310.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                color = dialBgColor,
                shadowElevation = if (isAligned) 14.dp else 6.dp,
                border = BorderStroke(
                    width = 4.dp,
                    color = activeInkColor
                )
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val outerRadius = size.width / 2f
                    val innerRingRadius = outerRadius - 14.dp.toPx()
                    val tickBaseRadius = innerRingRadius - 2.dp.toPx()

                    // Rotate the entire dial so that the Pointer + Kaaba points towards Qibla/Makkah
                    rotate(degrees = animatedRotation, pivot = center) {

                        // 1. Inner Concentric Ring
                        drawCircle(
                            color = activeInkColor,
                            radius = innerRingRadius,
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // 2. Compass Dial Radial Tick Marks
                        // 360 degrees: 4 Major (every 90°), 8 Medium (every 30°), Minor ticks every 10°
                        for (deg in 0 until 360 step 10) {
                            val rad = Math.toRadians(deg.toDouble() - 90.0) // 0 is top
                            val isMajor = deg % 90 == 0
                            val isMedium = deg % 30 == 0 && !isMajor

                            val tickLen = when {
                                isMajor -> 18.dp.toPx()
                                isMedium -> 12.dp.toPx()
                                else -> 7.dp.toPx()
                            }
                            val strokeW = when {
                                isMajor -> 4.dp.toPx()
                                isMedium -> 2.5.dp.toPx()
                                else -> 1.8.dp.toPx()
                            }

                            val startX = center.x + (tickBaseRadius - tickLen) * cos(rad).toFloat()
                            val startY = center.y + (tickBaseRadius - tickLen) * sin(rad).toFloat()
                            val endX = center.x + tickBaseRadius * cos(rad).toFloat()
                            val endY = center.y + tickBaseRadius * sin(rad).toFloat()

                            drawLine(
                                color = activeInkColor,
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = strokeW,
                                cap = StrokeCap.Square
                            )
                        }

                        // 3. Central Majestic 3D Isometric Kaaba with Golden Kiswah, Bab al-Kaaba & Pointer Arrow
                        val kaabaScale = 72.dp.toPx()

                        // Metallic Gold & Kiswah Palette
                        val goldColor = Color(0xFFD4AF37)
                        val lightGold = Color(0xFFFDE047)
                        val darkGold = Color(0xFF996515)
                        val kiswahLeft = if (isDarkTheme) Color(0xFF242A27) else Color(0xFF1A221E)
                        val kiswahRight = if (isDarkTheme) Color(0xFF161A18) else Color(0xFF0F1412)
                        val marbleRoof = Color(0xFFF8FAFC)
                        val marbleBase = Color(0xFFE2E8F0)
                        val silverFrame = Color(0xFFCBD5E1)

                        // A. Upward Directional Pointer Arrow (Golden Pointer to Qibla)
                        val arrowTipY = center.y - kaabaScale * 1.05f
                        val arrowBaseLeftX = center.x - kaabaScale * 0.38f
                        val arrowBaseRightX = center.x + kaabaScale * 0.38f
                        val arrowBaseY = center.y - kaabaScale * 0.52f
                        val arrowInnerY = center.y - kaabaScale * 0.65f

                        val arrowPath = Path().apply {
                            moveTo(center.x, arrowTipY)
                            lineTo(arrowBaseLeftX, arrowBaseY)
                            lineTo(center.x, arrowInnerY)
                            lineTo(arrowBaseRightX, arrowBaseY)
                            close()
                        }
                        drawPath(arrowPath, color = goldColor, style = Fill)
                        drawPath(arrowPath, color = lightGold, style = Stroke(width = 1.5.dp.toPx()))

                        // B. Kaaba Top Roof Face (Isometric Marble Diamond)
                        val roofTop = Offset(center.x, center.y - kaabaScale * 0.44f)
                        val roofRight = Offset(center.x + kaabaScale * 0.60f, center.y - kaabaScale * 0.14f)
                        val roofBottom = Offset(center.x, center.y + kaabaScale * 0.16f)
                        val roofLeft = Offset(center.x - kaabaScale * 0.60f, center.y - kaabaScale * 0.14f)

                        // Marble Base / Shadherwan (Sloping base foundation at bottom)
                        val baseBottomCenter = Offset(center.x, center.y + kaabaScale * 0.82f)
                        val baseBottomLeft = Offset(center.x - kaabaScale * 0.66f, center.y + kaabaScale * 0.50f)
                        val baseBottomRight = Offset(center.x + kaabaScale * 0.66f, center.y + kaabaScale * 0.50f)

                        val basePath = Path().apply {
                            moveTo(roofLeft.x - 4.dp.toPx(), roofLeft.y + kaabaScale * 0.58f)
                            lineTo(roofBottom.x, roofBottom.y + kaabaScale * 0.66f)
                            lineTo(roofRight.x + 4.dp.toPx(), roofRight.y + kaabaScale * 0.58f)
                            lineTo(baseBottomRight.x, baseBottomRight.y)
                            lineTo(baseBottomCenter.x, baseBottomCenter.y)
                            lineTo(baseBottomLeft.x, baseBottomLeft.y)
                            close()
                        }
                        drawPath(basePath, color = marbleBase, style = Fill)

                        // Roof Marble Slab
                        val roofPath = Path().apply {
                            moveTo(roofTop.x, roofTop.y)
                            lineTo(roofRight.x, roofRight.y)
                            lineTo(roofBottom.x, roofBottom.y)
                            lineTo(roofLeft.x, roofLeft.y)
                            close()
                        }
                        drawPath(roofPath, color = marbleRoof, style = Fill)
                        drawPath(roofPath, color = goldColor, style = Stroke(width = 2.dp.toPx()))

                        // Inner Roof Recessed Gold Rim
                        val innerRoofTop = Offset(center.x, center.y - kaabaScale * 0.36f)
                        val innerRoofRight = Offset(center.x + kaabaScale * 0.46f, center.y - kaabaScale * 0.14f)
                        val innerRoofBottom = Offset(center.x, center.y + kaabaScale * 0.08f)
                        val innerRoofLeft = Offset(center.x - kaabaScale * 0.46f, center.y - kaabaScale * 0.14f)

                        val innerRoofPath = Path().apply {
                            moveTo(innerRoofTop.x, innerRoofTop.y)
                            lineTo(innerRoofRight.x, innerRoofRight.y)
                            lineTo(innerRoofBottom.x, innerRoofBottom.y)
                            lineTo(innerRoofLeft.x, innerRoofLeft.y)
                            close()
                        }
                        drawPath(innerRoofPath, color = goldColor.copy(alpha = 0.25f), style = Fill)
                        drawPath(innerRoofPath, color = goldColor, style = Stroke(width = 1.5.dp.toPx()))

                        // C. Kaaba Left Wall (Kiswah - Lighted Side)
                        val wallBottomCenter = Offset(center.x, center.y + kaabaScale * 0.74f)
                        val wallBottomLeft = Offset(center.x - kaabaScale * 0.60f, center.y + kaabaScale * 0.44f)
                        val wallBottomRight = Offset(center.x + kaabaScale * 0.60f, center.y + kaabaScale * 0.44f)

                        val leftWallPath = Path().apply {
                            moveTo(roofLeft.x, roofLeft.y)
                            lineTo(roofBottom.x, roofBottom.y)
                            lineTo(wallBottomCenter.x, wallBottomCenter.y)
                            lineTo(wallBottomLeft.x, wallBottomLeft.y)
                            close()
                        }
                        drawPath(leftWallPath, color = kiswahLeft, style = Fill)

                        // D. Kaaba Right Wall (Kiswah - Shadowed Side)
                        val rightWallPath = Path().apply {
                            moveTo(roofBottom.x, roofBottom.y)
                            lineTo(roofRight.x, roofRight.y)
                            lineTo(wallBottomRight.x, wallBottomRight.y)
                            lineTo(wallBottomCenter.x, wallBottomCenter.y)
                            close()
                        }
                        drawPath(rightWallPath, color = kiswahRight, style = Fill)

                        // E. Gold Kiswah Embroidery Belt (Hizam) across both walls
                        val leftHizamTop = Offset(roofLeft.x, roofLeft.y + 11.dp.toPx())
                        val leftHizamBottom = Offset(roofBottom.x, roofBottom.y + 11.dp.toPx())
                        val rightHizamTop = Offset(roofRight.x, roofRight.y + 11.dp.toPx())

                        // Left Belt Path
                        val leftBeltPath = Path().apply {
                            moveTo(leftHizamTop.x, leftHizamTop.y)
                            lineTo(leftHizamBottom.x, leftHizamBottom.y)
                            lineTo(leftHizamBottom.x, leftHizamBottom.y + 11.dp.toPx())
                            lineTo(leftHizamTop.x, leftHizamTop.y + 11.dp.toPx())
                            close()
                        }
                        drawPath(leftBeltPath, color = goldColor, style = Fill)
                        drawPath(leftBeltPath, color = lightGold, style = Stroke(width = 1.dp.toPx()))

                        // Right Belt Path
                        val rightBeltPath = Path().apply {
                            moveTo(leftHizamBottom.x, leftHizamBottom.y)
                            lineTo(rightHizamTop.x, rightHizamTop.y)
                            lineTo(rightHizamTop.x, rightHizamTop.y + 11.dp.toPx())
                            lineTo(leftHizamBottom.x, leftHizamBottom.y + 11.dp.toPx())
                            close()
                        }
                        drawPath(rightBeltPath, color = darkGold, style = Fill)
                        drawPath(rightBeltPath, color = lightGold, style = Stroke(width = 1.dp.toPx()))

                        // F. Bab al-Kaaba (Golden Door of Kaaba on Right Wall)
                        val doorWidth = kaabaScale * 0.22f
                        val doorHeight = kaabaScale * 0.36f
                        val doorLeftX = center.x + kaabaScale * 0.14f
                        val doorTopY = center.y + kaabaScale * 0.22f

                        val doorPath = Path().apply {
                            moveTo(doorLeftX, doorTopY)
                            lineTo(doorLeftX + doorWidth, doorTopY - doorWidth * 0.45f)
                            lineTo(doorLeftX + doorWidth, doorTopY - doorWidth * 0.45f + doorHeight)
                            lineTo(doorLeftX, doorTopY + doorHeight)
                            close()
                        }
                        drawPath(doorPath, color = goldColor, style = Fill)
                        drawPath(doorPath, color = lightGold, style = Stroke(width = 1.5.dp.toPx()))

                        // Door Center Line / Panels
                        drawLine(
                            color = darkGold,
                            start = Offset(doorLeftX + doorWidth * 0.5f, doorTopY - doorWidth * 0.22f),
                            end = Offset(doorLeftX + doorWidth * 0.5f, doorTopY + doorHeight - doorWidth * 0.22f),
                            strokeWidth = 1.5.dp.toPx()
                        )

                        // G. Hajar al-Aswad (Silver Framed Black Stone at Center Corner)
                        val stoneCenter = Offset(center.x, center.y + kaabaScale * 0.62f)
                        drawCircle(color = silverFrame, radius = 5.dp.toPx(), center = stoneCenter)
                        drawCircle(color = Color.Black, radius = 2.5.dp.toPx(), center = stoneCenter)

                        // H. Meezab al-Rahmah (Golden Water Spout on Top Left Roof Line)
                        val spoutStart = Offset(roofLeft.x + (roofBottom.x - roofLeft.x) * 0.40f, roofLeft.y + (roofBottom.y - roofLeft.y) * 0.40f)
                        drawLine(
                            color = lightGold,
                            start = spoutStart,
                            end = Offset(spoutStart.x - 6.dp.toPx(), spoutStart.y + 4.dp.toPx()),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Prominent Degree & Bearing readout card matching Action Card theme
        val qiblaCardBg = if (isDarkTheme) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary
        val qiblaCardBorder = if (isDarkTheme) MaterialTheme.colorScheme.outlineVariant else qiblaHeaderAccent
        val qiblaTitleText = if (isDarkTheme) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary
        val qiblaSubtitleText = if (isDarkTheme) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = qiblaCardBg,
            border = BorderStroke(1.dp, qiblaCardBorder),
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Compass Heading",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = qiblaSubtitleText,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${azimuth.toInt()}°",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = qiblaTitleText,
                            fontSize = 22.sp
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.5.dp)
                        .background(qiblaCardBorder)
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Qibla Direction",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = qiblaSubtitleText,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NearMe,
                            contentDescription = null,
                            tint = if (isAligned) accentAlignedColor else qiblaSubtitleText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${qiblaBearing.toInt()}°",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isAligned) accentAlignedColor else qiblaTitleText,
                                fontSize = 22.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Calculates the forward azimuth bearing from user's (latitude, longitude) to the Kaaba in Makkah
 * Kaaba Coordinates: Lat = 21.422487° N, Long = 39.826206° E
 */
private fun calculateQiblaBearing(lat: Double, lng: Double): Float {
    val phi1 = Math.toRadians(lat)
    val lambda1 = Math.toRadians(lng)
    val phi2 = Math.toRadians(21.422487)
    val lambda2 = Math.toRadians(39.826206)

    val deltaLambda = lambda2 - lambda1
    val y = sin(deltaLambda) * cos(phi2)
    val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
    val bearingRad = atan2(y, x)
    val bearingDeg = Math.toDegrees(bearingRad)
    return ((bearingDeg + 360) % 360).toFloat()
}

