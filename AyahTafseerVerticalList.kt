package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AyahTafseerDetails
import com.example.data.TafseerTranslationEngine
import com.example.ui.theme.IndoPakFontFamily
import com.example.ui.theme.UrduFontFamily
import com.example.ui.theme.getQuranFontFamily
import com.example.util.QuranSanitizer

import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.material.icons.filled.AutoAwesome
import android.widget.Toast
import android.content.Intent

// Color Tokens for the Alternating Vertical Layout
private val EmeraldPrimary = Color(0xFF0F3E2E)
private val EmeraldDark = Color(0xFF0A2B20)
private val GoldAccent = Color(0xFFD4AF37)
private val WarmAmber = Color(0xFFC59B27)
private val SoftCream = Color(0xFFFAF7EE)
private val TranslationBg = Color(0xFFF3EFE0)
private val TafseerBg = Color(0xFFFFFFFF)
private val BorderMuted = Color(0xFFE2DCB8)
private val TextDark = Color(0xFF1E2621)
private val TextMuted = Color(0xFF5C6B61)

/**
 * Utility to format complete Arabic text, Urdu translation, and Tafseer Ibn Kathir
 * for seamless copying and sharing on WhatsApp, SMS, and other platforms.
 */
object AyahTafseerShareUtils {
    fun generateFullShareText(
        surahNumber: Int,
        surahNameArabic: String,
        ayahNumber: Int,
        totalAyahs: Int,
        ayahDetails: AyahTafseerDetails,
        tafseerName: String = "Tafseer Ibn Kaseer",
        tafseerUrduName: String = "تفسیر ابن کثیر",
        selectedLanguage: String = "URDU"
    ): String {
        val cleanArabic = QuranSanitizer.cleanAyahArabic(ayahDetails.arabicText, surahNumber, ayahNumber)
        val cleanUrdu = QuranSanitizer.cleanAyahUrdu(ayahDetails.urduTranslation, surahNumber, ayahNumber)
        val englishTrans = ayahDetails.englishTranslation.ifBlank {
            TafseerTranslationEngine.getEnglishTranslation(surahNumber, ayahNumber)
        }
        val hindiTrans = ayahDetails.hindiTranslation.ifBlank {
            TafseerTranslationEngine.getHindiTranslation(surahNumber, ayahNumber, cleanUrdu)
        }

        val tafseerContent = if (ayahDetails.tafseerParagraphs.isNotEmpty()) {
            ayahDetails.tafseerParagraphs.joinToString("\n\n")
        } else if (ayahDetails.tafseerText.isNotBlank()) {
            ayahDetails.tafseerText
        } else {
            when (selectedLanguage) {
                "ENGLISH" -> "Tafseer Commentary for Surah $surahNumber, Ayah $ayahNumber ($tafseerName)."
                "ARABIC" -> "تفسير الآية $ayahNumber من سورة $surahNameArabic ($tafseerUrduName)."
                "HINDI" -> "तफ़सीर व्याख्या सूरह $surahNameArabic, आयत $ayahNumber ($tafseerName)।"
                else -> "$tafseerUrduName: سورة $surahNameArabic کی آیت نمبر $ayahNumber میں اللہ تعالیٰ کی توحید، قدرتِ کاملہ اور احکامات کی جامع وضاحت کی گئی ہے۔"
            }
        }

        val sb = StringBuilder()
        sb.append("بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ\n\n")
        sb.append("📖 سورة $surahNameArabic | آیت نمبر $ayahNumber (من $totalAyahs)\n")
        sb.append("═══════════════════════\n\n")
        sb.append("📜 النَّصُّ الْقُرْآنِيُّ (عربی متن):\n")
        sb.append(cleanArabic).append(" ﴿").append(ayahNumber).append("﴾\n\n")

        when (selectedLanguage) {
            "HINDI" -> {
                sb.append("📝 हिन्दी अनुवाद (Hindi Translation):\n")
                sb.append(hindiTrans).append("\n\n")
                val hindiTitle = when {
                    tafseerName.contains("Usmani", ignoreCase = true) -> "तफ़सीर-ए-उस्मानी"
                    tafseerName.contains("Jalalayn", ignoreCase = true) -> "तफ़सीर अल-जलालेन"
                    tafseerName.contains("Mazhari", ignoreCase = true) -> "तफ़सीर-ए-मज़हरी"
                    tafseerName.contains("Jawahir", ignoreCase = true) -> "तफ़सीर जवाहरुल क़ुरआन"
                    else -> "तफ़सीर इब्ने कसीر"
                }
                sb.append("📚 $hindiTitle:\n")
                sb.append(tafseerContent).append("\n")
                if (ayahDetails.isAiGenerated) {
                    sb.append("\n[AI-generated translation]\n")
                }
            }
            "ENGLISH" -> {
                sb.append("📝 English Translation (Saheeh International):\n")
                sb.append(englishTrans).append("\n\n")
                sb.append("📚 $tafseerName:\n")
                sb.append(tafseerContent).append("\n")
                if (ayahDetails.isAiGenerated) {
                    sb.append("\n[AI-generated translation]\n")
                }
            }
            "ARABIC" -> {
                val arabicTafseerTitle = when {
                    tafseerName.contains("Usmani", ignoreCase = true) -> "التفسير العثماني"
                    tafseerName.contains("Jalalayn", ignoreCase = true) -> "تفسير الجلالين"
                    tafseerName.contains("Mazhari", ignoreCase = true) -> "التفسير المظهري"
                    tafseerName.contains("Jawahir", ignoreCase = true) -> "تفسير جواهر القرآن"
                    else -> "تفسير ابن كثير"
                }
                sb.append("📚 $arabicTafseerTitle:\n")
                sb.append(tafseerContent).append("\n")
                if (ayahDetails.isAiGenerated) {
                    sb.append("\n[ترجمة مولدة بالذكاء الاصطناعي - AI-generated translation]\n")
                }
            }
            else -> {
                sb.append("📝 اردو ترجمہ:\n")
                sb.append(cleanUrdu).append("\n\n")
                sb.append("📚 $tafseerUrduName ($tafseerName):\n")
                sb.append(tafseerContent).append("\n")
                if (ayahDetails.isAiGenerated) {
                    sb.append("\n[مصنوعی ذہانت سے ترجمہ شدہ - AI-generated translation]\n")
                }
            }
        }

        val resolvedWords = WordByWordColorizer.getWordsForAyah(
            arabicText = cleanArabic,
            language = selectedLanguage
        )

        if (resolvedWords.isNotEmpty() && selectedLanguage != "ARABIC") {
            when (selectedLanguage) {
                "HINDI" -> {
                    sb.append("\n🔍 Word-by-Word Translation (शब्दार्थ):\n")
                    val wordsList = resolvedWords.take(8).joinToString(" • ") { 
                        "${it.arabic}: ${it.hindi.ifBlank { it.urdu }}"
                    }
                    sb.append(wordsList).append("\n")
                }
                "ENGLISH" -> {
                    sb.append("\n🔍 Word-by-Word Translation:\n")
                    val wordsList = resolvedWords.take(8).joinToString(" • ") { 
                        "${it.arabic}: ${it.english.ifBlank { it.urdu }}"
                    }
                    sb.append(wordsList).append("\n")
                }
                else -> {
                    sb.append("\n🔍 لغوی و لفظی معانی:\n")
                    val wordsList = resolvedWords.take(8).joinToString(" • ") { "${it.arabic}: ${it.urdu}" }
                    sb.append(wordsList).append("\n")
                }
            }
        }

        sb.append("\n═══════════════════════\n")
        sb.append("تفسیر القرآن الکریم - $tafseerUrduName")

        return sb.toString()
    }
}

/**
 * A dedicated Jetpack Compose component that creates a vertical list layout
 * alternating between:
 * 1. Arabic Ayah text (with script rendering, ornamental numbering & audio/copy actions)
 * 2. Urdu Translation (with quote bar and Nastaliq typography)
 * 3. Tafseer Commentary Paragraph (with authentic commentary, scholar tag, and clear visual separation)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AyahTafseerVerticalItem(
    modifier: Modifier = Modifier,
    surahNumber: Int,
    surahNameArabic: String,
    ayahNumber: Int,
    totalAyahs: Int,
    ayahDetails: AyahTafseerDetails,
    tafseerName: String = "Tafseer Ibn Kaseer",
    tafseerUrduName: String = "تفسیر ابن کثیر",
    tafseerSubtitle: String = "مفصل و مستند تفسیری نکات",
    selectedLanguage: String = "URDU",
    fontSizeSp: Float = 16f,
    lineHeightMultiplier: Float = 1.0f,
    isDarkMode: Boolean = false,
    wordByWordEnabled: Boolean = false,
    scriptStyle: String = "INDO_PAK",
    tajweedEnabled: Boolean = wordByWordEnabled,
    isBookmarked: Boolean = false,
    onBookmarkToggle: (() -> Unit)? = null,
    onCopyAyah: (() -> Unit)? = null,
    onShareAyah: (() -> Unit)? = null,
    onPlayAudio: (() -> Unit)? = null,
    onAddNote: (() -> Unit)? = null
) {
    val isWordByWord = wordByWordEnabled || tajweedEnabled
    var isTafseerExpanded by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val cleanArabic = remember(ayahDetails.arabicText, surahNumber, ayahNumber) {
        QuranSanitizer.cleanAyahArabic(ayahDetails.arabicText, surahNumber, ayahNumber)
    }
    val cleanUrdu = remember(ayahDetails.urduTranslation, surahNumber, ayahNumber) {
        QuranSanitizer.cleanAyahUrdu(ayahDetails.urduTranslation, surahNumber, ayahNumber)
    }
    val englishTranslationText = remember(ayahDetails.englishTranslation, surahNumber, ayahNumber) {
        ayahDetails.englishTranslation.ifBlank {
            TafseerTranslationEngine.getEnglishTranslation(surahNumber, ayahNumber)
        }
    }

    // Dynamic word-by-word tokenized pairs with colors
    val wordsList = remember(cleanArabic, selectedLanguage) {
        WordByWordColorizer.getWordsForAyah(
            arabicText = cleanArabic,
            language = selectedLanguage
        )
    }

    val showWordByWord = isWordByWord && selectedLanguage != "ARABIC"

    // Dynamic Reading Theme colors for Content Card
    val cardContainerColor = if (isDarkMode) Color(0xFF1B1E1B) else SoftCream
    val cardBorder = if (isDarkMode) BorderStroke(1.dp, Color(0xFF2E3D35)) else BorderStroke(1.dp, BorderMuted)
    
    val arabicSectionColor = if (isDarkMode) Color(0xFF242925) else Color.White
    val arabicSectionBorder = if (isDarkMode) BorderStroke(1.dp, Color(0xFF2E3D35)) else BorderStroke(1.dp, BorderMuted)
    val arabicDefaultTextColor = if (isDarkMode) Color.White else EmeraldDark

    val translationSectionColor = if (isDarkMode) Color(0xFF1E2420) else TranslationBg
    val translationSectionBorder = if (isDarkMode) BorderStroke(1.dp, Color(0xFF2A362F)) else BorderStroke(1.dp, BorderMuted.copy(alpha = 0.8f))
    val urduTitleColor = if (isDarkMode) Color(0xFF4ADE80) else EmeraldPrimary
    val urduTextColor = if (isDarkMode) Color(0xFFE2E8F0) else TextDark

    val tafseerSectionColor = if (isDarkMode) Color(0xFF18201C) else TafseerBg
    val tafseerSectionBorder = if (isDarkMode) BorderStroke(1.dp, Color(0xFF2A362F)) else BorderStroke(1.dp, BorderMuted)
    val tafseerHeaderTitleColor = if (isDarkMode) Color(0xFF4ADE80) else EmeraldPrimary
    val tafseerSubtitleColor = if (isDarkMode) Color(0xFF9CA3AF) else TextMuted
    val tafseerTextColor = if (isDarkMode) Color(0xFFE2E8F0) else TextDark
    val tafseerDividerColor = if (isDarkMode) Color(0xFF2A362F) else BorderMuted.copy(alpha = 0.6f)

    val cleanArabicFontSize = fontSizeSp.sp
    val cleanArabicLineHeight = (fontSizeSp * 1.6f * lineHeightMultiplier).sp
    val cleanUrduFontSize = (fontSizeSp * 0.72f).coerceIn(14f, 32f).sp
    val cleanUrduLineHeight = (fontSizeSp * 0.62f * 1.45f * lineHeightMultiplier).sp
    val cleanTafseerFontSize = (fontSizeSp * 0.68f).coerceIn(14f, 30f).sp
    val cleanTafseerLineHeight = (fontSizeSp * 0.60f * 1.45f * lineHeightMultiplier).sp

    val inlineWordByWordText = remember(wordsList, selectedLanguage, isDarkMode, fontSizeSp) {
        buildAnnotatedString {
            wordsList.forEachIndexed { idx, wordItem ->
                val wordColor = WordByWordColorizer.getWordColor(idx, isDarkMode)
                val wordTranslation = when (selectedLanguage) {
                    "HINDI" -> wordItem.hindi.ifBlank { wordItem.urdu }
                    "ENGLISH" -> wordItem.english.ifBlank { wordItem.urdu }
                    else -> wordItem.urdu
                }

                // 1. Arabic word in bold & colored
                withStyle(
                    SpanStyle(
                        color = wordColor,
                        fontFamily = getQuranFontFamily(scriptStyle),
                        fontSize = cleanArabicFontSize,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(wordItem.arabic)
                }

                // 2. Colon and Translation in same color, compact size
                if (wordTranslation.isNotBlank()) {
                    val fontFam = when (selectedLanguage) {
                        "HINDI", "ENGLISH" -> FontFamily.Default
                        else -> UrduFontFamily
                    }
                    val transFontSize = when (selectedLanguage) {
                        "HINDI", "ENGLISH" -> (fontSizeSp * 0.54f).coerceIn(12f, 22f).sp
                        else -> (fontSizeSp * 0.58f).coerceIn(13f, 24f).sp
                    }

                    withStyle(
                        SpanStyle(
                            color = wordColor,
                            fontFamily = fontFam,
                            fontSize = transFontSize,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(":")
                    }
                    withStyle(
                        SpanStyle(
                            color = wordColor,
                            fontFamily = fontFam,
                            fontSize = transFontSize,
                            fontWeight = FontWeight.Medium
                        )
                    ) {
                        append(" $wordTranslation")
                    }
                }

                // 3. Spacing gap between word units
                if (idx < wordsList.size - 1) {
                    withStyle(
                        SpanStyle(
                            fontSize = cleanArabicFontSize
                        )
                    ) {
                        append("   ")
                    }
                }
            }
        }
    }

    val handleCopy: () -> Unit = onCopyAyah ?: {
        val fullText = AyahTafseerShareUtils.generateFullShareText(
            surahNumber = surahNumber,
            surahNameArabic = surahNameArabic,
            ayahNumber = ayahNumber,
            totalAyahs = totalAyahs,
            ayahDetails = ayahDetails,
            tafseerName = tafseerName,
            tafseerUrduName = tafseerUrduName,
            selectedLanguage = selectedLanguage
        )
        clipboardManager.setText(AnnotatedString(fullText))
        val copyToastText = when (selectedLanguage) {
            "HINDI" -> "आयत $ayahNumber का अरबी पाठ, अनुवाद और तफ़सीर कॉपी हो गई!"
            "ENGLISH" -> "Ayah $ayahNumber text, translation & $tafseerName copied!"
            "ARABIC" -> "تم نسخ نص الآية $ayahNumber وتفسير $tafseerUrduName!"
            else -> "آیت $ayahNumber کا عربی متن، ترجمہ اور $tafseerUrduName کاپی ہو گئی!"
        }
        Toast.makeText(context, copyToastText, Toast.LENGTH_SHORT).show()
    }

    val handleShare: () -> Unit = onShareAyah ?: {
        val fullText = AyahTafseerShareUtils.generateFullShareText(
            surahNumber = surahNumber,
            surahNameArabic = surahNameArabic,
            ayahNumber = ayahNumber,
            totalAyahs = totalAyahs,
            ayahDetails = ayahDetails,
            tafseerName = tafseerName,
            tafseerUrduName = tafseerUrduName,
            selectedLanguage = selectedLanguage
        )
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "سورة $surahNameArabic (آیت $ayahNumber)")
            putExtra(Intent.EXTRA_TEXT, fullText)
            type = "text/plain"
        }
        val shareChooserTitle = when (selectedLanguage) {
            "HINDI" -> "शेयर करें (Share Ayah, Hindi Translation & Tafseer)"
            "ENGLISH" -> "Share Ayah, Translation & Tafseer"
            "ARABIC" -> "مشاركة الآية والتفسير"
            else -> "شیئر کریں (Share Ayah, Translation & Tafseer)"
        }
        val shareIntent = Intent.createChooser(sendIntent, shareChooserTitle)
        context.startActivity(shareIntent)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 2.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = cardBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ==========================================
            // TOP BAR: AYAH NUMBER BADGE & ACTION BUTTONS
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ayah Header Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = EmeraldPrimary,
                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "۝",
                            color = GoldAccent,
                            fontSize = 13.sp
                        )
                        val badgeText = when (selectedLanguage) {
                            "ENGLISH" -> "Verse $ayahNumber ($ayahNumber of $totalAyahs)"
                            "ARABIC" -> "الآية ${toArabicDigits(ayahNumber)} (${toArabicDigits(ayahNumber)} من ${toArabicDigits(totalAyahs)})"
                            "HINDI" -> "आयत संख्या $ayahNumber (आयत $ayahNumber / $totalAyahs)"
                            else -> "آیت نمبر $ayahNumber (آیت $ayahNumber من $totalAyahs)"
                        }
                        val badgeFont = when (selectedLanguage) {
                            "ENGLISH", "HINDI" -> FontFamily.Default
                            "ARABIC" -> getQuranFontFamily(scriptStyle)
                            else -> UrduFontFamily
                        }
                        Text(
                            text = badgeText,
                            fontFamily = badgeFont,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                    }
                }

                // Action Icons (Copy & Share)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onPlayAudio != null) {
                        Surface(
                            onClick = onPlayAudio,
                            shape = CircleShape,
                            color = EmeraldPrimary.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.8f)),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play Ayah Audio",
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = handleCopy,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = handleShare,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // ==========================================
            // 1. ARABIC AYAH TEXT SECTION
            // ==========================================
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = arabicSectionColor,
                border = arabicSectionBorder,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Script Title Indicator
                    val scriptIndicator = when {
                        selectedLanguage == "ARABIC" -> "النَّصُّ الْقُرْآنِيُّ"
                        selectedLanguage == "HINDI" && showWordByWord -> "क़ुरआन करीम • शब्दार्थ (Word by Word)"
                        selectedLanguage == "HINDI" -> "अल-क़ुरआन करीम"
                        selectedLanguage == "ENGLISH" && showWordByWord -> "THE NOBLE QUR'AN • WORD BY WORD"
                        selectedLanguage == "ENGLISH" -> "THE NOBLE QUR'AN"
                        showWordByWord -> "الْقُرْآنُ الْكَرِيمُ • لَفْظِي تَرْجَمَہ"
                        else -> "النَّصُّ الْقُرْآنِيُّ"
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = scriptIndicator,
                            fontFamily = when (selectedLanguage) {
                                "HINDI", "ENGLISH" -> FontFamily.Default
                                else -> UrduFontFamily
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                        Text(
                            text = "سورة $surahNameArabic",
                            fontFamily = getQuranFontFamily(scriptStyle),
                            fontSize = 12.sp,
                            color = EmeraldPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        if (showWordByWord && wordsList.isNotEmpty()) {
                            Text(
                                text = inlineWordByWordText,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                lineHeight = (fontSizeSp * 1.8f * lineHeightMultiplier).sp
                            )
                        } else {
                            Text(
                                text = cleanArabic,
                                fontFamily = getQuranFontFamily(scriptStyle),
                                fontSize = cleanArabicFontSize,
                                fontWeight = FontWeight.Bold,
                                color = arabicDefaultTextColor,
                                textAlign = TextAlign.Center,
                                lineHeight = cleanArabicLineHeight,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // ==========================================
            // 2. TRANSLATION SECTION (Shown for all translations Urdu, English, Hindi)
            // ==========================================
            if (selectedLanguage != "ARABIC") {
                val isEnglish = selectedLanguage == "ENGLISH"
                val isHindi = selectedLanguage == "HINDI"
                val translationHeading = when {
                    isHindi -> "हिन्दी अनुवाद (Hindi Translation)"
                    isEnglish -> "English Translation (Saheeh International)"
                    else -> "اردو ترجمہ (Urdu Translation)"
                }
                val translationBody = when {
                    isHindi -> ayahDetails.hindiTranslation.ifBlank {
                        TafseerTranslationEngine.getHindiTranslation(surahNumber, ayahNumber, cleanUrdu)
                    }
                    isEnglish -> englishTranslationText
                    else -> cleanUrdu
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = translationSectionColor,
                    border = translationSectionBorder,
                    shadowElevation = 0.5.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Left Golden Quote Accent
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(60.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(GoldAccent, WarmAmber)
                                    )
                                )
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FormatQuote,
                                    contentDescription = null,
                                    tint = WarmAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = translationHeading,
                                    fontFamily = if (isEnglish || isHindi) FontFamily.Default else UrduFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = urduTitleColor
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            val layoutDir = if (isEnglish || isHindi) LayoutDirection.Ltr else LayoutDirection.Rtl
                            CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
                                Text(
                                    text = translationBody,
                                    fontFamily = if (isEnglish || isHindi) FontFamily.Default else UrduFontFamily,
                                    fontSize = if (isEnglish || isHindi) 14.sp else cleanUrduFontSize,
                                    lineHeight = if (isEnglish || isHindi) 22.sp else cleanUrduLineHeight,
                                    color = urduTextColor,
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 3. TAFSEER COMMENTARY PARAGRAPH SECTION
            // ==========================================
            val isEnglishLanguage = selectedLanguage == "ENGLISH"
            val isArabicLanguage = selectedLanguage == "ARABIC"
            val isHindiLanguage = selectedLanguage == "HINDI"

            val displayTafseerTitle = when (selectedLanguage) {
                "HINDI" -> when {
                    tafseerName.contains("Usmani", ignoreCase = true) -> "तफ़सीर-ए-उस्मानी (Tafseer Usmani)"
                    tafseerName.contains("Jalalayn", ignoreCase = true) -> "तफ़सीर अल-जलालेन (Tafseer Al-Jalalayn)"
                    tafseerName.contains("Mazhari", ignoreCase = true) -> "तफ़सीर-ए-मज़हरी (Tafseer Mazhari)"
                    tafseerName.contains("Jawahir", ignoreCase = true) -> "तफ़सीर जवाहरुल क़ुरआन (Tafseer Jawahir-ul-Quran)"
                    else -> "तफ़सीर इब्ने कसीर (Tafseer Ibn Kaseer)"
                }
                "ENGLISH" -> tafseerName
                "ARABIC" -> when {
                    tafseerName.contains("Usmani", ignoreCase = true) -> "التفسير العثماني"
                    tafseerName.contains("Jalalayn", ignoreCase = true) -> "تفسير الجلالين"
                    tafseerName.contains("Mazhari", ignoreCase = true) -> "التفسير المظهري"
                    tafseerName.contains("Jawahir", ignoreCase = true) -> "تفسير جواهر القرآن"
                    else -> "تفسير ابن كثير"
                }
                else -> "$tafseerUrduName ($tafseerName)"
            }

            val displayTafseerSubtitle = when (selectedLanguage) {
                "HINDI" -> "विस्तृत तफ़सीर व व्याख्या"
                "ENGLISH" -> "Detailed Commentary & Insights"
                "ARABIC" -> "شرح وتفسير الآيات الكريمة"
                else -> tafseerSubtitle
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = tafseerSectionColor,
                border = tafseerSectionBorder,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    // Tafseer Header with Collapse/Expand Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isTafseerExpanded = !isTafseerExpanded }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(EmeraldPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = displayTafseerTitle,
                                    fontFamily = if (isEnglishLanguage || isHindiLanguage) FontFamily.Default else UrduFontFamily,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tafseerHeaderTitleColor
                                )
                                Text(
                                    text = displayTafseerSubtitle,
                                    fontFamily = if (isEnglishLanguage || isHindiLanguage) FontFamily.Default else UrduFontFamily,
                                    fontSize = 11.sp,
                                    color = tafseerSubtitleColor
                                )
                            }
                        }

                        IconButton(
                            onClick = { isTafseerExpanded = !isTafseerExpanded },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isTafseerExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isTafseerExpanded) "Collapse" else "Expand",
                                tint = EmeraldPrimary
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = isTafseerExpanded,
                        enter = fadeIn() + expandVertically(animationSpec = spring()),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(
                                color = tafseerDividerColor,
                                thickness = 0.8.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Tafseer Paragraphs
                            val tafseerLayoutDir = if (isEnglishLanguage || isHindiLanguage) LayoutDirection.Ltr else LayoutDirection.Rtl
                            CompositionLocalProvider(LocalLayoutDirection provides tafseerLayoutDir) {
                                ayahDetails.tafseerParagraphs.forEachIndexed { idx, paragraph ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        // Subtle Gold Bullet Indicator
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 8.dp, end = 8.dp)
                                                .size(6.dp)
                                                .background(GoldAccent, CircleShape)
                                        )

                                        Text(
                                            text = paragraph,
                                            fontFamily = if (isEnglishLanguage || isHindiLanguage) FontFamily.Default else UrduFontFamily,
                                            fontSize = if (isEnglishLanguage || isHindiLanguage) 14.sp else cleanTafseerFontSize,
                                            lineHeight = if (isEnglishLanguage || isHindiLanguage) 22.sp else cleanTafseerLineHeight,
                                            color = tafseerTextColor,
                                            textAlign = TextAlign.Start,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    if (idx < ayahDetails.tafseerParagraphs.lastIndex) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }
                                }
                            }

                            // AI-generated translation subtle label (Only shown if isAiGenerated is true)
                            if (ayahDetails.isAiGenerated) {
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(
                                    color = tafseerDividerColor.copy(alpha = 0.5f),
                                    thickness = 0.5.dp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isEnglishLanguage || isHindiLanguage) Arrangement.Start else Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "AI-generated translation",
                                        fontSize = 11.sp,
                                        fontStyle = FontStyle.Italic,
                                        color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun toArabicDigits(number: Int): String {
    val latin = number.toString()
    val digits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val sb = StringBuilder()
    for (ch in latin) {
        if (ch in '0'..'9') {
            sb.append(digits[ch - '0'])
        } else {
            sb.append(ch)
        }
    }
    return sb.toString()
}
