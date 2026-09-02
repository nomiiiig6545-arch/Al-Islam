package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.data.mushaf.IndoPakMushafData
import com.example.data.mushaf.MushafPageCacheManager
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min

enum class PageCurlDirection {
    FORWARD,  // Next page (Page 30 -> 31: right-to-left peel)
    BACKWARD  // Previous page (Page 30 -> 29: left-to-right peel)
}

/**
 * Controller to trigger page curl animations programmatically from buttons or keys
 */
class PageCurlState {
    var animateToNext: (suspend () -> Unit)? = null
    var animateToPrevious: (suspend () -> Unit)? = null
}

@Composable
fun rememberPageCurlState(): PageCurlState = remember { PageCurlState() }

/**
 * Authentic 16-Line Mushaf Book Page-Curl Reader matching exact RTL Quran page flips.
 * - Smooth, curved organic corner/edge fold with realistic cylindrical paper shading.
 * - Tap zones: Left 25% = Next Page, Right 25% = Previous Page, Center = Controls toggle.
 * - RTL Swipe: Swiping Left (<---) peels page to Next, Swiping Right (--->) peels page to Previous.
 * - Zero-latency synchronized paper page-turn sound effect.
 */
@Composable
fun MushafPageCurlReader(
    currentPage: Int,
    isDark: Boolean,
    isNightMode: Boolean,
    overlayColor: Color,
    overlayAlpha: Float,
    modifier: Modifier = Modifier,
    pageCurlState: PageCurlState? = null,
    onPageChanged: (Int) -> Unit,
    onTapPage: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Curl progress: 0f = flat resting, 1f = fully peeled
    val curlProgress = remember { Animatable(0f) }
    var curlDirection by remember { mutableStateOf(PageCurlDirection.FORWARD) }
    var targetPage by remember(currentPage) { mutableIntStateOf(currentPage) }
    val isAnimating = curlProgress.value > 0.001f

    val context = androidx.compose.ui.platform.LocalContext.current

    // Reset curl progress & warm cache when page changes
    LaunchedEffect(currentPage) {
        curlProgress.snapTo(0f)
        PageTurnSoundManager.init(context)
        MushafPageCacheManager.initializeIndex(context)
        MushafPageCacheManager.preloadAdjacentPages(context, currentPage)
    }

    // Connect imperative button triggers
    LaunchedEffect(currentPage, pageCurlState) {
        pageCurlState?.animateToNext = {
            if (currentPage < IndoPakMushafData.TOTAL_PAGES && !isAnimating) {
                curlDirection = PageCurlDirection.FORWARD
                targetPage = currentPage + 1
                PageTurnSoundManager.play(context)
                curlProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing)
                )
                onPageChanged(targetPage)
            }
        }
        pageCurlState?.animateToPrevious = {
            if (currentPage > 1 && !isAnimating) {
                curlDirection = PageCurlDirection.BACKWARD
                targetPage = currentPage - 1
                PageTurnSoundManager.play(context)
                curlProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing)
                )
                onPageChanged(targetPage)
            }
        }
    }

    val effectiveDark = isNightMode
    val pageThemeBg = if (effectiveDark) Color(0xFF000000) else Color(0xFFFAF6EE)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(pageThemeBg)
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val progress = curlProgress.value

        // 1. UNDERLYING READER VISUAL LAYERS (Page curl animations and image rendering)
        if (!isAnimating) {
            // Resting state: 100% full uncropped current page
            MushafPageImageView(
                pageNumber = currentPage,
                isDark = isDark,
                isNightMode = isNightMode,
                overlayColor = overlayColor,
                overlayAlpha = overlayAlpha,
                onTapPage = {}
            )
        } else {
                if (curlDirection == PageCurlDirection.FORWARD) {
                // FORWARD (RTL Arabic Quran: Next Page - e.g. Page 7 -> 8)
                // Current page (Page 7) lifts from the left edge (x=0) and folds towards the right (x=widthPx).
                // Underneath on the left, Page 8 (targetPage) is uncovered.
                val foldX = widthPx * progress
                val curveRadius = min(50f * density.density, foldX)

                // Path for the unpeeled top page (Right of fold line foldX)
                val topPagePath = Path().apply {
                    moveTo(foldX, 0f)
                    lineTo(widthPx, 0f)
                    lineTo(widthPx, heightPx)
                    lineTo(foldX, heightPx)
                    quadraticTo(
                        foldX - curveRadius * 0.35f, heightPx * 0.5f,
                        foldX, 0f
                    )
                    close()
                }

                // 1. Bottom Layer: Underneath Next Page (Page 8, uncovered on the left side of foldX)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(pageThemeBg)
                ) {
                    MushafPageImageView(
                        pageNumber = targetPage,
                        isDark = isDark,
                        isNightMode = isNightMode,
                        overlayColor = overlayColor,
                        overlayAlpha = overlayAlpha,
                        onTapPage = {}
                    )

                    // Soft drop shadow cast onto bottom page (left of foldX)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawWithContent {
                                drawContent()
                                val shadowWidth = 40.dp.toPx()
                                val shadowStart = (foldX - shadowWidth).coerceAtLeast(0f)
                                if (foldX > 0f) {
                                    drawRect(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.08f),
                                                Color.Black.copy(alpha = 0.28f)
                                            ),
                                            startX = shadowStart,
                                            endX = foldX
                                        ),
                                        topLeft = Offset(shadowStart, 0f),
                                        size = Size(foldX - shadowStart, size.height)
                                    )
                                }
                            }
                    )
                }

                // 2. Top Layer: Current Page (Page 7, sitting on the right side of foldX)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            clipPath(topPagePath) {
                                this@drawWithContent.drawContent()

                                // Soft curvature shadow along fold (right of foldX)
                                if (foldX < widthPx) {
                                    val curlEnd = (foldX + curveRadius * 1.4f).coerceAtMost(widthPx)
                                    drawRect(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.24f),
                                                Color.Black.copy(alpha = 0.08f),
                                                Color.Transparent
                                            ),
                                            startX = foldX,
                                            endX = curlEnd
                                        ),
                                        topLeft = Offset(foldX, 0f),
                                        size = Size(curlEnd - foldX, size.height)
                                    )

                                    // Subtle specular light highlight
                                    val highlightWidth = 18.dp.toPx()
                                    val highlightStart = (foldX + highlightWidth * 0.4f).coerceAtMost(widthPx)
                                    drawRect(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.White.copy(alpha = 0.25f),
                                                Color.Transparent
                                            ),
                                            startX = highlightStart,
                                            endX = (highlightStart + highlightWidth).coerceAtMost(widthPx)
                                        ),
                                        topLeft = Offset(highlightStart, 0f),
                                        size = Size(highlightWidth, size.height)
                                    )
                                }
                            }

                            // Gilded paper edge stroke along the curved fold line
                            if (foldX in 1f..(widthPx - 1f)) {
                                val edgePath = Path().apply {
                                    moveTo(foldX, 0f)
                                    quadraticTo(
                                        foldX - curveRadius * 0.35f, heightPx * 0.5f,
                                        foldX, heightPx
                                    )
                                }
                                drawPath(
                                    path = edgePath,
                                    color = if (isDark || isNightMode) Color(0xFFD4A343) else Color(0xFFC4A462),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                        width = 1.6.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                )
                            }
                        }
                ) {
                    MushafPageImageView(
                        pageNumber = currentPage,
                        isDark = isDark,
                        isNightMode = isNightMode,
                        overlayColor = overlayColor,
                        overlayAlpha = overlayAlpha,
                        onTapPage = {}
                    )
                }
            } else {
                // BACKWARD (RTL Arabic Quran: Previous Page - e.g. Page 8 -> 7)
                // Previous page (Page 7) folds back in from the right edge (x=widthPx) towards the left (x=0).
                val foldX = widthPx * (1f - progress)
                val curveRadius = min(50f * density.density, widthPx - foldX)

                val prevPagePath = Path().apply {
                    moveTo(foldX, 0f)
                    lineTo(widthPx, 0f)
                    lineTo(widthPx, heightPx)
                    lineTo(foldX, heightPx)
                    quadraticTo(
                        foldX - curveRadius * 0.35f, heightPx * 0.5f,
                        foldX, 0f
                    )
                    close()
                }

                // 1. Bottom Layer: Current Page (Page 8, sitting full screen)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(pageThemeBg)
                ) {
                    MushafPageImageView(
                        pageNumber = currentPage,
                        isDark = isDark,
                        isNightMode = isNightMode,
                        overlayColor = overlayColor,
                        overlayAlpha = overlayAlpha,
                        onTapPage = {}
                    )
                }

                // 2. Top Layer: Returning Previous Page (Page 7, covering right side from foldX to widthPx)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(pageThemeBg)
                        .drawWithContent {
                            clipPath(prevPagePath) {
                                this@drawWithContent.drawContent()

                                if (foldX < widthPx) {
                                    val curlEnd = (foldX + curveRadius * 1.4f).coerceAtMost(widthPx)
                                    drawRect(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.24f),
                                                Color.Black.copy(alpha = 0.08f),
                                                Color.Transparent
                                            ),
                                            startX = foldX,
                                            endX = curlEnd
                                        ),
                                        topLeft = Offset(foldX, 0f),
                                        size = Size(curlEnd - foldX, size.height)
                                    )
                                }
                            }

                            // Soft drop shadow cast onto bottom page (left of foldX)
                            val shadowWidth = 40.dp.toPx()
                            val shadowStart = (foldX - shadowWidth).coerceAtLeast(0f)
                            if (foldX > 0f) {
                                drawRect(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.08f),
                                            Color.Black.copy(alpha = 0.28f)
                                        ),
                                        startX = shadowStart,
                                        endX = foldX
                                    ),
                                    topLeft = Offset(shadowStart, 0f),
                                    size = Size(foldX - shadowStart, size.height)
                                )
                            }

                            // Gilded paper edge stroke along fold line
                            if (foldX in 1f..(widthPx - 1f)) {
                                val edgePath = Path().apply {
                                    moveTo(foldX, 0f)
                                    quadraticTo(
                                        foldX - curveRadius * 0.35f, heightPx * 0.5f,
                                        foldX, heightPx
                                    )
                                }
                                drawPath(
                                    path = edgePath,
                                    color = if (isDark || isNightMode) Color(0xFFD4A343) else Color(0xFFC4A462),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                        width = 1.6.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                )
                            }
                        }
                ) {
                    MushafPageImageView(
                        pageNumber = targetPage,
                        isDark = isDark,
                        isNightMode = isNightMode,
                        overlayColor = overlayColor,
                        overlayAlpha = overlayAlpha,
                        onTapPage = {}
                    )
                }
            }
        }

        // 2. TRANSPARENT FULL-SCREEN TOUCH OVERLAY COMPONENT
        // Positioned across 100% of the viewport. Captures touches, taps, and drag gestures cleanly.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemGestureExclusion()
                .pointerInput(currentPage) {
                    val screenWidthPx = size.width.toFloat()

                    awaitEachGesture {
                        val down = awaitFirstDown(pass = PointerEventPass.Initial, requireUnconsumed = false)
                        down.consume()
                        var totalDragPx = 0f
                        var totalDragPy = 0f
                        var gestureStarted = false
                        val gestureStartTime = System.currentTimeMillis()
                        val downX = down.position.x

                        var gestureEvaluated = false

                        while (true) {
                            val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break

                            val dx = change.positionChange().x
                            val dy = change.positionChange().y

                            event.changes.forEach { it.consume() }

                            if (!change.pressed) {
                                val duration = System.currentTimeMillis() - gestureStartTime
                                val currentProg = curlProgress.value

                                if (!gestureStarted && duration < 280 && abs(totalDragPx) < 14f && abs(totalDragPy) < 14f) {
                                    // Tap handling
                                    if (downX < screenWidthPx * 0.35f && currentPage < IndoPakMushafData.TOTAL_PAGES) {
                                        // Tap Left 35% -> Turn to Next Page (RTL)
                                        coroutineScope.launch {
                                            curlDirection = PageCurlDirection.FORWARD
                                            targetPage = currentPage + 1
                                            PageTurnSoundManager.play(context)
                                            curlProgress.animateTo(
                                                targetValue = 1f,
                                                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                                            )
                                            onPageChanged(targetPage)
                                        }
                                    } else if (downX > screenWidthPx * 0.65f && currentPage > 1) {
                                        // Tap Right 35% -> Turn to Previous Page (RTL)
                                        coroutineScope.launch {
                                            curlDirection = PageCurlDirection.BACKWARD
                                            targetPage = currentPage - 1
                                            PageTurnSoundManager.play(context)
                                            curlProgress.animateTo(
                                                targetValue = 1f,
                                                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                                            )
                                            onPageChanged(targetPage)
                                        }
                                    } else {
                                        // Center Tap -> Toggle controls / settings
                                        onTapPage()
                                    }
                                } else if (gestureStarted) {
                                    coroutineScope.launch {
                                        if (currentProg > 0.12f || (duration < 380 && currentProg > 0.04f)) {
                                            PageTurnSoundManager.play(context)
                                            curlProgress.animateTo(
                                                targetValue = 1f,
                                                animationSpec = tween(
                                                    durationMillis = (280 * (1f - currentProg)).toInt().coerceIn(60, 280),
                                                    easing = FastOutSlowInEasing
                                                )
                                            )
                                            onPageChanged(targetPage)
                                        } else {
                                            curlProgress.animateTo(
                                                targetValue = 0f,
                                                animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing)
                                            )
                                        }
                                    }
                                }
                                break
                            }

                            totalDragPx += dx
                            totalDragPy += dy

                            if (!gestureEvaluated && (abs(totalDragPx) > 6f || abs(totalDragPy) > 6f)) {
                                gestureEvaluated = true
                                if (abs(totalDragPx) > abs(totalDragPy)) {
                                    if ((totalDragPx > 0 || downX < screenWidthPx * 0.5f) && currentPage < IndoPakMushafData.TOTAL_PAGES) {
                                        // Dragging from Left -> Right (--->) -> Next Page (FORWARD / RTL)
                                        curlDirection = PageCurlDirection.FORWARD
                                        targetPage = currentPage + 1
                                        gestureStarted = true
                                    } else if ((totalDragPx < 0 || downX >= screenWidthPx * 0.5f) && currentPage > 1) {
                                        // Dragging from Right -> Left (<---) -> Previous Page (BACKWARD / RTL)
                                        curlDirection = PageCurlDirection.BACKWARD
                                        targetPage = currentPage - 1
                                        gestureStarted = true
                                    }
                                }
                            }

                            if (gestureStarted) {
                                if (curlDirection == PageCurlDirection.FORWARD && targetPage == currentPage + 1) {
                                    val newProgress = (totalDragPx / screenWidthPx).coerceIn(0f, 1f)
                                    coroutineScope.launch { curlProgress.snapTo(newProgress) }
                                } else if (curlDirection == PageCurlDirection.BACKWARD && targetPage == currentPage - 1) {
                                    val newProgress = (-totalDragPx / screenWidthPx).coerceIn(0f, 1f)
                                    coroutineScope.launch { curlProgress.snapTo(newProgress) }
                                }
                            }
                        }
                    }
                }
        )
    }
}

