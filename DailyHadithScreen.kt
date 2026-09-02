package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArabicFontFamily
import java.util.Calendar

data class DailyHadithItem(
    val arabic: String,
    val urdu: String,
    val english: String,
    val reference: String
)

val dailyHadithList = listOf(
    DailyHadithItem(
        arabic = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى",
        urdu = "بیشک اعمال کا دارومدار نیتوں پر ہے، اور ہر شخص کے لیے وہی ہے جس کی اس نے نیت کی۔",
        english = "\"Actions are judged by motives (niyyah), so each man will have what he intended.\"",
        reference = "Sahih Bukhari 1"
    ),
    DailyHadithItem(
        arabic = "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ",
        urdu = "تم میں سے بہترین شخص وہ ہے جو قرآن سیکھے اور سکھائے۔",
        english = "\"The best among you are those who learn the Quran and teach it.\"",
        reference = "Sahih Bukhari 5027"
    ),
    DailyHadithItem(
        arabic = "لاَ يُؤْمِنُ أَحَدُكُمْ حَتَّى يُحِبَّ لأَخِيهِ مَا يُحِبُّ لِنَفْسِهِ",
        urdu = "تم میں سے کوئی شخص اس وقت تک کامل مومن نہیں ہو سکتا جب تک اپنے بھائی کے لیے بھی وہی پسند نہ کرے جو اپنے لیے پسند کرتا ہے۔",
        english = "\"None of you truly believes until he wishes for his brother what he wishes for himself.\"",
        reference = "Sahih Bukhari 13"
    ),
    DailyHadithItem(
        arabic = "الدِّينُ النَّصِيحَةُ",
        urdu = "دین خیرخواہی اور نیت کی سچائی کا نام ہے۔",
        english = "\"Religion is sincerity and good advice.\"",
        reference = "Sahih Muslim 55"
    ),
    DailyHadithItem(
        arabic = "الْمُسْلِمُ مَنْ سَلِمَ الْمُسْلِمُونَ مِنْ لِسَانِهِ وَيَدِهِ",
        urdu = "مسلمان وہ ہے جس کی زبان اور ہاتھ سے دوسرے مسلمان محفوظ رہیں۔",
        english = "\"A Muslim is the one from whose tongue and hand other Muslims are safe.\"",
        reference = "Sahih Bukhari 10"
    ),
    DailyHadithItem(
        arabic = "تَبَسُّمُكَ فِي وَجْهِ أَخِيكَ لَكَ صَدَقَةٌ",
        urdu = "اپنے بھائی کے سامنے تمہارا مسکرانا تمہارے لیے صدقہ ہے۔",
        english = "\"Your smiling in the face of your brother is charity for you.\"",
        reference = "Jami at-Tirmidhi 1956"
    ),
    DailyHadithItem(
        arabic = "مَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الآخِرِ فَلْيَقُلْ خَيْرًا أَوْ لِيَصْمُتْ",
        urdu = "جو شخص اللہ اور آخرت کے دن پر ایمان رکھتا ہو اسے چاہیے کہ اچھی بات کہے یا خاموش رہے۔",
        english = "\"He who believes in Allah and the Last Day should speak good or remain silent.\"",
        reference = "Sahih Bukhari 6018"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyHadithScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isFavorite by remember { mutableStateOf(false) }

    // Dynamic selection based on Day of Year
    val dayOfYear = remember { Calendar.getInstance().get(Calendar.DAY_OF_YEAR) }
    val currentHadith = remember(dayOfYear) {
        dailyHadithList[dayOfYear % dailyHadithList.size]
    }

    val darkBg = MaterialTheme.colorScheme.background
    val goldColor = MaterialTheme.colorScheme.secondary
    val deepGreenCard = MaterialTheme.colorScheme.surfaceVariant
    val deepGreenBottom = MaterialTheme.colorScheme.surface
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant

    fun copyHadithToClipboard() {
        val textToCopy = "${currentHadith.arabic}\n\n${currentHadith.urdu}\n\n${currentHadith.english}\n\nReference: ${currentHadith.reference}"
        clipboardManager.setText(AnnotatedString(textToCopy))
        Toast.makeText(context, "Hadith copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun shareHadith() {
        val textToShare = "${currentHadith.arabic}\n\n${currentHadith.urdu}\n\n${currentHadith.english}\n\nReference: ${currentHadith.reference}\n- Shared via Al-Quran Majeed"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, textToShare)
        }
        context.startActivity(Intent.createChooser(intent, "Share Hadith"))
    }

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
                        text = "Daily Hadith",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldColor
                    )

                    IconButton(onClick = { shareHadith() }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = goldColor
                        )
                    }
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Info Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "HADITH OF THE DAY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = goldColor,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "18 Rabi Al-Awwal, 1448 AH",
                    fontSize = 14.sp,
                    color = textMuted
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Hadith Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = deepGreenCard,
                border = BorderStroke(1.dp, goldColor.copy(alpha = 0.3f)),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Arabic Text
                        Text(
                            text = currentHadith.arabic,
                            fontSize = 26.sp,
                            fontFamily = ArabicFontFamily,
                            color = textPrimary,
                            textAlign = TextAlign.Center,
                            lineHeight = 44.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Gradient Divider
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(0.7f),
                            thickness = 1.dp,
                            color = goldColor.copy(alpha = 0.3f)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Urdu Translation
                        Text(
                            text = currentHadith.urdu,
                            fontSize = 18.sp,
                            fontFamily = ArabicFontFamily,
                            color = textMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 32.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // English Translation
                        Text(
                            text = currentHadith.english,
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Italic,
                            color = textMuted.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Reference Badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = goldColor.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, goldColor.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = currentHadith.reference,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = goldColor,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Bottom Action Bar inside Card
                    Surface(
                        color = deepGreenBottom,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Copy
                            Row(
                                modifier = Modifier
                                    .clickable { copyHadithToClipboard() }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = goldColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(text = "Copy", fontSize = 12.sp, color = textMuted)
                            }

                            // Favorite/Save
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        isFavorite = !isFavorite
                                        Toast.makeText(context, if (isFavorite) "Added to favorites" else "Removed from favorites", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = goldColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(text = if (isFavorite) "Saved" else "Save", fontSize = 12.sp, color = textMuted)
                            }

                            // Share
                            Row(
                                modifier = Modifier
                                    .clickable { shareHadith() }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = goldColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(text = "Share", fontSize = 12.sp, color = textMuted)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
