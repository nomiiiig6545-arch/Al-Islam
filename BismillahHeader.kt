package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IndoPakFontFamily
import com.example.ui.theme.UrduFontFamily
import com.example.ui.theme.getQuranFontFamily
import com.example.util.QuranSanitizer

/**
 * Dedicated standalone header for "Bismillah ir-Rahman ir-Rahim".
 * Displayed separately before Ayah 1 (for all Surahs except Surah 9 At-Tawbah).
 */
@Composable
fun BismillahHeader(
    modifier: Modifier = Modifier,
    showUrduTranslation: Boolean = true,
    backgroundColor: Color = Color(0xFFF7F5EE),
    borderColor: Color = Color(0xFFD4AF37),
    textColor: Color = Color(0xFF1B4D3E),
    scriptStyle: String = "INDO_PAK"
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.5f)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Decorative Top Calligraphy Ornaments
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, borderColor.copy(alpha = 0.6f))
                            )
                        )
                )
                Text(
                    text = " ۞ ",
                    color = borderColor,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(borderColor.copy(alpha = 0.6f), Color.Transparent)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Standalone Arabic Bismillah Calligraphy Text
            Text(
                text = QuranSanitizer.BISMILLAH_ARABIC,
                fontFamily = getQuranFontFamily(scriptStyle),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp,
                modifier = Modifier.fillMaxWidth()
            )

            if (showUrduTranslation) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = QuranSanitizer.BISMILLAH_URDU,
                    fontFamily = UrduFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF556059),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Bottom decorative ornament
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(1.dp)
                        .background(borderColor.copy(alpha = 0.4f))
                )
            }
        }
    }
}
