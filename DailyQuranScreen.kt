package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

data class QuranDailyVerse(
    val arabic: String,
    val urdu: String,
    val english: String,
    val reference: String
)

val dailyVersesList = listOf(
    QuranDailyVerse(
        arabic = "ٱللَّهُ لَآ إِلَـٰهَ إِلَّا هُوَ ٱلْحَىُّ ٱلْقَيُّومُ ۚ لَا تَأْخُذُهُۥ سِنَةٌۭ وَلَا نَوْمٌۭ ۚ لَّهُۥ مَا فِى ٱلسَّمَـٰوَٰتِ وَمَا فِى ٱلْأَرْضِ ۗ",
        urdu = "اللہ، جس کے سوا کوئی معبود نہیں، وہ زندہ ہے، سب کا تھامنے والا ہے۔ اسے نہ اونگھ آتی ہے نہ نیند۔ اسی کا ہے جو کچھ آسمانوں اور زمین میں ہے۔",
        english = "\"Allah - there is no deity except Him, the Ever-Living, the Sustainer of [all] existence.\"",
        reference = "Surat Al-Baqarah [2:255]"
    ),
    QuranDailyVerse(
        arabic = "إِنَّ مَعَ ٱلْعُسْرِ يُسْرًۭا",
        urdu = "بے شک مشکل کے ساتھ آسانی ہے۔",
        english = "\"Indeed, with hardship will come ease.\"",
        reference = "Surat Ash-Sharh [94:6]"
    ),
    QuranDailyVerse(
        arabic = "وَقُل رَّبِّ زِدْنِي عِلْمًا",
        urdu = "اور دعا کرو: اے میرے رب! میرے علم میں اضافہ فرما۔",
        english = "\"And say: My Lord, increase me in knowledge.\"",
        reference = "Surat Ta-Ha [20:114]"
    ),
    QuranDailyVerse(
        arabic = "فَٱذْكُرُونِيٓ أَذْكُرْكُمْ وَٱشْكُرُوا۟ لِي وَلَا تَكْفُرُونِ",
        urdu = "پس تم مجھے یاد رکھو، میں تمہیں یاد رکھوں گا اور میرا شکر ادا کرو اور میری ناشکری نہ کرو۔",
        english = "\"So remember Me; I will remember you. And be grateful to Me and do not deny Me.\"",
        reference = "Surat Al-Baqarah [2:152]"
    ),
    QuranDailyVerse(
        arabic = "وَإِذَا سَأَلَكَ عِبَادِي عَنِّي فَإِنِّي قَرِيبٌ ۖ أُجِيبُ دَعْوَةَ الدَّاعِ إِذَا دَعَانِ",
        urdu = "اور جب میرے بندے آپ سے میرے بارے میں پوچھیں تو (کہہ دیجیے) میں قریب ہی ہوں۔ جب پکارنے والا مجھے پکارتا ہے تو میں اس کی دعا قبول کرتا ہوں۔",
        english = "\"And when My servants ask you concerning Me, indeed I am near. I respond to the invocation of the supplicant when he calls upon Me.\"",
        reference = "Surat Al-Baqarah [2:186]"
    ),
    QuranDailyVerse(
        arabic = "رَبَّنَآ ءَاتِنَا فِى ٱلدُّنْيَا حَسَنَةًۭ وَفِى ٱلْـَٔاخِرَةِ حَسَنَةًۭ وَقِنَا عَذَابَ ٱلنَّارِ",
        urdu = "اے ہمارے رب! ہمیں دنیا میں بھی بھلائی دے اور آخرت میں بھی بھلائی دے اور ہمیں آگ کے عذاب سے بچا۔",
        english = "\"Our Lord, give us in this world [that which is] good and in the Hereafter [that which is] good and protect us from the punishment of the Fire.\"",
        reference = "Surat Al-Baqarah [2:201]"
    ),
    QuranDailyVerse(
        arabic = "ٱلَّذِينَ ءَامَنُوا۟ وَتَطْمَئِنُّ قُلُوبُهُم بِذِكْرِ ٱللَّهِ ۗ أَلَا بِذِكْرِ ٱللَّهِ تَطْمَئِنُّ ٱلْقُلُوبُ",
        urdu = "وہ لوگ جو ایمان لائے اور ان کے دل اللہ کے ذکر سے مطمئن ہوتے ہیں۔ سن لو! اللہ کے ذکر ہی سے دلوں کو اطمینان حاصل ہوتا ہے۔",
        english = "\"Those who have believed and whose hearts are assured by the remembrance of Allah. Unquestionably, by the remembrance of Allah hearts are assured.\"",
        reference = "Surat Ar-Ra'd [13:28]"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyQuranScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isSaved by remember { mutableStateOf(false) }

    // Dynamic verse calculation based on Day of Year
    val dayOfYear = remember { Calendar.getInstance().get(Calendar.DAY_OF_YEAR) }
    val currentVerse = remember(dayOfYear) {
        dailyVersesList[dayOfYear % dailyVersesList.size]
    }

    val darkBg = MaterialTheme.colorScheme.background
    val goldColor = MaterialTheme.colorScheme.secondary
    val goldContainer = MaterialTheme.colorScheme.primary
    val cardBg = MaterialTheme.colorScheme.surfaceVariant
    val darkCardBg = MaterialTheme.colorScheme.surface
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant

    fun copyVerseToClipboard() {
        val textToCopy = "${currentVerse.arabic}\n\n${currentVerse.urdu}\n\n${currentVerse.english}\n\nReference: ${currentVerse.reference}"
        clipboardManager.setText(AnnotatedString(textToCopy))
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun shareVerse() {
        val textToShare = "${currentVerse.arabic}\n\n${currentVerse.urdu}\n\n${currentVerse.english}\n\nReference: ${currentVerse.reference}\n- Shared via Al-Quran Majeed"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, textToShare)
        }
        context.startActivity(Intent.createChooser(intent, "Share Verse"))
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
                        text = "Daily Quran",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldColor
                    )

                    IconButton(onClick = { shareVerse() }) {
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
            Spacer(modifier = Modifier.height(20.dp))

            // Central Focus Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = cardBg,
                border = BorderStroke(1.dp, goldColor.copy(alpha = 0.3f)),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = goldContainer.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, goldColor.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = goldColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "AYAT OF THE MOMENT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = goldColor,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Arabic Text
                    Text(
                        text = currentVerse.arabic,
                        fontSize = 28.sp,
                        fontFamily = ArabicFontFamily,
                        color = textPrimary,
                        textAlign = TextAlign.Center,
                        lineHeight = 48.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Calligraphy Divider
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(0.7f),
                        thickness = 1.dp,
                        color = goldColor.copy(alpha = 0.3f)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Urdu Translation
                    Text(
                        text = currentVerse.urdu,
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
                        text = currentVerse.english,
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        color = textMuted.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Reference
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(modifier = Modifier.width(30.dp), color = goldColor.copy(alpha = 0.3f))
                        Text(
                            text = currentVerse.reference,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = goldColor
                        )
                        HorizontalDivider(modifier = Modifier.width(30.dp), color = goldColor.copy(alpha = 0.3f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons Row (Copy, Save, Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Copy
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { copyVerseToClipboard() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(cardBg)
                            .border(1.dp, goldColor.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = goldColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Copy", fontSize = 12.sp, color = textMuted)
                }

                // Save
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        isSaved = !isSaved
                        Toast.makeText(context, if (isSaved) "Verse saved to bookmarks" else "Removed from bookmarks", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(if (isSaved) goldColor else goldContainer)
                            .border(1.dp, goldColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Save",
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = if (isSaved) "Saved" else "Save", fontSize = 12.sp, color = goldColor, fontWeight = FontWeight.Bold)
                }

                // Share
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { shareVerse() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(cardBg)
                            .border(1.dp, goldColor.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.IosShare,
                            contentDescription = "Share",
                            tint = goldColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Share", fontSize = 12.sp, color = textMuted)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
