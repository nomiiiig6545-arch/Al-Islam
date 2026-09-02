package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(ExperimentalTextApi::class)
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2500)
        onSplashFinished()
    }

    val primaryColor = Color(0xFFF2CA50)
    val goldStart = Color(0xFFD4AF37)
    val goldMid = Color(0xFFFFF5CC)
    val goldEnd = Color(0xFFD4AF37)

    val dot1Alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )

    val dot2Alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut, delayMillis = 200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )

    val dot3Alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut, delayMillis = 400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Background color #002B24
                drawRect(color = Color(0xFF002B24))
                // Linear gradient
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF004D40).copy(alpha = 0.8f),
                            Color(0xFF002B24)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    )
                )
                // Radial gradient
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color(0xFF002B24)),
                        center = Offset(size.width / 2, size.height / 2),
                        radius = size.width
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Top Calligraphy
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
                .fillMaxWidth()
                .height(80.dp)
                .alpha(0.8f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(200.dp, 60.dp)) {
                val scaleX = size.width / 200f
                val scaleY = size.height / 60f
                
                val path = Path().apply {
                    moveTo(100f * scaleX, 20f * scaleY)
                    cubicTo(120f * scaleX, 10f * scaleY, 140f * scaleX, 30f * scaleY, 160f * scaleX, 20f * scaleY)
                    cubicTo(170f * scaleX, 15f * scaleY, 180f * scaleX, 25f * scaleY, 190f * scaleX, 20f * scaleY)
                    cubicTo(195f * scaleX, 18f * scaleY, 198f * scaleX, 22f * scaleY, 195f * scaleX, 25f * scaleY)
                    cubicTo(185f * scaleX, 35f * scaleY, 160f * scaleX, 40f * scaleY, 140f * scaleX, 35f * scaleY)
                    cubicTo(120f * scaleX, 30f * scaleY, 100f * scaleX, 45f * scaleY, 80f * scaleX, 40f * scaleY)
                    cubicTo(60f * scaleX, 35f * scaleY, 40f * scaleX, 45f * scaleY, 20f * scaleX, 40f * scaleY)
                    cubicTo(10f * scaleX, 38f * scaleY, 5f * scaleX, 35f * scaleY, 10f * scaleX, 30f * scaleY)
                    cubicTo(15f * scaleX, 25f * scaleY, 25f * scaleX, 35f * scaleY, 40f * scaleX, 30f * scaleY)
                    cubicTo(60f * scaleX, 25f * scaleY, 80f * scaleX, 30f * scaleY, 100f * scaleX, 20f * scaleY)
                    close()
                }
                drawPath(path = path, color = primaryColor)
                drawCircle(color = primaryColor, radius = 3f * scaleX, center = Offset(95f * scaleX, 15f * scaleY))
                drawCircle(color = primaryColor, radius = 3f * scaleX, center = Offset(115f * scaleX, 45f * scaleY))
                drawCircle(color = primaryColor, radius = 2f * scaleX, center = Offset(45f * scaleX, 15f * scaleY))
                drawCircle(color = primaryColor, radius = 2f * scaleX, center = Offset(155f * scaleX, 45f * scaleY))
            }
        }

        // Centerpiece Emblem
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .background(Color(0xFF0B5345).copy(alpha = 0.1f))
                .border(1.dp, primaryColor.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Gold Glow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(40.dp)
                    .background(Color(0xD4AF37).copy(alpha = 0.2f), CircleShape)
            )

            // Inner dashed border
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .border(1.dp, primaryColor.copy(alpha = 0.1f), CircleShape) // Can't easily dash in Modifier.border, let's just make it thin
            )
            
            // Inner solid border
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .border(1.dp, primaryColor.copy(alpha = 0.3f), CircleShape)
            )

            // Stars / Polygon svg
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2
                val cy = size.height / 2
                val s = size.width / 100f

                val poly1 = Path().apply {
                    moveTo(50f * s, 5f * s)
                    lineTo(60f * s, 40f * s)
                    lineTo(95f * s, 50f * s)
                    lineTo(60f * s, 60f * s)
                    lineTo(50f * s, 95f * s)
                    lineTo(40f * s, 60f * s)
                    lineTo(5f * s, 50f * s)
                    lineTo(40f * s, 40f * s)
                    close()
                }
                
                val poly2 = Path().apply {
                    moveTo(50f * s, 15f * s)
                    lineTo(55f * s, 45f * s)
                    lineTo(85f * s, 50f * s)
                    lineTo(55f * s, 55f * s)
                    lineTo(50f * s, 85f * s)
                    lineTo(45f * s, 55f * s)
                    lineTo(15f * s, 50f * s)
                    lineTo(45f * s, 45f * s)
                    close()
                }
                
                drawPath(path = poly1, color = primaryColor.copy(alpha = 0.2f), style = Stroke(width = 0.5f * s))
                
                // Poly2 is rotated 45 degrees
                withTransform({
                    rotate(45f, Offset(cx, cy))
                }) {
                    drawPath(path = poly2, color = primaryColor.copy(alpha = 0.2f), style = Stroke(width = 0.5f * s))
                }
            }

            Text(
                text = "Al-Quran\nMajeed",
                style = TextStyle(
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 44.sp,
                    brush = Brush.linearGradient(
                        colors = listOf(goldStart, goldMid, goldEnd)
                    )
                )
            )
        }

        // Bottom Loading Dots
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(modifier = Modifier.size(12.dp).scale(dot1Alpha).alpha(dot1Alpha).background(primaryColor, CircleShape))
            Box(modifier = Modifier.size(12.dp).scale(dot2Alpha).alpha(dot2Alpha).background(primaryColor, CircleShape))
            Box(modifier = Modifier.size(12.dp).scale(dot3Alpha).alpha(dot3Alpha).background(primaryColor, CircleShape))
        }
    }
}
