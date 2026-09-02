package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.ArabicFontFamily
import com.example.ui.theme.HandmadeBrushesFontFamily
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingScreen(
    currentRoute: String? = "landing",
    onReadQuranClick: () -> Unit,
    onAudioQuranClick: () -> Unit,
    onTafseerClick: () -> Unit,
    onDailyQuranClick: () -> Unit = {},
    onDailyHadithClick: () -> Unit = {},
    onCalendarClick: () -> Unit = {},
    onPrayerTimingClick: () -> Unit = {},
    onTasbeehClick: () -> Unit = {},
    onQiblaClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }

    val dayOfYear = remember { Calendar.getInstance().get(Calendar.DAY_OF_YEAR) }

    val rotatingIslamicEvents = remember {
        listOf(
            "10 ربیع الاول 465ھ / 15 نومبر 1072ء کو سلطان الپ ارسلان سلجوقی نے وفات پائی اور اس کا بیٹا ملک شاہ تخت نشین ہوا۔",
            "17 رمضان 2ھ کو غزوہ بدر کا معرکہ پیش آیا جس میں مسلمانوں کو عظیم الشان فتح حاصل ہوئی۔",
            "8 رمضان 8ھ کو فتح مکہ کا عظیم الشان واقعہ پیش آیا اور کعبہ کو بتوں سے پاک کیا گیا۔",
            "10 محرم 61ھ کو نواسہ رسول حضرت امام حسین رضی اللہ عنہ کی شہادت کربلا کے میدان میں ہوئی۔",
            "12 ربیع الاول کو سرورِ کائنات حضور اکرم صلی اللہ علیہ وسلم کی ولادت باسعادت ہوئی۔",
            "27 رجب المرجب کو واقعہ معراج النبی صلی اللہ علیہ وسلم پیش آیا۔",
            "13 رجب کو حضرت علی بن ابی طالب رضی اللہ عنہ کی ولادت باسعادت ہوئی۔",
            "22 جمادی الثانی 13ھ کو امیر المؤمنین حضرت ابو بکر صدیق رضی اللہ عنہ کا وصال ہوا۔",
            "26 ذو الحجہ 23ھ کو امیر المؤمنین حضرت عمر فاروق رضی اللہ عنہ پر قاتلانہ حملہ ہوا اور شہادت ہوئی۔",
            "18 ذو الحجہ 35ھ کو امیر المؤمنین حضرت عثمان غنی رضی اللہ عنہ کی شہادت ہوئی۔",
            "21 رمضان 40ھ کو امیر المؤمنین حضرت علی المرتضیٰ رضی اللہ عنہ نے جامِ شہادت نوش فرمایا۔",
            "15 شعبان المعظم کو شبِ برات کی بابرکت رات اور تحویلِ قبلہ کا واقعہ پیش آیا۔",
            "1 شوال المکرم کو مسلمانوں کا عظیم دینی تہوار عید الفطر منایا جاتا ہے۔",
            "10 ذو الحجہ کو سنتِ ابراہیمی پر عمل کرتے ہوئے عید الاضحیٰ منائی جاتی ہے۔",
            "9 ذو الحجہ (یوم عرفہ) کو خطبہ حجۃ الوداع دیا گیا جس میں حقوق العباد اور انسانی مساوات کا منشور عطا کیا گیا۔",
            "5 شوال 3ھ کو غزوہ احد کا معرکہ پیش آیا جس میں صحابہ کرام نے بے مثال قربانیاں دیں۔",
            "شوال 5ھ کو غزوہ خندق (احزاب) کا واقعہ پیش آیا جس میں سلمان فارسی رضی اللہ عنہ کے مشورے پر خندق کھودی گیا۔",
            "ذی القعدہ 6ھ کو صلح حدیبیہ کا تاریخی معاہدہ طے پایا جسے قرآن نے 'فتح مبین' قرار دیا۔",
            "ربیع الاول 7ھ کو غزوہ خیبر کا معرکہ پیش آیا اور حضرت علی رضی اللہ عنہ کے ہاتھ پر فتح نصیب ہوئی۔",
            "جمادی الأولى 8ھ کو غزوہ موتہ پیش آیا جس میں حضرت خالد بن ولید رضی اللہ عنہ نے سیف اللہ کا لقب پایا۔",
            "رجب 9ھ کو غزوہ تبوک پیش آیا جو حضور اکرم صلی اللہ علیہ وسلم کا آخری غزوہ تھا۔",
            "10 محرم الحرام کو حضرت موسیٰ علیہ السلام اور بنی اسرائیل کو فرعون سے نجات ملی۔",
            "18 ذو الحجہ 10ھ کو غدیر خم کا تاریخی خطبہ دیا گیا۔",
            "28 صفر 50ھ کو نواسہ رسول حضرت امام حسن مجتبیٰ رضی اللہ عنہ کی شہادت ہوئی۔",
            "5 شعبان 4ھ کو حضرت امام حسین رضی اللہ عنہ کی ولادت باسعادت ہوئی۔",
            "15 رمضان 3ھ کو حضرت امام حسن رضی اللہ عنہ کی ولادت ہوئی۔",
            "25 شوال 148ھ کو حضرت امام جعفر صادق علیہ السلام کا وصال ہوا۔",
            "17 صفر 203ھ کو حضرت امام علی رضا علیہ السلام کا وصال ہوا۔",
            "4 ربیع الثانی 561ھ کو شیخ عبدالقادر جیلانی رحمۃ اللہ علیہ کا وصال ہوا۔",
            "20 رمضان 8ھ کو مکہ مکرمہ بغیر خون خرابے کے فتح ہوا اور عام معافی کا اعلان کیا گیا۔",
            "24 رجب 7ھ کو خیبر کا مشہور قلعہ قموص فتح ہوا اور اسلام کا پرچم لہرایا گیا۔",
            "15 رجب 2ھ کو تحویلِ قبلہ کا حکم نازل ہوا اور بیت المقدس کی جگہ کعبہ شریف قبلہ مقرر ہوا۔",
            "27 رمضان المبارک کی بابرکت رات میں قرآن کریم کا نزول مبارک شروع ہوا۔",
            "17 ربیع الاول کو اسلامی تاریخ کی عظیم الشان علمی و معاشی بیداری کا آغاز ہوا۔",
            "27 صفر المظفر کو حضور اکرم صلی اللہ علیہ وسلم نے مکہ مکرمہ سے مدینہ منورہ کی طرف ہجرت فرمائی۔",
            "10 رجب 195ھ کو امام محمد تقی علیہ السلام کی ولادت باسعادت ہوئی۔"
        )
    }

    val currentIslamicEvent = remember(dayOfYear) {
        rotatingIslamicEvents[dayOfYear % rotatingIslamicEvents.size]
    }

    fun shareIslamicEvent() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "$currentIslamicEvent\n\n- Shared via Al-Quran Majeed")
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Islamic Event"))
    }

    fun shareApp() {
        val shareText = "📖 القرآن مجید ایپ انسٹال کریں — تجوید کے ساتھ قرآن پڑھیں، اردو ترجمہ اور تفسیر کے ساتھ سنیں، نماز کے اوقات اور قبلہ کی سمت معلوم کریں، اور تسبیح شمار کریں — سب ایک ہی ایپ میں۔ آپ بھی انسٹال کریں اور اپنے پیاروں کے ساتھ شیئر کریں تاکہ یہ آپ کے لیے صدقہ جاریہ بن جائے۔\n\nhttps://ai.studio/build"
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Al-Quran Majeed"))
    }

    LaunchedEffect(currentRoute) {
        scrollState.scrollTo(0)
    }

    val handleNavClick: (() -> Unit) -> Unit = { action ->
        coroutineScope.launch {
            scrollState.scrollTo(0)
        }
        action()
    }

    val darkBg = MaterialTheme.colorScheme.background
    val goldColor = MaterialTheme.colorScheme.secondary
    val cardBg = MaterialTheme.colorScheme.surfaceVariant
    val cardBgHigh = MaterialTheme.colorScheme.surface
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant
    val emeraldContainer = MaterialTheme.colorScheme.primaryContainer

    Scaffold(
        containerColor = darkBg,
        topBar = {
            Surface(
                color = darkBg.copy(alpha = 0.95f),
                shadowElevation = 4.dp,
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
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = goldColor
                        )
                    }

                    Text(
                        text = "Al-Quran Majeed",
                        fontFamily = HandmadeBrushesFontFamily,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldColor
                    )

                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
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
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Date Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Calendar",
                        tint = goldColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "18 Rabi I, 1448",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldColor
                    )
                }
                Text(
                    text = "Tue 01-09-26",
                    fontSize = 14.sp,
                    color = textMuted
                )
            }

            // Prayer Time Tracker Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { handleNavClick(onPrayerTimingClick) },
                shape = RoundedCornerShape(16.dp),
                color = cardBgHigh,
                border = BorderStroke(1.dp, goldColor.copy(alpha = 0.2f)),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Mosque Silhouette Graphic
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(emeraldContainer)
                            .border(1.dp, goldColor.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mosque,
                            contentDescription = "Mosque",
                            tint = goldColor,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "NOW",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textMuted,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "SUNRISE",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = goldColor
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = goldColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "12:09 PM",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textMuted,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "DHUHR",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                        }
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Search word or ayat in Quran",
                        color = textMuted,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = textMuted
                    )
                },
                trailingIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Search",
                            tint = goldColor
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = cardBg,
                    unfocusedContainerColor = cardBg,
                    focusedBorderColor = goldColor,
                    unfocusedBorderColor = goldColor.copy(alpha = 0.3f),
                    focusedTextColor = textPrimary,
                    unfocusedTextColor = textPrimary
                ),
                singleLine = true
            )

            // Quick Actions Grid (5 Items)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = cardBg,
                border = BorderStroke(1.dp, goldColor.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuickActionButton(
                        title = "Quran",
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        onClick = { handleNavClick(onDailyQuranClick) },
                        goldColor = goldColor,
                        cardBgHigh = cardBgHigh
                    )
                    QuickActionButton(
                        title = "Hadith",
                        icon = Icons.Default.AutoStories,
                        onClick = { handleNavClick(onDailyHadithClick) },
                        goldColor = goldColor,
                        cardBgHigh = cardBgHigh
                    )
                    QuickActionButton(
                        title = "Prayer",
                        icon = Icons.Default.Schedule,
                        onClick = { handleNavClick(onPrayerTimingClick) },
                        goldColor = goldColor,
                        cardBgHigh = cardBgHigh
                    )
                    QuickActionButton(
                        title = "Calendar",
                        icon = Icons.Default.CalendarMonth,
                        onClick = { handleNavClick(onCalendarClick) },
                        goldColor = goldColor,
                        cardBgHigh = cardBgHigh
                    )
                    QuickActionButton(
                        title = "Qibla",
                        icon = Icons.Default.Explore,
                        onClick = { handleNavClick(onQiblaClick) },
                        goldColor = goldColor,
                        cardBgHigh = cardBgHigh
                    )
                }
            }

            // Main Feature Cards (Read Quran, Audio Quran, Tasbeeh)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { handleNavClick(onReadQuranClick) },
                shape = RoundedCornerShape(16.dp),
                color = emeraldContainer,
                border = BorderStroke(1.2.dp, goldColor),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(cardBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = goldColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Read Quran",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "16-line Tajweed Colour Coded Mushaf",
                                fontSize = 12.sp,
                                color = goldColor.copy(alpha = 0.9f)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = goldColor
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .clickable { handleNavClick(onAudioQuranClick) },
                    shape = RoundedCornerShape(16.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, goldColor.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headset,
                            contentDescription = null,
                            tint = goldColor,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Audio Quran",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .clickable { handleNavClick(onTafseerClick) },
                    shape = RoundedCornerShape(16.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, goldColor.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.LibraryBooks,
                            contentDescription = null,
                            tint = goldColor,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tafseer Quran",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }
                }
            }

            // Islamic Event Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = cardBg,
                border = BorderStroke(1.dp, goldColor.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Islamic Event",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Text(
                                text = "Historical Highlight",
                                fontSize = 12.sp,
                                color = goldColor
                            )
                        }
                        IconButton(onClick = { shareIslamicEvent() }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Event",
                                tint = goldColor
                            )
                        }
                    }

                    Text(
                        text = currentIslamicEvent,
                        fontSize = 16.sp,
                        fontFamily = ArabicFontFamily,
                        color = textPrimary,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth(),
                        lineHeight = 24.sp
                    )
                }
            }

            // Share This App Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clickable { shareApp() },
                shape = RoundedCornerShape(16.dp),
                color = cardBgHigh,
                border = BorderStroke(1.dp, goldColor.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentHeight(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Share This App",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = goldColor,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "اس ایپ کو کم از کم 5 لوگوں کے ساتھ شیئر کریں — ہو سکتا ہے کوئی ایک شخص اس سے ہدایت پائے اور یہ آپ کے لیے صدقہ جاریہ بن جائے۔",
                            fontSize = 15.sp,
                            fontFamily = ArabicFontFamily,
                            color = textMuted,
                            modifier = Modifier.fillMaxWidth(),
                            lineHeight = 26.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(emeraldContainer)
                            .clickable { shareApp() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share App",
                            tint = goldColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    goldColor: Color,
    cardBgHigh: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(cardBgHigh)
                .border(1.dp, goldColor.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = goldColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
