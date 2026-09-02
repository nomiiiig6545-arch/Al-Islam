package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.IndoPakFontFamily
import com.example.ui.theme.UrduFontFamily
import com.example.ui.theme.getQuranFontFamily

/**
 * Authentic 16-Line Pakistani Tajweed Mushaf Ornamental Frame.
 * Features the signature magenta scalloped florets, green arabesque vines, and gold ruled lines.
 */
@Composable
fun IndoPakMushafFrame(
    modifier: Modifier = Modifier,
    isNightMode: Boolean = false,
    isFirstPages: Boolean = false // Page 1 & 2 have grand decorative style
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f || isNightMode

    val magentaBorder = if (isDark) Color(0xFFE879F9) else Color(0xFFD81B60)
    val emeraldGreen = if (isDark) Color(0xFF4ADE80) else Color(0xFF1B8A38)
    val goldBorder = if (isDark) Color(0xFFFCD34D) else Color(0xFFC59B27)
    val pageBg = if (isDark) Color(0xFF161917) else Color(0xFFFCFAF4)

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val outPad = 4.dp.toPx()
        val midPad = 10.dp.toPx()
        val inPad = 16.dp.toPx()

        // 1. Outer Magenta / Pink Border
        drawRoundRect(
            color = magentaBorder,
            topLeft = Offset(outPad, outPad),
            size = Size(w - outPad * 2, h - outPad * 2),
            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
            style = Stroke(width = 3.5f)
        )

        // 2. Floral Scallops & Florets along outer border
        val scallopCountX = 14
        val scallopStepX = (w - outPad * 2) / scallopCountX
        for (i in 0..scallopCountX) {
            val x = outPad + i * scallopStepX
            // Top florets
            drawCircle(color = emeraldGreen, radius = 2.5f, center = Offset(x, outPad + 4f))
            drawCircle(color = goldBorder, radius = 1.5f, center = Offset(x, outPad + 4f))
            // Bottom florets
            drawCircle(color = emeraldGreen, radius = 2.5f, center = Offset(x, h - outPad - 4f))
            drawCircle(color = goldBorder, radius = 1.5f, center = Offset(x, h - outPad - 4f))
        }

        val scallopCountY = 26
        val scallopStepY = (h - outPad * 2) / scallopCountY
        for (i in 0..scallopCountY) {
            val y = outPad + i * scallopStepY
            // Left florets
            drawCircle(color = emeraldGreen, radius = 2.2f, center = Offset(outPad + 4f, y))
            // Right florets
            drawCircle(color = emeraldGreen, radius = 2.2f, center = Offset(w - outPad - 4f, y))
        }

        // 3. Middle Gold & Orange Ruled Line
        drawRoundRect(
            color = goldBorder,
            topLeft = Offset(midPad, midPad),
            size = Size(w - midPad * 2, h - midPad * 2),
            cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
            style = Stroke(width = 1.5f)
        )

        // 4. Inner Emerald Green Line
        drawRoundRect(
            color = emeraldGreen,
            topLeft = Offset(inPad, inPad),
            size = Size(w - inPad * 2, h - inPad * 2),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
            style = Stroke(width = 2.0f)
        )

        // Top Header Divider line (Below Surah name / Page / Juz)
        val headerY = inPad + 32.dp.toPx()
        drawLine(
            color = goldBorder,
            start = Offset(inPad, headerY),
            end = Offset(w - inPad, headerY),
            strokeWidth = 1.5f
        )

        // Bottom Footer Divider line (Above Manzil & Tajweed rules)
        val footerY = h - inPad - 20.dp.toPx()
        drawLine(
            color = goldBorder,
            start = Offset(inPad, footerY),
            end = Offset(w - inPad, footerY),
            strokeWidth = 1.5f
        )

        // 5. Corner Islamic Medallions (4 Corners)
        val corners = listOf(
            Offset(inPad, inPad),
            Offset(w - inPad, inPad),
            Offset(inPad, h - inPad),
            Offset(w - inPad, h - inPad)
        )

        corners.forEach { pt ->
            drawCircle(color = pageBg, radius = 10f, center = pt)
            drawCircle(color = magentaBorder, radius = 8f, center = pt, style = Stroke(width = 1.5f))
            drawCircle(color = goldBorder, radius = 5f, center = pt)
            drawCircle(color = emeraldGreen, radius = 2.5f, center = pt)
        }

        // Bottom Center Manzil Medallion
        val manzilCenterX = w / 2f
        val manzilCenterY = h - inPad
        drawCircle(color = pageBg, radius = 14f, center = Offset(manzilCenterX, manzilCenterY))
        drawCircle(color = magentaBorder, radius = 12f, center = Offset(manzilCenterX, manzilCenterY), style = Stroke(width = 1.5f))
        drawCircle(color = goldBorder, radius = 9f, center = Offset(manzilCenterX, manzilCenterY), style = Stroke(width = 1.2f))
    }
}

/**
 * Grand Floral Arch Header for Surah Al-Fatiha & Surah Al-Baqarah (Pages 1 & 2).
 * Replicates the magnificent green arabesque gate with dual cartouches for Surah name & Bismillah.
 */
@Composable
fun GrandTajweedSurahArch(
    surahNameArabic: String,
    surahType: String, // e.g. "مَكِّيَّةٌ وَّهِیَ سَبْعُ اٰیَاتٍ"
    showBismillah: Boolean = true,
    isDark: Boolean = false,
    scriptStyle: String = "INDO_PAK"
) {
    val emerald = if (isDark) Color(0xFF1B4D3E) else Color(0xFF1B8A38)
    val gold = if (isDark) Color(0xFFFCD34D) else Color(0xFFC59B27)
    val magenta = if (isDark) Color(0xFFE879F9) else Color(0xFFD81B60)
    val archBg = if (isDark) Color(0xFF0F261E) else Color(0xFFE8F5E9)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 12.dp, bottomEnd = 12.dp),
        color = archBg,
        border = androidx.compose.foundation.BorderStroke(2.dp, emerald)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Surah Name Cartouche (Pill box with gold/magenta borders)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) Color(0xFF1E293B) else Color(0xFFFCFAF4),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, magenta),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(vertical = 2.dp)
            ) {
                Text(
                    text = "سُوْرَةُ $surahNameArabic $surahType",
                    fontFamily = getQuranFontFamily(scriptStyle),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = magenta,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            if (showBismillah) {
                Spacer(modifier = Modifier.height(6.dp))

                // Bismillah Cartouche
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) Color(0xFF1E293B) else Color(0xFFFCFAF4),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, gold),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(vertical = 2.dp)
                ) {
                    Text(
                        text = "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِیْمِ",
                        fontFamily = getQuranFontFamily(scriptStyle),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = gold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

/**
 * Bottom Tajweed Rule Guide Banner (سبز حروف کو موٹا کریں • سرخ حروف پر غنہ کریں • نیلے حروف پر قلقلہ کریں)
 */
@Composable
fun TajweedRulesFooter(isDark: Boolean = false) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 2.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "سبز حروف: موٹا | سرخ حروف: غنہ و مد | نیلے حروف: قلقلہ",
                fontFamily = UrduFontFamily,
                fontSize = 11.sp,
                color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Front Cover Page of Mushaf (جدید رنگین تجویدی اندازِ تعلیم سے آراستہ - القرآن الکریم)
 */
@Composable
fun MushafCoverPageView(
    onOpenQuranClick: () -> Unit,
    isDark: Boolean = false
) {
    val pageBg = if (isDark) Color(0xFF161917) else Color(0xFFFCFAF4)
    val gold = if (isDark) Color(0xFFFCD34D) else Color(0xFFC59B27)
    val green = if (isDark) Color(0xFF4ADE80) else Color(0xFF1B8A38)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        IndoPakMushafFrame(isNightMode = isDark, isFirstPages = true)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Cover Inscription
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "جدید رنگین تجویدی اندازِ تعلیم سے آراستہ",
                    fontFamily = UrduFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = green,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "١٦ سطر تجویدی قرآن مجید (پاکستانی رسم الخط)",
                    fontFamily = UrduFontFamily,
                    fontSize = 13.sp,
                    color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
            }

            // Central Grand Medallion Art
            Surface(
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(32.dp)),
                color = pageBg,
                border = androidx.compose.foundation.BorderStroke(2.dp, gold),
                shadowElevation = 6.dp
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().background(Color(0xFF0F3A20))) {
                    Text(text = "القرآن الكريم", color = Color(0xFFD4AF37), fontSize = 32.sp)
                }
            }

            // Bottom Inscription & Open Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "مع متشابہات و ضروری قواعد اردو و انگلش",
                    fontFamily = UrduFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFFFCD34D) else Color(0xFFB45309),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "حضرت مولانا قاری رحیم بخش پانی پتیؒ",
                    fontFamily = UrduFontFamily,
                    fontSize = 13.sp,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                androidx.compose.material3.Button(
                    onClick = onOpenQuranClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = green
                    )
                ) {
                    Text(
                        text = "قرآن مجید شروع کریں (صفحہ ۱)",
                        fontFamily = UrduFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
