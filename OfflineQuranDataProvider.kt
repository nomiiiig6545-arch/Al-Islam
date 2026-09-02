package com.example.data.mushaf

import android.content.Context
import android.util.Log
import com.example.data.api.Surah
import com.example.data.db.AyahEntity
import com.example.data.db.SurahEntity
import com.example.util.QuranSanitizer
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.GZIPInputStream

object OfflineQuranDataProvider {

    private const val TAG = "OfflineQuranData"
    private var applicationContext: Context? = null
    private val parsedSurahsCache = ConcurrentHashMap<Int, List<AyahEntity>>()
    private var fullQuranJsonObject: JSONObject? = null
    private val lock = Any()

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }

    /**
     * Complete list of all 114 Surahs for offline use.
     */
    fun getOfflineSurahsList(): List<Surah> {
        return IndoPakMushafData.SURAH_NAMES_ARABIC.mapIndexed { index, arabicName ->
            val number = index + 1
            val englishName = IndoPakMushafData.SURAH_NAMES_ENGLISH.getOrElse(index) { "Surah $number" }
            val ayahCount = IndoPakMushafData.SURAH_AYAH_COUNTS.getOrElse(index) { 7 }
            val revelation = if (number in listOf(1, 2, 3, 4, 5, 8, 9, 13, 22, 24, 33, 47, 48, 49, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 110)) "Medinan" else "Meccan"

            Surah(
                number = number,
                name = "سُورَةُ $arabicName",
                englishName = englishName,
                englishNameTranslation = "Chapter $number",
                numberOfAyahs = ayahCount,
                revelationType = revelation
            )
        }
    }

    /**
     * Get offline SurahEntity list for Room database pre-population.
     */
    fun getOfflineSurahEntities(): List<SurahEntity> {
        return getOfflineSurahsList().map { surah ->
            SurahEntity(
                number = surah.number,
                name = surah.name,
                englishName = surah.englishName,
                englishNameTranslation = surah.englishNameTranslation,
                numberOfAyahs = surah.numberOfAyahs,
                revelationType = surah.revelationType
            )
        }
    }

    /**
     * Generates or fetches authentic offline Ayahs with Arabic and Urdu text for a given Surah.
     * Loads from the bundled complete Quran JSON asset.
     */
    fun getOfflineAyahsForSurah(surahNumber: Int, context: Context? = null): List<AyahEntity> {
        val clampedSurah = surahNumber.coerceIn(1, 114)
        
        // 1. Check in-memory cache
        parsedSurahsCache[clampedSurah]?.let { return it }

        val ctx = context?.applicationContext ?: applicationContext
        if (ctx != null) {
            val loadedList = loadSurahFromAssets(ctx, clampedSurah)
            if (loadedList.isNotEmpty()) {
                parsedSurahsCache[clampedSurah] = loadedList
                return loadedList
            }
        }

        // 2. Fallback to sample text if asset is somehow unreachable
        val surahName = IndoPakMushafData.SURAH_NAMES_ARABIC.getOrElse(clampedSurah - 1) { "" }
        val totalAyahs = IndoPakMushafData.SURAH_AYAH_COUNTS.getOrElse(clampedSurah - 1) { 7 }
        val sampleTexts = getSampleSurahTexts(clampedSurah)

        val list = ArrayList<AyahEntity>(totalAyahs)
        for (i in 1..totalAyahs) {
            val rawText = sampleTexts.getOrElse(i - 1) {
                "آيَةُ $i مِنْ سُورَةِ $surahName"
            }
            val text = QuranSanitizer.cleanAyahArabic(rawText, clampedSurah, i)
            val urduText = QuranSanitizer.cleanAyahUrdu(getSampleUrduText(clampedSurah, i), clampedSurah, i)

            list.add(
                AyahEntity(
                    surahNumber = clampedSurah,
                    numberInSurah = i,
                    overallNumber = clampedSurah * 100 + i,
                    arabicText = text,
                    urduText = urduText,
                    audioUrl = "https://everyayah.com/data/Alafasy_64kbps/${String.format("%03d%03d", clampedSurah, i)}.mp3",
                    urduAudioUrl = "https://cdn.islamic.network/quran/audio/64/ur.khan/${clampedSurah * 100 + i}.mp3"
                )
            )
        }
        parsedSurahsCache[clampedSurah] = list
        return list
    }

    private fun loadSurahFromAssets(context: Context, surahNumber: Int): List<AyahEntity> {
        try {
            ensureJsonObjectLoaded(context)
            val json = fullQuranJsonObject ?: return emptyList()
            val surahKey = surahNumber.toString()
            if (!json.has(surahKey)) return emptyList()

            val array = json.getJSONArray(surahKey)
            val list = ArrayList<AyahEntity>(array.length())

            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val ayahNum = item.getInt("num")
                val overall = item.getInt("overall")
                val rawAr = item.getString("ar")
                val rawUr = item.getString("ur")

                val cleanAr = QuranSanitizer.cleanAyahArabic(rawAr, surahNumber, ayahNum)
                val cleanUr = QuranSanitizer.cleanAyahUrdu(rawUr, surahNumber, ayahNum)

                list.add(
                    AyahEntity(
                        surahNumber = surahNumber,
                        numberInSurah = ayahNum,
                        overallNumber = overall,
                        arabicText = cleanAr,
                        urduText = cleanUr,
                        audioUrl = "https://everyayah.com/data/Alafasy_64kbps/${String.format("%03d%03d", surahNumber, ayahNum)}.mp3",
                        urduAudioUrl = "https://cdn.islamic.network/quran/audio/64/ur.khan/$overall.mp3"
                    )
                )
            }
            return list
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load Surah $surahNumber from assets: ${e.message}")
            return emptyList()
        }
    }

    /**
     * Efficiently loads and parses all 6236 Ayahs of all 114 Surahs from the bundled asset JSON.
     */
    fun loadAllAyahsFromAssets(context: Context): List<AyahEntity> {
        val allAyahs = ArrayList<AyahEntity>(6236)
        try {
            ensureJsonObjectLoaded(context)
            val json = fullQuranJsonObject ?: return emptyList()

            for (surahNum in 1..114) {
                val surahKey = surahNum.toString()
                if (!json.has(surahKey)) continue

                val array = json.getJSONArray(surahKey)
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    val ayahNum = item.getInt("num")
                    val overall = item.getInt("overall")
                    val rawAr = item.getString("ar")
                    val rawUr = item.getString("ur")

                    val cleanAr = QuranSanitizer.cleanAyahArabic(rawAr, surahNum, ayahNum)
                    val cleanUr = QuranSanitizer.cleanAyahUrdu(rawUr, surahNum, ayahNum)

                    allAyahs.add(
                        AyahEntity(
                            surahNumber = surahNum,
                            numberInSurah = ayahNum,
                            overallNumber = overall,
                            arabicText = cleanAr,
                            urduText = cleanUr,
                            audioUrl = "https://everyayah.com/data/Alafasy_64kbps/${String.format("%03d%03d", surahNum, ayahNum)}.mp3",
                            urduAudioUrl = "https://cdn.islamic.network/quran/audio/64/ur.khan/$overall.mp3"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load all Ayahs from assets: ${e.message}")
        }
        return allAyahs
    }

    private fun ensureJsonObjectLoaded(context: Context) {
        if (fullQuranJsonObject != null) return
        synchronized(lock) {
            if (fullQuranJsonObject != null) return
            try {
                // Try reading compressed .gz first, or raw .json
                var inputStream: InputStream? = null
                try {
                    inputStream = GZIPInputStream(context.assets.open("quran/quran_text.json.gz"))
                } catch (_: Exception) {
                    try {
                        inputStream = context.assets.open("quran/quran_text.json")
                    } catch (_: Exception) {}
                }

                if (inputStream != null) {
                    val text = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    try {
                        fullQuranJsonObject = JSONObject(text)
                    } catch (e: org.json.JSONException) {
                        Log.w(TAG, "JSON parsing failed, attempting to recover truncated data: ${e.message}")
                        val lastBracketIndex = text.lastIndexOf(']')
                        if (lastBracketIndex != -1) {
                            val fixedText = text.substring(0, lastBracketIndex + 1) + "}"
                            try {
                                fullQuranJsonObject = JSONObject(fixedText)
                            } catch (e2: Exception) {
                                Log.e(TAG, "Failed to recover JSON: ${e2.message}")
                            }
                        } else {
                            Log.e(TAG, "Could not find valid JSON array to recover.")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Quran JSON asset: ${e.message}")
            }
        }
    }

    /**
     * Returns offline Ayahs corresponding to a specific 16-Line Mushaf Page Number (1..549).
     */
    fun getOfflineAyahsForPage(context: Context?, pageNumber: Int): List<AyahEntity> {
        val page = pageNumber.coerceIn(1, IndoPakMushafData.TOTAL_PAGES)
        val pageInfo = IndoPakMushafData.getPageInfo(page)

        // Find surah starting or running on this page
        val surahNum = pageInfo.surahNumber
        val ayahsOfSurah = getOfflineAyahsForSurah(surahNum)

        // Slicing ayahs for page estimation
        val totalPagesInSurah = if (surahNum < 114) {
            (IndoPakMushafData.SURAH_START_PAGES[surahNum] - IndoPakMushafData.SURAH_START_PAGES[surahNum - 1]).coerceAtLeast(1)
        } else 1

        val pageOffset = (page - IndoPakMushafData.SURAH_START_PAGES[surahNum - 1]).coerceAtLeast(0)
        val ayahsPerPage = (ayahsOfSurah.size / totalPagesInSurah).coerceAtLeast(1)

        val startIndex = (pageOffset * ayahsPerPage).coerceAtMost(ayahsOfSurah.size - 1)
        val endIndex = ((pageOffset + 1) * ayahsPerPage).coerceAtMost(ayahsOfSurah.size)

        val pageAyahs = if (startIndex < endIndex) {
            ayahsOfSurah.subList(startIndex, endIndex)
        } else {
            ayahsOfSurah.take(1)
        }

        return pageAyahs
    }

    private fun getSampleSurahTexts(surahNumber: Int): List<String> {
        return when (surahNumber) {
            1 -> listOf(
                "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَٰلَمِينَ",
                "ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                "مَٰلِكِ يَوْمِ ٱلدِّينِ",
                "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ",
                "ٱهْدِنَا ٱلصِّرَٰطَ ٱلْمُسْتَقِيمَ",
                "صِرَٰطَ ٱلَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ ٱلْمَغْضُوبِ عَلَيْهِمْ وَلَا ٱلضَّآلِّينَ"
            )
            2 -> listOf(
                "الم",
                "ذَٰلِكَ ٱلْكِتَٰبُ لَا رَيْبَ ۛ فِيهِ ۛ هُدًى لِّلْمُتَّقِينَ",
                "ٱلَّذِينَ يُؤْمِنُونَ بِٱلْغَيْبِ وَيُقِيمُونَ ٱلصَّلَوٰةَ وَمِمَّا رَزَقْنَٰهُمْ يُنفِقُونَ",
                "وَٱلَّذِينَ يُؤْمِنُونَ بِمَآ أُنزِلَ إِلَيْكَ وَمَآ أُنزِلَ مِن قَبْلِكَ وَبِٱلْـَٔاخِرَةِ هُمْ يُوقِنُونَ",
                "أُولَٰٓئِكَ عَلَىٰ هُدًى مِّن رَّبِّهِمْ ۖ وَأُولَٰٓئِكَ هُمُ ٱلْمُفْلِحُونَ"
            )
            3 -> listOf( // Al-Imran
                "الۤمۤ",
                "ٱللَّهُ لَآ إِلَـٰهَ إِلَّا هُوَ ٱلْحَىُّ ٱلْقَيُّومُ",
                "نَزَّلَ عَلَيْكَ ٱلْكِتَـٰبَ بِٱلْحَقِّ مُصَدِّقًۭا لِّمَا بَيْنَ يَدَيْهِ وَأَنزَلَ ٱلتَّوْرَىٰةَ وَٱلْإِنجِيلَ",
                "مِن قَبْلُ هُدًۭى لِّلنَّاسِ وَأَنزَلَ ٱلْفُرْقَانَ ۗ إِنَّ ٱلَّذِينَ كَفَرُوا۟ بِـَٔايَـٰتِ ٱللَّهِ لَهُمْ عَذَابٌۭ شَدِيدٌۭ ۗ وَٱللَّهُ عَزِيزٌۭ ذُو ٱنتِقَامٍ"
            )
            4 -> listOf( // An-Nisa
                "يَـٰٓأَيُّهَا ٱلنَّاسُ ٱتَّقُوا۟ رَبَّكُمُ ٱلَّذِى خَلَقَكُم مِّن نَّفْسٍۢ وَٰحِدَةٍۢ وَخَلَقَ مِنْهَا زَوْجَهَا وَبَثَّ مِنْهُمَا رِجَالًۭا كَثِيرًۭا وَنِسَآءًۭ ۚ وَٱتَّقُوا۟ ٱللَّهَ ٱلَّذِى تَسَآءَلُونَ بِهِۦ وَٱلْأَرْحَامَ ۚ إِنَّ ٱللَّهَ كَانَ عَلَيْكُمْ رَقِيبًۭا",
                "وَءَاتُوا۟ ٱلْيَتَـٰمَىٰٓ أَمْوَٰلَهُمْ ۖ وَلَا تَتَبَدَّلُوا۟ ٱلْخَبِيثَ بِٱلطَّيِّبِ ۖ وَلَا تَأْكُلُوٓا۟ أَمْوَٰلَهُمْ إِلَىٰٓ أَمْوَٰلِكُمْ ۚ إِنَّهُۥ كَانَ حُوبًۭا كَبِيرًۭا"
            )
            5 -> listOf( // Al-Ma'idah
                "يَـٰٓأَيُّهَا ٱلَّذِينَ ءَامَنُوٓا۟ أَوْفُوا۟ بِٱلْعُقُودِ ۚ أُحِلَّتْ لَكُم بَهِيمَةُ ٱلْأَنْعَـٰمِ إِلَّا مَا يُتْلَىٰ عَلَيْكُمْ غَيْرَ مُحِلِّى ٱلصَّيْدِ وَأَنتُمْ حُرُمٌ ۗ إِنَّ ٱللَّهَ يَحْكُمُ مَا يُرِيدُ"
            )
            6 -> listOf( // Al-An'am
                "ٱلْحَمْدُ لِلَّهِ ٱلَّذِى خَلَقَ ٱلسَّمَـٰوَٰتِ وَٱلْأَرْضَ وَجَعَلَ ٱلظُّلُمَـٰتِ وَٱلنُّورَ ۖ ثُمَّ ٱلَّذِينَ كَفَرُوا۟ بِرَبِّهِمْ يَعْدِلُونَ",
                "هُوَ ٱلَّذِى خَلَقَكُم مِّن طِينٍۢ ثُمَّ قَضَىٰٓ أَجَلًۭا ۖ وَأَجَلٌۭ مُّسَمًّى عِندَهُۥ ۖ ثُمَّ أَنتُمْ تَمْتَرُونَ",
                "وَهُوَ ٱللَّهُ فِى ٱلسَّمَـٰوَٰتِ وَفِى ٱلْأَرْضِ ۖ يَعْلَمُ سِرَّكُمْ وَجَهْرَكُمْ وَيَعْلَمُ مَا تَكْسِبُونَ"
            )
            18 -> listOf( // Al-Kahf
                "ٱلْحَمْدُ لِلَّهِ ٱلَّذِىٓ أَنزَلَ عَلَىٰ عَبْدِهِ ٱلْكِتَـٰبَ وَلَمْ يَجْعَل لَّهُۥ عِوَجَاۜ",
                "قَيِّمًۭا لِّيُنذِرَ بَأْسًۭا شَدِيدًۭا مِّن لَّدُنْهُ وَيُبَشِّرَ ٱلْمُؤْمِنِينَ ٱلَّذِينَ يَعْمَلُونَ ٱلصَّـٰلِحَـٰتِ أَنَّ لَهُمْ أَجْرًا حَسَنًۭا"
            )
            36 -> listOf( // Yaseen
                "يس",
                "وَٱلْقُرْءَانِ ٱلْحَكِيمِ",
                "إِنَّكَ لَمِنَ ٱلْمُرْسَلِينَ",
                "عَلَىٰ صِرَٰطٍ مُّسْتَقِيمٍ",
                "تَنزِيلَ ٱلْعَزِيزِ ٱلرَّحِيمِ"
            )
            55 -> listOf( // Ar-Rahman
                "ٱلرَّحْمَـٰنُ",
                "عَلَّمَ ٱلْقُرْءَانَ",
                "خَلَقَ ٱلْإِنسَـٰنَ",
                "عَلَّمَهُ ٱلْبَيَانَ",
                "ٱلشَّمْسُ وَٱلْقَمَرُ بِحُسْبَانٍۢ"
            )
            67 -> listOf( // Mulk
                "تَبَٰرَكَ ٱلَّذِي بِيَدِهِ ٱلْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ",
                "ٱلَّذِي خَلَقَ ٱلْمَوْتَ وَٱلْحَيَوٰةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًا ۚ وَهُوَ ٱلْعَزِيزُ ٱلْغَفُورُ"
            )
            103 -> listOf( // Al-Asr
                "وَٱلْعَصْرِ",
                "إِنَّ ٱلْإِنسَـٰنَ لَفِى خُسْرٍ",
                "إِلَّا ٱلَّذِينَ ءَامَنُوا۟ وَعَمِلُوا۟ ٱلصَّـٰلِحَـٰتِ وَتَوَاصَوْا۟ بِٱلْحَقِّ وَتَوَاصَوْا۟ بِٱلصَّبْرِ"
            )
            108 -> listOf( // Al-Kawthar
                "إِنَّآ أَعْطَيْنَـٰكَ ٱلْكَوْثَرَ",
                "فَصَلِّ لِرَبِّكَ وَٱنْحَرْ",
                "إِنَّ شَانِئَكَ هُوَ ٱلْأَبْتَرُ"
            )
            112 -> listOf( // Ikhlas
                "قُلْ هُوَ ٱللَّهُ أَحَدٌ",
                "ٱللَّهُ ٱلصَّمَدُ",
                "لَمْ يَلِدْ وَلَمْ يُولَدْ",
                "وَلَمْ يَكُن لَّهُۥ كُفُوًا أَحَدٌ"
            )
            113 -> listOf( // Falaq
                "قُلْ أَعُوذُ بِرَبِّ ٱلْفَلَقِ",
                "مِن شَرِّ مَا خَلَقَ",
                "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ",
                "وَمِن شَرِّ ٱلنَّفَّٰثَٰتِ فِي ٱلْعُقَدِ",
                "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ"
            )
            114 -> listOf( // Nas
                "قُلْ أَعُوذُ بِرَبِّ ٱلنَّاسِ",
                "مَلِكِ ٱلنَّاسِ",
                "إِلَٰهِ ٱلنَّاسِ",
                "مِن شَرِّ ٱلْوَسْوَاسِ ٱلْخَنَّاسِ",
                "ٱلَّذِي يُوَسْوِسُ فِي صُدُورِ ٱلنَّاسِ",
                "مِنَ ٱلْجِنَّةِ وَٱلنَّاسِ"
            )
            else -> emptyList()
        }
    }

    private fun getSampleUrduText(surahNumber: Int, ayahNumber: Int): String {
        return when (surahNumber) {
            1 -> when (ayahNumber) {
                1 -> "شروع اللہ کے نام سے جو بڑا مہربان نہایت رحم والا ہے۔"
                2 -> "سب تعریفیں اللہ ہی کے لیے ہیں جو تمام جہانوں کا پروردگار ہے۔"
                3 -> "بڑا مہربان، نہایت رحم کرنے والا۔"
                4 -> "روزِ جزا کا مالک۔"
                5 -> "ہم تیری ہی عبادت کرتے ہیں اور تجھ ہی سے مدد مانگتے ہیں۔"
                6 -> "ہمیں سیدھا راستہ دکھا۔"
                7 -> "ان لوگوں کا راستہ جن پر تو نے انعام فرمایا، نہ ان کا جن پر غضب ہوا اور نہ گمراہوں کا۔"
                else -> "ترجمہ آیت $ayahNumber"
            }
            2 -> when (ayahNumber) {
                1 -> "الف، لام، میم۔"
                2 -> "یہ وہ کتاب ہے جس میں کسی شک کی گنجائش نہیں، پرہیزگاروں کے لیے سراسر ہدایت ہے۔"
                3 -> "جو غیب پر ایمان لاتے ہیں اور نماز قائم کرتے ہیں اور جو کچھ ہم نے انہیں دیا ہے اس میں سے خرچ کرتے ہیں۔"
                4 -> "اور جو لوگ ایمان لاتے ہیں اس پر جو آپ کی طرف نازل کیا گیا اور جو آپ سے پہلے نازل کیا گیا، اور آخرت پر وہ یقین رکھتے ہیں۔"
                5 -> "یہی لوگ اپنے رب کی طرف سے ہدایت پر ہیں اور یہی فلاح پانے والے ہیں۔"
                255 -> "اللہ، جس کے سوا کوئی معبود نہیں، وہ ہمیشہ زندہ اور سب کو سنبھالنے والا ہے۔"
                else -> "اللہ کے نام سے جو بڑا مہربان نہایت رحم والا ہے۔ (سورۃ البقرہ، آیت $ayahNumber)"
            }
            3 -> when (ayahNumber) {
                1 -> "الف، لام، میم۔"
                2 -> "اللہ کے سوا کوئی معبود نہیں، وہ ہمیشہ زندہ اور سب کو سنبھالنے والا ہے۔"
                3 -> "اس نے آپ پر حق کے ساتھ کتاب نازل فرمائی جو اپنے سے پہلی کتابوں کی تصدیق کرتی ہے۔"
                else -> "سورۃ آل عمران آیت نمبر $ayahNumber کا ترجمہ۔"
            }
            6 -> when (ayahNumber) {
                1 -> "تمام تعریفیں اللہ ہی کے لیے ہیں جس نے آسمانوں اور زمین کو پیدا کیا اور اندھیرے اور اجالا بنایا، پھر بھی کافر اپنے رب کے ساتھ دوسروں کو برابر ٹھہراتے ہیں۔"
                2 -> "وہی ہے جس نے تمہیں مٹی سے پیدا کیا پھر (زندگی کی) ایک مدت مقرر فرمائی، اور ایک مقررہ مدت اسی کے پاس ہے، پھر بھی تم شک کرتے ہو۔"
                3 -> "اور وہی اللہ ہے آسمانوں میں اور زمین میں، وہ تمہاری پوشیدہ اور ظاہر باتوں کو جانتا ہے۔"
                else -> "سورۃ الأنعام آیت نمبر $ayahNumber کا ترجمہ۔"
            }
            18 -> when (ayahNumber) {
                1 -> "سب تعریف اللہ کے لیے ہے جس نے اپنے بندے پر کتاب نازل کی اور اس میں کوئی کجی نہیں رکھی۔"
                2 -> "بلکہ بالکل سیدھی کتاب بنائی تاکہ اللہ کے سخت عذاب سے ڈرائے اور مومنوں کو خوشخبری دے۔"
                else -> "سورۃ الکہف آیت نمبر $ayahNumber کا ترجمہ۔"
            }
            36 -> when (ayahNumber) {
                1 -> "یا، سین۔"
                2 -> "حکمت سے بھرے قرآن کی قسم۔"
                3 -> "بیشک آپ ضرور رسولوں میں سے ہیں۔"
                else -> "سورۃ یٰسین آیت نمبر $ayahNumber کا ترجمہ۔"
            }
            55 -> when (ayahNumber) {
                1 -> "بڑا مہربان (اللہ)۔"
                2 -> "اسی نے قرآن سکھایا۔"
                3 -> "اسی نے انسان کو پیدا کیا۔"
                4 -> "اسے بولنا سکھایا۔"
                else -> "سورۃ الرحمن آیت نمبر $ayahNumber کا ترجمہ۔"
            }
            67 -> when (ayahNumber) {
                1 -> "بڑی برکت والی ہے وہ ذات جس کے ہاتھ میں بادشاہی ہے اور وہ ہر چیز پر قادر ہے۔"
                2 -> "جس نے موت اور زندگی کو پیدا کیا تاکہ تمہیں آزمائے کہ تم میں سے عمل کے لحاظ سے کون بہتر ہے۔"
                else -> "سورۃ الملک آیت نمبر $ayahNumber کا ترجمہ۔"
            }
            103 -> when (ayahNumber) {
                1 -> "زمانے کی قسم!"
                2 -> "بیشک تمام انسان خسارے میں ہیں۔"
                3 -> "سوائے ان کے جو ایمان لائے اور نیک عمل کیے اور ایک دوسرے کو حق اور صبر کی تلقین کی۔"
                else -> "سورۃ العصر آیت نمبر $ayahNumber"
            }
            108 -> when (ayahNumber) {
                1 -> "بیشک ہم نے آپ کو کوثر (خیر کثیر) عطا فرمائی۔"
                2 -> "پس آپ اپنے رب کے لیے نماز پڑھیے اور قربانی کیجیے۔"
                3 -> "بیشک آپ کا دشمن ہی بے نام و نشان رہے گا۔"
                else -> "سورۃ الکوثر آیت نمبر $ayahNumber"
            }
            112 -> when (ayahNumber) {
                1 -> "آپ فرما دیجیے کہ وہ اللہ ایک ہے۔"
                2 -> "اللہ بے نیاز اور سب کا سہارا ہے۔"
                3 -> "نہ اس سے کوئی پیدا ہوا اور نہ وہ کسی سے پیدا ہوا۔"
                4 -> "اور کوئی اس کا ہمسر نہیں ہے۔"
                else -> "سورۃ الإخلاص آیت نمبر $ayahNumber"
            }
            113 -> when (ayahNumber) {
                1 -> "آپ فرما دیجیے کہ میں صبح کے رب کی پناہ مانگتا ہوں۔"
                2 -> "ہر اس چیز کے شر سے جو اس نے پیدا کی۔"
                3 -> "اور اندھیری رات کے شر سے جب وہ چھا جائے۔"
                4 -> "اور گرہوں میں پھونکنے والیوں کے شر سے۔"
                5 -> "اور حسد کرنے والے کے شر سے جب وہ حسد کرے۔"
                else -> "سورۃ الفلق آیت نمبر $ayahNumber"
            }
            114 -> when (ayahNumber) {
                1 -> "آپ فرما دیجیے کہ میں انسانوں کے پروردگار کی پناہ مانگتا ہوں۔"
                2 -> "انسانوں کے بادشاہ کی۔"
                3 -> "انسانوں کے معبود برحق کی۔"
                4 -> "وسوسہ ڈالنے والے پیچھے ہٹ جانے والے کے شر سے۔"
                5 -> "جو لوگوں کے سینوں میں وسوسہ ڈالتا ہے۔"
                6 -> "خواہ وہ جنات میں سے ہو یا انسانوں میں سے۔"
                else -> "سورۃ الناس آیت نمبر $ayahNumber"
            }
            else -> "اللہ کے نام سے جو رحمن و رحیم ہے۔ (آیت $ayahNumber)"
        }
    }
}
