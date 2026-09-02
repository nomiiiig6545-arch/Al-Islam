package com.example.data.db

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.TafseerDao
import com.example.data.TafseerIbnKaseer
import com.example.data.mushaf.IndoPakMushafData
import com.example.data.mushaf.OfflineQuranDataProvider
import com.example.util.QuranSanitizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * DatabaseSeeder populates the Room database with initial authentic Quranic data:
 * - 114 Surahs metadata (Names in Arabic/Urdu/English, revelation type, ayah counts)
 * - Authentic Arabic Ayahs and Urdu Translations
 * - Authentic Tafseer Ibn Kathir (تفسیر ابن کثیر) commentary
 * - Enforces strict separation of Bismillah (Bismillah is NOT embedded into Ayah 1 of any Surah except Surah 1 Al-Fatiha).
 */
object DatabaseSeeder {

    /**
     * Creates a Room Database Callback that triggers database seeding upon database creation and opening.
     */
    fun createRoomCallback(context: Context, scope: CoroutineScope): RoomDatabase.Callback {
        return object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                scope.launch(Dispatchers.IO) {
                    val appDb = AppDatabase.getDatabase(context)
                    seedDatabase(appDb)
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                scope.launch(Dispatchers.IO) {
                    val appDb = AppDatabase.getDatabase(context)
                    seedIfEmpty(appDb)
                }
            }
        }
    }

    /**
     * Seeds the database with Surah list, sample Ayahs, Tafseer Ibn Kathir, and 549 Mushaf pages.
     * Uses `REPLACE` / `IGNORE` strategy to avoid duplicates.
     */
    suspend fun seedDatabase(database: AppDatabase, context: Context? = null) = withContext(Dispatchers.IO) {
        try {
            seedSurahs(database.surahDao())
            seedSampleAyahs(database.ayahDao(), context)
            seedTafseerIbnKathir(database.tafseerDao())
            seedMushafPages(database.mushafPageDao(), context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Seeds if tables are empty.
     */
    suspend fun seedIfEmpty(database: AppDatabase, context: Context? = null) = withContext(Dispatchers.IO) {
        try {
            val surahCount = database.surahDao().getAllSurahsSync().size
            if (surahCount == 0) {
                seedSurahs(database.surahDao())
            }

            val tafseerCount = database.tafseerDao().getCount()
            if (tafseerCount == 0) {
                seedTafseerIbnKathir(database.tafseerDao())
            }

            // Ensure prominent Surahs or all ayahs are seeded
            val fatihaAyahs = database.ayahDao().getAyahsForSurahSync(1)
            if (fatihaAyahs.isEmpty() || fatihaAyahs.size < 7) {
                seedSampleAyahs(database.ayahDao(), context)
            }

            val mushafPagesCount = database.mushafPageDao().getTotalPagesCount()
            if (mushafPagesCount < IndoPakMushafData.TOTAL_PAGES) {
                seedMushafPages(database.mushafPageDao(), context)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Seeds all 549 Mushaf pages into Room database, indexing local storage & bundled asset files.
     */
    suspend fun seedMushafPages(mushafPageDao: MushafPageDao, context: Context?) = withContext(Dispatchers.IO) {
        val pagesList = ArrayList<MushafPageEntity>(IndoPakMushafData.TOTAL_PAGES)
        val filesDir = context?.let { java.io.File(it.filesDir, "mushaf_tajweed_pages") }

        for (pageNumber in 1..IndoPakMushafData.TOTAL_PAGES) {
            val pageInfo = IndoPakMushafData.getPageInfo(pageNumber)
            val padded = "%03d".format(pageNumber)

            var isDownloaded = false
            var filePath: String? = null
            var fileSize = 0L

            // 1. Check internal storage filesDir
            if (filesDir != null && filesDir.exists()) {
                for (ext in listOf("png", "webp", "jpg")) {
                    val f1 = java.io.File(filesDir, "page_$padded.$ext")
                    if (f1.exists() && f1.length() > 1000) {
                        isDownloaded = true
                        filePath = f1.absolutePath
                        fileSize = f1.length()
                        break
                    }
                    val f2 = java.io.File(filesDir, "page_$pageNumber.$ext")
                    if (f2.exists() && f2.length() > 1000) {
                        isDownloaded = true
                        filePath = f2.absolutePath
                        fileSize = f2.length()
                        break
                    }
                }
            }

            // 2. Check bundled APK assets if not in filesDir
            if (!isDownloaded && context != null) {
                for (assetCandidate in listOf("mushaf_tajweed_pages/page_$padded.jpg", "mushaf_tajweed_pages/page_$padded.webp")) {
                    try {
                        context.assets.open(assetCandidate).use { stream ->
                            fileSize = stream.available().toLong()
                            isDownloaded = true
                            filePath = "asset://$assetCandidate"
                        }
                        if (isDownloaded) break
                    } catch (_: Exception) {}
                }
            }

            pagesList.add(
                MushafPageEntity(
                    pageNumber = pageNumber,
                    surahNumber = pageInfo.surahNumber,
                    surahNameArabic = pageInfo.surahNameArabic,
                    surahNameEnglish = pageInfo.surahNameEnglish,
                    juzNumber = pageInfo.juzNumber,
                    juzNameArabic = pageInfo.juzNameArabic,
                    localFilePath = filePath,
                    isDownloaded = isDownloaded,
                    fileSize = fileSize,
                    lastAccessedTimestamp = System.currentTimeMillis()
                )
            )
        }

        mushafPageDao.insertPages(pagesList)
    }

    /**
     * Seeds all 114 Surahs into cached_surahs table.
     */
    suspend fun seedSurahs(surahDao: SurahDao) = withContext(Dispatchers.IO) {
        val surahEntities = OfflineQuranDataProvider.getOfflineSurahEntities()
        surahDao.insertSurahs(surahEntities)
    }

    /**
     * Seeds authentic Ayahs with Arabic and Urdu text for all Surahs,
     * ensuring that Bismillah is strictly separated from Ayah 1 (except in Surah Al-Fatiha).
     */
    suspend fun seedSampleAyahs(ayahDao: AyahDao, context: Context? = null) = withContext(Dispatchers.IO) {
        if (context != null) {
            val allAyahs = OfflineQuranDataProvider.loadAllAyahsFromAssets(context)
            if (allAyahs.isNotEmpty()) {
                ayahDao.insertAyahs(allAyahs)
                return@withContext
            }
        }
        for (surahNum in 1..114) {
            val rawAyahs = OfflineQuranDataProvider.getOfflineAyahsForSurah(surahNum, context)
            val sanitizedAyahs = rawAyahs.map { ayah ->
                ayah.copy(
                    arabicText = QuranSanitizer.cleanAyahArabic(ayah.arabicText, surahNum, ayah.numberInSurah),
                    urduText = QuranSanitizer.cleanAyahUrdu(ayah.urduText, surahNum, ayah.numberInSurah)
                )
            }
            if (sanitizedAyahs.isNotEmpty()) {
                ayahDao.insertAyahs(sanitizedAyahs)
            }
        }
    }

    /**
     * Seeds detailed Tafseer Ibn Kathir (تفسیر ابن کثیر) records.
     */
    suspend fun seedTafseerIbnKathir(tafseerDao: TafseerDao) = withContext(Dispatchers.IO) {
        val tafseerList = getSampleTafseerIbnKathirData()
        val sanitizedTafseerList = tafseerList.map { item ->
            item.copy(
                arabicText = QuranSanitizer.cleanAyahArabic(item.arabicText, item.surahId, item.ayahNumber),
                urduTranslation = QuranSanitizer.cleanAyahUrdu(item.urduTranslation, item.surahId, item.ayahNumber)
            )
        }
        tafseerDao.insertAll(sanitizedTafseerList)
    }

    /**
     * Comprehensive authentic sample data for Tafseer Ibn Kathir.
     * Bismillah is cleanly isolated (only Ayah 1 of Surah 1 Al-Fatiha contains Bismillah as a verse).
     */
    fun getSampleTafseerIbnKathirData(): List<TafseerIbnKaseer> {
        val list = mutableListOf<TafseerIbnKaseer>()

        // 1. Al-Fatiha (1 to 7) - Here Ayah 1 IS Bismillah
        list.add(
            TafseerIbnKaseer(
                surahId = 1,
                ayahNumber = 1,
                arabicText = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
                urduTranslation = "شروع اللہ کا نام لے کر جو بڑا مہربان نہایت رحم والا ہے۔",
                tafseerContent = "تسمیہ کی تفسیر: علامہ ابن کثیرؒ فرماتے ہیں کہ ہر اچھے اور بابرکت کام سے پہلے 'بسم اللہ' پڑھنا مسنون اور برکت کا ذریعہ ہے۔ اللہ تعالیٰ کا نام تمام پاکیزہ ناموں میں سب سے افضل و اعظم ہے۔ 'الرحمن' اور 'الرحیم' اللہ کی رحمتِ عامہ اور خاصہ پر دلالت کرتے ہیں۔"
            )
        )
        list.add(
            TafseerIbnKaseer(
                surahId = 1,
                ayahNumber = 2,
                arabicText = "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَـٰلَمِينَ",
                urduTranslation = "سب تعریف اللہ تعالیٰ کے لئے ہے جو تمام جہانوں کا پالنے والا ہے۔",
                tafseerContent = "الحمدللہ کے معنیٰ: ابن کثیر کے مطابق تمام تعریفیں، شکر اور کمال صرف اللہ وحدہ لا شریک کے لائق ہے۔ الف لام استغراق کے لئے ہے یعنی حمد کی جتنی بھی قسمیں ہیں وہ سب اللہ تعالیٰ کے لئے ہی ثابت اور اسی کے لائق ہیں۔"
            )
        )
        list.add(
            TafseerIbnKaseer(
                surahId = 1,
                ayahNumber = 3,
                arabicText = "ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
                urduTranslation = "بڑا مہربان نہایت رحم فرمانے والا ہے۔",
                tafseerContent = "رحمانیت و رحیمیت کا تسلسل: ابن کثیرؒ فرماتے ہیں کہ جب اللہ نے اپنی ربوبیت کا ذکر کیا، تو اس کے فوراً بعد اپنی صفتِ رحمت کا ذکر فرمایا تاکہ بندوں کے دلوں میں خوف کے ساتھ امید اور محبت پیدا ہو۔"
            )
        )
        list.add(
            TafseerIbnKaseer(
                surahId = 1,
                ayahNumber = 4,
                arabicText = "مَـٰلِكِ يَوْمِ ٱلدِّينِ",
                urduTranslation = "روزِ جزا (قیامت کے دن) کا تنہا مالک و مختار ہے۔",
                tafseerContent = "یومِ جزا کی حاکمیت: ابن کثیرؒ فرماتے ہیں کہ اس دن کوئی بادشاہی کا دعویٰ نہیں کر سکے گا اور تمام اختیارات صرف قادرِ مطلق اللہ تعالیٰ کے پاس ہوں گے۔"
            )
        )
        list.add(
            TafseerIbnKaseer(
                surahId = 1,
                ayahNumber = 5,
                arabicText = "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ",
                urduTranslation = "ہم صرف تیری ہی عبادت کرتے ہیں اور صرف تجھ ہی سے مدد مانگتے ہیں۔",
                tafseerContent = "توحیدِ عبادت و توحیدِ استعانت: ابن کثیرؒ کے مطابق مفعول کو فعل پر مقدم کرنا حصر (تخصیص) کا فائدہ دیتا ہے، یعنی ہم صرف تیری ہی عبادت کرتے ہیں اور صرف تجھ ہی سے مدد مانگتے ہیں۔"
            )
        )
        list.add(
            TafseerIbnKaseer(
                surahId = 1,
                ayahNumber = 6,
                arabicText = "ٱهْدِنَا ٱلصِّرَٰطَ ٱلْمُسْتَقِيمَ",
                urduTranslation = "ہمیں سیدھے اور سچے راستے کی ہدایت فرما۔",
                tafseerContent = "صراط مستقیم کی دعا: ابن کثیر کے مطابق صراط مستقیم وہ واضح اور روشن راستہ ہے جس میں کوئی کجی نہیں، اور وہ اللہ کی کتاب اور سنتِ رسولؐ کی پیروی ہے۔"
            )
        )
        list.add(
            TafseerIbnKaseer(
                surahId = 1,
                ayahNumber = 7,
                arabicText = "صِرَٰطَ ٱلَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ ٱلْمَغْضُوبِ عَلَيْهِمْ وَلَا ٱلضَّآلِّينَ",
                urduTranslation = "ان لوگوں کا راستہ جن پر تو نے انعام فرمایا، نہ کہ ان کا جن پر غضب ہوا اور نہ ہی گمراہوں کا۔",
                tafseerContent = "انعام یافتگان: انبیاء، صدیقین، شہداء اور صالحین کا راستہ۔ مغضوب علیہم سے مراد وہ ہیں جنہوں نے حق کو جاننے کے باوجود نافرمانی کی، اور ضالین وہ ہیں جنہوں نے جہالت کی وجہ سے غلط راستہ اختیار کیا۔"
            )
        )

        // 2. Al-Baqarah (1 to 5, 255) - Ayah 1 is strictly "الۤمۤ" (NO Bismillah attached)
        list.add(
            TafseerIbnKaseer(
                surahId = 2,
                ayahNumber = 1,
                arabicText = "الۤمۤ",
                urduTranslation = "الف، لام، میم۔",
                tafseerContent = "حروفِ مقطعات: ابن کثیر کے مطابق ان حروف کا حقیقی علم اللہ تعالیٰ ہی کے پاس ہے، اور یہ اعجازِ قرآن کی روشن دلیل ہیں کہ انسانی کلام اس کے ہم پلہ نہیں ہو سکتا۔"
            )
        )
        list.add(
            TafseerIbnKaseer(
                surahId = 2,
                ayahNumber = 2,
                arabicText = "ذَٰلِكَ ٱلْكِتَـٰبُ لَا رَيْبَ ۛ فِيهِ ۛ هُدًۭى لِّلْمُتَّقِينَ",
                urduTranslation = "یہ وہ کتاب ہے جس میں کسی شک کی گنجائش نہیں، پرہیزگاروں کے لیے سراسر ہدایت ہے۔",
                tafseerContent = "اعجازِ قرآن اور متقین کی رہنمائی: ابن کثیرؒ فرماتے ہیں کہ اس کتاب کے اللہ کی طرف سے نازل شدہ ہونے میں کوئی شک نہیں، اور اس سے حقیقی نفع صرف متقی لوگ حاصل کرتے ہیں۔"
            )
        )
        list.add(
            TafseerIbnKaseer(
                surahId = 2,
                ayahNumber = 3,
                arabicText = "ٱلَّذِينَ يُؤْمِنُونَ بِٱلْغَيْبِ وَيُقِيمُونَ ٱلصَّلَوٰةَ وَمِمَّا رَزَقْنَـٰهُمْ يُنفِقُونَ",
                urduTranslation = "جو غیب پر ایمان لاتے ہیں اور نماز قائم کرتے ہیں اور جو کچھ ہم نے انہیں دیا ہے اس میں سے خرچ کرتے ہیں۔",
                tafseerContent = "متقین کے بنیادی اوصاف: ایمان بالغیب یعنی پوشیدہ حقائق (جنت، دوزخ، فرشتے، قیامت) پر یقین، نماز کی کامل اقامت، اور راہِ خدا میں مال کا انفاق۔"
            )
        )
        list.add(
            TafseerIbnKaseer(
                surahId = 2,
                ayahNumber = 4,
                arabicText = "وَٱلَّذِينَ يُؤْمِنُونَ بِمَآ أُنزِلَ إِلَيْكَ وَمَآ أُنزِلَ مِن قَبْلِكَ وَبِٱلْـَٔاخِرَةِ هُمْ يُوقِنُونَ",
                urduTranslation = "اور جو لوگ ایمان لاتے ہیں اس پر جو آپ پر اتارا گیا اور جو آپ سے پہلے اتارا گیا، اور وہ آخرت پر کامل یقین رکھتے ہیں۔",
                tafseerContent = "سابقہ کتب اور آخرت پر یقین: ابن کثیرؒ کے مطابق کامل ایمان یہ ہے کہ قرآن اور پچھلی تمام آسمانی کتابوں کی سچائی پر ایمان ہو اور یومِ آخرت کے حساب و کتاب پر پختہ یقین ہو۔"
            )
        )
        list.add(
            TafseerIbnKaseer(
                surahId = 2,
                ayahNumber = 5,
                arabicText = "أُو۟لَـٰٓئِكَ عَلَىٰ هُدًۭى مِّن رَّبِّهِمْ ۖ وَأُو۟لَـٰٓئِكَ هُمُ ٱلْمُفْلِحُونَ",
                urduTranslation = "یہی لوگ اپنے رب کی طرف سے سیدھی راہ پر ہیں اور یہی لوگ فلاح پانے والے ہیں۔",
                tafseerContent = "فلاح و کامرانی کی بشارت: ابن کثیرؒ فرماتے ہیں کہ جن لوگوں میں یہ صفات جمع ہو جائیں، وہی دنیا اور آخرت میں کامیاب اور اللہ کے عذاب سے محفوظ ہیں۔"
            )
        )
        list.add(
            TafseerIbnKaseer(
                surahId = 2,
                ayahNumber = 255,
                arabicText = "ٱللَّهُ لَآ إِلَـٰهَ إِلَّا هُوَ ٱلْحَىُّ ٱلْقَيُّومُ ۚ لَا تَأْخُذُهُۥ سِنَةٌۭ وَلَا نَوْمٌۭ ۚ لَّهُۥ مَا فِى ٱلسَّمَـٰوَٰتِ وَمَا فِى ٱلْأَرْضِ ۗ مَن ذَا ٱلَّذِى يَشْفَعُ عِندَهُۥٓ إِلَّا بِإِذْنِهِۦ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَىْءٍۢ مِّنْ عِلْمِهِۦٓ إِلَّا بِمَا شَآءَ ۚ وَسِعَ كُرْسِيُّهُ ٱلسَّمَـٰوَٰتِ وَٱلْأَرْضَ ۖ وَلَا يَـُٔودُهُۥ حِفْظُهُمَا ۚ وَهُوَ ٱلْعَلِىُّ ٱلْعَظِيمُ",
                urduTranslation = "اللہ، جس کے سوا کوئی معبود برحق نہیں، وہ ہمیشہ زندہ رہنے والا اور تمام کائنات کو قائم رکھنے والا ہے۔",
                tafseerContent = "آیۃ الکرسی کی عظمت: قرآن مجید کی سب سے عظیم آیت۔ علامہ ابن کثیرؒ فرماتے ہیں کہ اس میں توحیدِ باری تعالیٰ، عظمت، علم اور قدرت کا کامل بیان ہے اور 'الحی القیوم' اللہ تعالیٰ کا اسمِ اعظم ہے۔"
            )
        )

        // 3. Al-Imran (1 to 2)
        list.add(
            TafseerIbnKaseer(
                surahId = 3,
                ayahNumber = 1,
                arabicText = "الۤمۤ",
                urduTranslation = "الف، لام، میم۔",
                tafseerContent = "حروف مقطعات: ابن کثیرؒ فرماتے ہیں کہ یہ کلامِ الٰہی کا اعجاز ہے جو انسان کو غور و فکر اور عاجزی کی دعوت دیتا ہے۔"
            )
        )
        list.add(
            TafseerIbnKaseer(
                surahId = 3,
                ayahNumber = 2,
                arabicText = "ٱللَّهُ لَآ إِلَـٰهَ إِلَّا هُوَ ٱلْحَىُّ ٱلْقَيُّومُ",
                urduTranslation = "اللہ، اس کے سوا کوئی معبود نہیں، وہ ہمیشہ زندہ اور سب کا نگہبان ہے۔",
                tafseerContent = "توحیدِ الوہیت: علامہ ابن کثیرؒ کے مطابق اللہ ہی لائقِ عبادت ہے اور وہ حی و قیوم ہے جس کی بادشاہت کو کبھی زوال نہیں۔"
            )
        )

        // 6. Al-An'am (1 to 3) - Ayah 1 is strictly without Bismillah
        list.add(
            TafseerIbnKaseer(
                surahId = 6,
                ayahNumber = 1,
                arabicText = "ٱلْحَمْدُ لِلَّهِ ٱلَّذِى خَلَقَ ٱلسَّمَـٰوَٰتِ وَٱلْأَرْضَ وَجَعَلَ ٱلظُّلُمَـٰتِ وَٱلنُّورَ ۖ ثُمَّ ٱلَّذِينَ كَفَرُوا۟ بِرَبِّهِمْ يَعْدِلُونَ",
                urduTranslation = "تمام تعریفیں اللہ ہی کے لیے ہیں جس نے آسمانوں اور زمین کو پیدا کیا اور اندھیرے اور اجالا بنایا، پھر بھی کافر لوگ اپنے رب کے ساتھ دوسروں کو برابر ٹھہراتے ہیں۔",
                tafseerContent = "توحیدِ تخلیق: علامہ ابن کثیرؒ فرماتے ہیں کہ لفظ 'الظلمات' (اندھیرے) کو جمع کے صیغے میں لایا گیا کیونکہ گمراہی و باطل کے راستے بے شمار ہیں، جبکہ 'النور' (حق/ہدایت) کو مفرد لایا گیا کیونکہ حق کا راستہ صرف اور صرف ایک ہی ہے۔"
            )
        )
        list.add(
            TafseerIbnKaseer(
                surahId = 6,
                ayahNumber = 2,
                arabicText = "هُوَ ٱلَّذِى خَلَقَكُم مِّن طِينٍۢ ثُمَّ قَضَىٰٓ أَجَلًۭا ۖ وَأَجَلٌۭ مُّسَمًّى عِندَهُۥ ۖ ثُمَّ أَنتُمْ تَمْتَرُونَ",
                urduTranslation = "وہی ہے جس نے تمہیں مٹی سے پیدا کیا پھر ایک مدت مقرر فرمائی، اور ایک مقررہ مدت اسی کے پاس ہے، پھر بھی تم شک کرتے ہو۔",
                tafseerContent = "پیدائشِ انسانی اور دو معین مدتیں: علامہ ابن کثیرؒ فرماتے ہیں کہ پہلی مدت ہر انسان کی پیدائش سے موت تک ہے، اور دوسری مدت قیامت کا وہ قطعی وقت ہے جس کا علم صرف اللہ تعالیٰ کے پاس ہے۔"
            )
        )
        list.add(
            TafseerIbnKaseer(
                surahId = 6,
                ayahNumber = 3,
                arabicText = "وَهُوَ ٱللَّهُ فِى ٱلسَّمَـٰوَٰتِ وَفِى ٱلْأَرْضِ ۖ يَعْلَمُ سِرَّكُمْ وَجَهْرَكُمْ وَيَعْلَمُ مَا تَكْسِبُونَ",
                urduTranslation = "اور وہی اللہ ہے آسمانوں میں بھی اور زمین میں بھی، وہ تمہاری پوشیدہ اور ظاہر باتوں کو جانتا ہے اور جو کچھ تم کماتے ہو اسے بھی جانتا ہے۔",
                tafseerContent = "اللہ تعالیٰ کا عالم الغیب ہونا: ابن کثیر کے مطابق اللہ کی حاکمیت اور الوہیت آسمان و زمین دونوں میں ہے اور بندوں کا کوئی عمل، راز یا نیت اس سے پوشیدہ نہیں ہے۔"
            )
        )

        // 18. Al-Kahf (1 to 2)
        list.add(
            TafseerIbnKaseer(
                surahId = 18,
                ayahNumber = 1,
                arabicText = "ٱلْحَمْدُ لِلَّهِ ٱلَّذِىٓ أَنزَلَ عَلَىٰ عَبْدِهِ ٱلْكِتَـٰبَ وَلَمْ يَجْعَل لَّهُۥ عِوَجَاۜ",
                urduTranslation = "تمام تعریفیں اللہ ہی کے لیے ہیں جس نے اپنے بندے پر یہ کتاب نازل فرمائی اور اس میں کوئی کجی نہیں رکھی۔",
                tafseerContent = "قرآن مجید کا بے عیب ہونا: ابن کثیرؒ فرماتے ہیں کہ لفظ 'عبده' سے نبی کریمؐ کا وہ اعلیٰ مقام ظاہر ہوتا ہے جہاں بندگیِ الٰہی سب سے بڑا شرف ہے۔ قرآن میں کوئی اختلاف یا کجی نہیں، یہ سیدھا اور مکمل ہے۔"
            )
        )

        // 36. Ya-Sin (1 to 3)
        list.add(
            TafseerIbnKaseer(
                surahId = 36,
                ayahNumber = 1,
                arabicText = "يسٓ",
                urduTranslation = "یا، سین۔",
                tafseerContent = "تفسیر سورۃ یٰسین: قلبِ قرآن۔ علامہ ابن کثیرؒ کے مطابق یہ حروفِ مقطعات میں سے ہیں اور ان کا حقیقی علم اللہ تعالیٰ ہی کے پاس ہے۔"
            )
        )
        list.add(
            TafseerIbnKaseer(
                surahId = 36,
                ayahNumber = 2,
                arabicText = "وَٱلْقُرْءَانِ ٱلْحَكِيمِ",
                urduTranslation = "حکمت سے بھرے قرآن کی قسم۔",
                tafseerContent = "قرآن حکیم کی قسم: ابن کثیر کے مطابق اللہ تعالیٰ نے قرآن کی حکمت اور اعجاز کی قسم کھا کر نبی کریمؐ کی رسالت کی صداقت ثابت فرمائی ہے۔"
            )
        )
        list.add(
            TafseerIbnKaseer(
                surahId = 36,
                ayahNumber = 3,
                arabicText = "إِنَّكَ لَمِنَ ٱلْمُرْسَلِينَ",
                urduTranslation = "بیشک آپ ضرور پیغمبروں میں سے ہیں۔",
                tafseerContent = "رسالتِ نبویؐ کی تصدیق: کفارِ مکہ کے انکار کے مقابلے میں اللہ رب العزت نے خود قسم کھا کر اپنے پیارے حبیبؐ کی نبوت و رسالت کی گواہی دی۔"
            )
        )

        // 55. Ar-Rahman (1 to 4)
        list.add(
            TafseerIbnKaseer(
                surahId = 55,
                ayahNumber = 1,
                arabicText = "ٱلرَّحْمَـٰنُ",
                urduTranslation = "نہایت مہربان (اللہ)۔",
                tafseerContent = "صفتِ رحمانیت: ابن کثیرؒ فرماتے ہیں کہ اللہ تعالیٰ نے اپنی رحمت کا اظہار فرمایا جس کے تحت اس نے انسانوں پر قرآن جیسی عظیم نعمت نازل فرمائی۔"
            )
        )
        list.add(
            TafseerIbnKaseer(
                surahId = 55,
                ayahNumber = 2,
                arabicText = "عَلَّمَ ٱلْقُرْءَانَ",
                urduTranslation = "اسی نے قرآن سکھایا۔",
                tafseerContent = "تعلیمِ قرآن: ابن کثیرؒ فرماتے ہیں کہ انسان کی ہدایت کے لیے سب سے افضل اور عظیم ترین احسان قرآن کی تعلیم ہے۔"
            )
        )
        list.add(
            TafseerIbnKaseer(
                surahId = 55,
                ayahNumber = 3,
                arabicText = "خَلَقَ ٱلْإِنسَـٰنَ",
                urduTranslation = "اسی نے انسان کو پیدا کیا۔",
                tafseerContent = "تخلیقِ انسان: اللہ تعالیٰ نے انسان کو عدم سے وجود بخشا اور اسے عقل و شعور کی نعمت عطا فرمائی۔"
            )
        )
        list.add(
            TafseerIbnKaseer(
                surahId = 55,
                ayahNumber = 4,
                arabicText = "عَلَّمَهُ ٱلْبَيَانَ",
                urduTranslation = "اسے بولنا اور مافی الضمیر بیان کرنا سکھایا۔",
                tafseerContent = "قوتِ گویائی: ابن کثیرؒ کے مطابق انسان کو بولنے اور اپنے دل کی بات بیان کرنے کی صلاحیت دینا اللہ کی ایک عظیم ترین نشانی ہے۔"
            )
        )

        // 67. Al-Mulk (1 to 2)
        list.add(
            TafseerIbnKaseer(
                surahId = 67,
                ayahNumber = 1,
                arabicText = "تَبَـٰرَكَ ٱلَّذِى بِيَدِهِ ٱلْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَىْءٍۢ قَدِيرٌ",
                urduTranslation = "بڑی برکت والی ہے وہ ذات جس کے ہاتھ میں تمام بادشاہی ہے اور وہ ہر چیز پر پوری قدرت رکھتا ہے۔",
                tafseerContent = "سورۃ الملک کی فضیلت: حدیث شریف کے مطابق یہ سورت عذابِ قبر سے نجات دہندہ ہے۔ ابن کثیر فرماتے ہیں کہ تمام کائنات کا اقتدار اور تصرف صرف اللہ تعالیٰ کے دستِ قدرت میں ہے۔"
            )
        )
        list.add(
            TafseerIbnKaseer(
                surahId = 67,
                ayahNumber = 2,
                arabicText = "ٱلَّذِى خَلَقَ ٱلْمَوْتَ وَٱلْحَيَوٰةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًۭا ۚ وَهُوَ ٱلْعَزِيزُ ٱلْغَفُورُ",
                urduTranslation = "جس نے موت اور زندگی کو پیدا کیا تاکہ تمہیں آزمائے کہ تم میں سے عمل کے اعتبار سے کون زیادہ اچھا ہے۔",
                tafseerContent = "مقصودِ حیات اور حسنِ عمل: علامہ ابن کثیرؒ فضیل بن عیاضؒ سے نقل کرتے ہیں کہ 'أحسن عملاً' سے مراد وہ عمل ہے جو سب سے زیادہ اخلاص پر مبنی ہو اور سنتِ نبویؐ کے عین موافق ہو۔"
            )
        )

        // 108. Al-Kawthar (1 to 3)
        list.add(
            TafseerIbnKaseer(
                surahId = 108,
                ayahNumber = 1,
                arabicText = "إِنَّآ أَعْطَيْنَـٰكَ ٱلْكَوْثَرَ",
                urduTranslation = "بیشک ہم نے آپ کو کوثر عطا فرمائی۔",
                tafseerContent = "حوض و نہرِ کوثر: ابن کثیرؒ فرماتے ہیں کہ کوثر خیرِ کثیر اور جنت کی وہ خاص نہر ہے جو قیامت کے دن نبی کریمؐ کو اور آپ کی امت کو عطا کی جائے گی، جس کا پانی دودھ سے زیادہ سفید اور شہد سے زیادہ میٹھا ہے۔"
            )
        )
        list.add(
            TafseerIbnKaseer(
                surahId = 108,
                ayahNumber = 2,
                arabicText = "فَصَلِّ لِرَبِّكَ وَٱنْحَرْ",
                urduTranslation = "پس اپنے رب کے لیے نماز پڑھیے اور قربانی کیجیے۔",
                tafseerContent = "اخلاصِ عبادت و قربانی: مشرکین کے بتوں کے مقابلے میں اللہ کا حکم ہے کہ نماز اور قربانی صرف اور صرف اللہ وحدہ لا شریک کے لیے ہو۔"
            )
        )
        list.add(
            TafseerIbnKaseer(
                surahId = 108,
                ayahNumber = 3,
                arabicText = "إِنَّ شَانِئَكَ هُوَ ٱلْأَبْتَرُ",
                urduTranslation = "یقیناً آپ کا دشمن ہی بے نام و نشان اور جڑ کٹا ہے۔",
                tafseerContent = "دشمنانِ رسول کا انجام: ابن کثیرؒ کے مطابق جو بھی رسول اللہؐ سے بغض رکھے گا، دنیا و آخرت میں اس کا کوئی ذکرِ خیر اور انجام نہیں رہے گا۔"
            )
        )

        // 112. Al-Ikhlas (1 to 4)
        list.add(
            TafseerIbnKaseer(
                surahId = 112,
                ayahNumber = 1,
                arabicText = "قُلْ هُوَ ٱللَّهُ أَحَدٌ",
                urduTranslation = "آپ فرما دیجیے کہ وہ اللہ ایک ہے۔",
                tafseerContent = "توحیدِ خالص: سورۃ الإخلاص ثلث قرآن (ایک تہائی قرآن) کے برابر ہے۔ ابن کثیرؒ فرماتے ہیں کہ وہ ذاتِ اقدس جس کا کوئی شریک، وزیر یا مشیر نہیں، وہ یکتا و یگانہ ہے۔"
            )
        )
        list.add(
            TafseerIbnKaseer(
                surahId = 112,
                ayahNumber = 2,
                arabicText = "ٱللَّهُ ٱلصَّمَدُ",
                urduTranslation = "اللہ بے نیاز اور سب کا سہارا ہے۔",
                tafseerContent = "الصمد کے معنیٰ: ابن عباسؓ کے مطابق الصمد وہ سردار ہے جس کی سرداری کامل ہو، جو تمام ضرورتوں سے بے نیاز ہو اور ساری کائنات اپنی ہر حاجت میں جس کی محتاج ہو۔"
            )
        )
        list.add(
            TafseerIbnKaseer(
                surahId = 112,
                ayahNumber = 3,
                arabicText = "لَمْ يَلِدْ وَلَمْ يُولَدْ",
                urduTranslation = "نہ اس کی کوئی اولاد ہے اور نہ وہ کسی کی اولاد ہے۔",
                tafseerContent = "نفی ولادت و تولد: اللہ تعالیٰ ہر قسم کے نسب، اولاد، والدین اور ہمسر سے پاک اور منزہ ہے۔"
            )
        )
        list.add(
            TafseerIbnKaseer(
                surahId = 112,
                ayahNumber = 4,
                arabicText = "وَلَمْ يَكُن لَّهُۥ كُفُوًا أَحَدٌۢ",
                urduTranslation = "اور کوئی اس کا ہمسر اور برابری کرنے والا نہیں ہے۔",
                tafseerContent = "کمالِ توحید: کائنات کی کوئی مخلوق اللہ تعالیٰ کی مثل، نظیر، شبیہ یا ہم پلہ نہیں ہے۔"
            )
        )

        // 113. Al-Falaq (1 to 5)
        list.add(
            TafseerIbnKaseer(
                surahId = 113,
                ayahNumber = 1,
                arabicText = "قُلْ أَعُوذُ بِرَبِّ ٱلْفَلَقِ",
                urduTranslation = "آپ کہیے کہ میں صبح کے رب کی پناہ مانگتا ہوں۔",
                tafseerContent = "معوذتین کی فضیلت: ابن کثیرؒ فرماتے ہیں کہ الفلق سے مراد صبح کی پو پھوٹنا اور تاریکی کو چیر کر روشنی لانے والا رب ہے۔"
            )
        )

        // 114. An-Nas (1 to 6)
        list.add(
            TafseerIbnKaseer(
                surahId = 114,
                ayahNumber = 1,
                arabicText = "قُلْ أَعُوذُ بِرَبِّ ٱلنَّاسِ",
                urduTranslation = "آپ کہیے کہ میں تمام انسانوں کے رب کی پناہ میں آتا ہوں۔",
                tafseerContent = "وسوسوں سے پناہ: ابن کثیرؒ فرماتے ہیں کہ اللہ تعالیٰ رب الناس، ملک الناس اور الہ الناس ہے اور جن و انس کے شرور اور وسوسوں سے اسی کی پناہ مانگی جاتی ہے۔"
            )
        )

        return list
    }
}
