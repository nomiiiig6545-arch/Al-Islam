package com.example.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Size
import com.example.R
import com.example.data.mushaf.IndoPakMushafData
import com.example.data.mushaf.MushafPageCacheManager
import com.example.data.mushaf.MushafPageInfo
import com.example.ui.theme.ArabicFontFamily
import com.example.ui.theme.UrduFontFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * High-performance 16-line Mushaf Page View.
 * 1. Leverages MushafPageCacheManager (L2 memory cache + instant pre-indexed local storage).
 * 2. Pre-loads adjacent pages for 0ms lag-free swiping.
 * 3. Completely bypasses any runtime network validation when local assets or downloaded files exist.
 * 4. High-contrast OLED HD+ Dark Mode with deep blacks and luminous crisp calligraphy.
 * 5. Supports pinch-to-zoom (up to 4x), double-tap zoom, and smooth gestures.
 */
@Composable
fun MushafPageImageView(
    pageNumber: Int,
    isDark: Boolean = false,
    isNightMode: Boolean = false,
    overlayColor: Color = Color.Transparent,
    overlayAlpha: Float = 0f,
    enableInternalGestures: Boolean = false,
    onTapPage: () -> Unit = {}
) {
    val context = LocalContext.current
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Reset zoom when page changes
    LaunchedEffect(pageNumber) {
        scale = 1f
        offset = Offset.Zero
    }

    val effectiveDark = isNightMode
    val backgroundColor = if (effectiveDark) Color(0xFF000000) else Color(0xFFFAF6EE)
    val pageInfo = remember(pageNumber) { IndoPakMushafData.getPageInfo(pageNumber) }
    val downloadManager = remember { com.example.data.mushaf.MushafPageDownloadManager.getInstance(context) }

    // L2 Memory Cache + Instant Sync Local Index lookup on frame 0
    var localBitmap by remember(pageNumber) {
        mutableStateOf(
            MushafPageCacheManager.getMemoryCachedBitmap(pageNumber)
                ?: MushafPageCacheManager.getPageBitmapSync(context, pageNumber)
        )
    }

    LaunchedEffect(pageNumber) {
        if (localBitmap == null) {
            localBitmap = MushafPageCacheManager.getPageBitmap(context, pageNumber)
        }
        MushafPageCacheManager.preloadAdjacentPages(context, pageNumber)
    }

    // Resolve high-resolution 16-line page image URLs for remote fallback
    val remoteImageUrls = remember(pageNumber) { IndoPakMushafData.get16LinePageImageUrls(context, pageNumber) }
    var urlIndex by remember(pageNumber) { mutableIntStateOf(0) }
    val currentUrl = remoteImageUrls.getOrElse(urlIndex) { remoteImageUrls.first() }

    // Background pre-fetch / auto-cache current page to permanent storage if not yet offline
    LaunchedEffect(pageNumber) {
        MushafPageCacheManager.preloadAdjacentPages(context, pageNumber)
        if (!downloadManager.isPageOffline(pageNumber)) {
            downloadManager.downloadSinglePage(pageNumber)
        }
    }

    // Ultra HD Inverted Matrix for genuine Night Mode
    val colorFilter = remember(isNightMode) {
        if (isNightMode) {
            androidx.compose.ui.graphics.ColorFilter.colorMatrix(
                androidx.compose.ui.graphics.ColorMatrix(
                    floatArrayOf(
                        -1.25f,   0f,     0f,    0f, 275f,
                          0f,   -1.25f,   0f,    0f, 275f,
                          0f,     0f,   -1.25f,  0f, 275f,
                          0f,     0f,     0f,    1f,   0f
                    )
                )
            )
        } else null
    }

    val zoomModifier = if (enableInternalGestures && scale > 1.05f) {
        Modifier.pointerInput(pageNumber) {
            detectTransformGestures { _, pan, zoom, _ ->
                scale = (scale * zoom).coerceIn(1f, 4f)
                val maxOffset = (scale - 1f) * 500f
                if (scale > 1f) {
                    val newOffsetX = (offset.x + pan.x).coerceIn(-maxOffset, maxOffset)
                    val newOffsetY = (offset.y + pan.y).coerceIn(-maxOffset, maxOffset)
                    offset = Offset(newOffsetX, newOffsetY)
                } else {
                    offset = Offset.Zero
                }
            }
        }
    } else {
        Modifier
    }

    val tapModifier = if (enableInternalGestures) {
        Modifier.pointerInput(pageNumber) {
            detectTapGestures(
                onTap = { onTapPage() },
                onDoubleTap = {
                    scale = if (scale > 1.2f) 1f else 2.2f
                    offset = Offset.Zero
                }
            )
        }
    } else {
        Modifier
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .then(zoomModifier)
            .then(tapModifier),
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (pageNumber == 1) {
                // Page 1 is the custom Quran title and cover page
                Image(
                    painter = painterResource(id = R.drawable.img_mushaf_cover),
                    contentDescription = "Quran Cover & Title Page",
                    contentScale = ContentScale.FillBounds,
                    alignment = Alignment.Center,
                    colorFilter = colorFilter,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                )
            } else if (localBitmap != null) {
                // Instant zero-lag rendering from L2 Cache / Pre-indexed Bitmap
                Image(
                    bitmap = localBitmap!!,
                    contentDescription = "16-Line Quran Page $pageNumber",
                    contentScale = ContentScale.FillBounds,
                    alignment = Alignment.Center,
                    filterQuality = FilterQuality.High,
                    colorFilter = colorFilter,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                )
            } else {
                // Placeholder-free transition: Surface holds persistent background color behind AsyncImage
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(currentUrl)
                        .size(Size.ORIGINAL)
                        .precision(Precision.EXACT)
                        .crossfade(false)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .build(),
                    contentDescription = "16-Line Quran Page $pageNumber",
                    contentScale = ContentScale.FillBounds,
                    alignment = Alignment.Center,
                    filterQuality = FilterQuality.High,
                    colorFilter = colorFilter,
                    onState = { state ->
                        if (state is AsyncImagePainter.State.Error && urlIndex < remoteImageUrls.size - 1) {
                            urlIndex++
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                )
            }
        }

        // Adjustable Eye Comfort Page Overlay
        if (overlayAlpha > 0f && overlayColor != Color.Transparent && !isNightMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(overlayColor.copy(alpha = overlayAlpha))
            )
        }
    }
}

fun get16LinePageImageUrls(pageNumber: Int): List<String> {
    return IndoPakMushafData.get16LinePageImageUrls(null, pageNumber)
}

fun get16LinePageImageUrl(pageNumber: Int): String {
    return get16LinePageImageUrls(pageNumber).first()
}
