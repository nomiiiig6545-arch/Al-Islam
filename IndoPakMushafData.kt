package com.example.data.mushaf

import androidx.compose.ui.graphics.Color

data class PageOverlaySetting(
    val colorHex: String = "#FAF3DD",
    val intensity: Float = 0.5f,
    val isNightMode: Boolean = false
) {
    fun getBackgroundColor(): Color {
        if (isNightMode) return Color(0xFF1B1B1B)
        val baseColor = when (colorHex) {
            "#FAF3DD" -> Color(0xFFFAF3DD) // Cream
            "#F5E6CA" -> Color(0xFFF5E6CA) // Sepia
            "#E8F5E9" -> Color(0xFFE8F5E9) // Eye Comfort Green
            "#E0F7FA" -> Color(0xFFE0F7FA) // Soft Blue
            "#FFFFFF" -> Color(0xFFFFFFFF) // Pure White
            else -> Color(0xFFFAF3DD)
        }
        return baseColor
    }

    fun getTextColor(): Color {
        return if (isNightMode) Color(0xFFECEFF1) else Color(0xFF1A1A1A)
    }

    fun getHeaderColor(): Color {
        return if (isNightMode) Color(0xFFD4A343) else Color(0xFF1B5E20)
    }
}

data class MushafPageInfo(
    val pageNumber: Int,
    val surahNumber: Int,
    val surahNameArabic: String,
    val surahNameEnglish: String,
    val juzNumber: Int,
    val juzNameArabic: String,
    val juzNameEnglish: String = ""
)

object IndoPakMushafData {

    // Total pages in 16-Line Indo-Pak Mushaf Standard (ends at Surah An-Nas on page 549)
    const val TOTAL_PAGES = 549

    // 114 Surahs starting page in authentic 16-Line Indo-Pak Mushaf layout (549 Pages)
    val SURAH_START_PAGES = intArrayOf(
        1, 2, 46, 70, 97, 116, 137, 160, 169, 188,
        200, 212, 225, 231, 237, 242, 255, 265, 276, 282,
        291, 300, 309, 316, 325, 331, 340, 348, 358, 365,
        371, 375, 377, 386, 392, 397, 403, 409, 414, 422,
        430, 435, 441, 447, 450, 453, 457, 461, 465, 467,
        469, 472, 475, 477, 479, 482, 485, 489, 492, 494,
        498, 500, 501, 503, 505, 506, 509, 511, 513, 515,
        516, 519, 521, 522, 523, 525, 526, 529, 530, 531,
        533, 533, 533, 535, 536, 536, 538, 538, 539, 540,
        541, 541, 542, 542, 543, 543, 544, 544, 545, 545,
        545, 547, 547, 547, 547, 547, 547, 547, 548, 548,
        548, 548, 549, 549
    )

    // 30 Juz start pages in authentic 16-Line Indo-Pak Mushaf layout (549 Pages)
    val JUZ_START_PAGES = intArrayOf(
        1, 21, 39, 57, 75, 93, 111, 129, 147, 165,
        183, 201, 219, 237, 255, 273, 291, 309, 327, 345,
        363, 381, 399, 417, 435, 453, 471, 489, 509, 529
    )

    val JUZ_NAMES_ARABIC = arrayOf(
        "آلم ١", "سَيَقُولُ ٢", "تِلْكَ الرُّسُلُ ٣", "لَنْ تَنَالُوا ٤", "وَالْمُحْصَنَاتُ ٥",
        "لَا يُحِبُّ اللَّهُ ٦", "وَإِذَا سَمِعُوا ٧", "وَلَوْ أَنَّنَا ٨", "قَالَ الْمَلَأُ ٩", "وَاعْلَمُوا ١٠",
        "يَعْتَذِرُونَ ١١", "وَمَا مِنْ دَابَّةٍ ١٢", "وَمَا أُبَرِّئُ ١٣", "رُبَمَا ١٤", "سُبْحَانَ الَّذِي ١٥",
        "قَالَ أَلَمْ ١٦", "اقْتَرَبَ لِلنَّاسِ ١٧", "قَدْ أَفْلَحَ ١٨", "وَقَالَ الَّذِينَ ١٩", "أَمَّنْ خَلَقَ ٢٠",
        "اتْلُ مَا أُوحِيَ ٢١", "وَمَنْ يَقْنُتْ ٢٢", "وَما لِيَ ٢٣", "فَمَنْ أَظْلَمُ ٢٤", "إِلَيْهِ يُرَدُّ ٢٥",
        "حـم ٢٦", "قَالَ فَمَا خَطْبُكُمْ ٢٧", "قَدْ سَمِعَ اللَّهُ ٢٨", "تَبَارَكَ الَّذِي ٢٩", "عَمَّ يَتَسَاءَلُونَ ٣٠"
    )

    val JUZ_NAMES_ENGLISH = arrayOf(
        "Alif Lam Meem", "Sayaqool", "Tilkal Rusul", "Lan Tanaaloo", "Wal Muhsanat",
        "La Yuhibbullah", "Wa Iza Samiu", "Wa Lau Annana", "Qalal Mala'u", "Wa'lamoo",
        "Ya'taziroona", "Wa Mamin Da'abbah", "Wa Ma Ubri'oo", "Rubama", "Subhanallazi",
        "Qala Alam", "Iqtarraba Linnas", "Qad Aflaha", "Wa Qalallazina", "A'man Khalaq",
        "Utlu Ma Oohiya", "Wa Manyaqnut", "Wa Maliya", "Faman Azlamu", "Ilaihi Yuraddu",
        "Ha'a Meem", "Qala Fama Khatbukum", "Qad Sami Allah", "Tabarakallazi", "'Amma Yatasa'aloon"
    )

    val SURAH_NAMES_ARABIC = arrayOf(
        "الفاتحة", "البقرة", "آل عمران", "النساء", "المائدة", "الأنعام", "الأعراف", "الأنفال", "التوبة", "يونس",
        "هود", "يوسف", "الرعد", "إبراهيم", "الحجر", "النحل", "الإسراء", "الكهف", "مريم", "طه",
        "الأنبياء", "الحج", "المؤمنون", "النور", "الفرقان", "الشعراء", "النمل", "القصص", "العنكبوت", "الروم",
        "لقمان", "السجدة", "الأحزاب", "سبأ", "فاطر", "يس", "الصافات", "ص", "الزمر", "غافر",
        "فصلت", "الشورى", "الزخرف", "الدخان", "الجاثية", "الأحقاف", "محمد", "الفتح", "الحجرات", "ق",
        "الذاريات", "الطور", "النجم", "القمر", "الرحمن", "الواقعة", "الحديد", "المجادلة", "الحشر", "الممتحنة",
        "الصف", "الجمعة", "المنافقون", "التغابن", "الطلاق", "التحريم", "الملك", "القلم", "الحاقة", "المعارج",
        "نوح", "الجن", "المزمل", "المدثر", "القيامة", "الإنسان", "المرسلات", "النبأ", "النازعات", "عبس",
        "التكوير", "الانفطار", "المطففين", "الانشقاق", "البروج", "الطارق", "الأعلى", "الغاشية", "الفجر", "البلد",
        "الشمس", "الليل", "الضحى", "الشرح", "التين", "العلق", "القدر", "البينة", "الزلزلة", "العاديات",
        "القارعة", "التكاثر", "العصر", "الهمزة", "الفيل", "قريش", "الماعون", "الكوثر", "الكافرون", "النصر",
        "المسد", "الإخلاص", "الفلق", "الناس"
    )

    val SURAH_NAMES_ENGLISH = arrayOf(
        "Al-Fatiha", "Al-Baqarah", "Ali 'Imran", "An-Nisa", "Al-Ma'idah", "Al-An'am", "Al-A'raf", "Al-Anfal", "At-Tawbah", "Yunus",
        "Hud", "Yusuf", "Ar-Ra'd", "Ibrahim", "Al-Hijr", "An-Nahl", "Al-Isra", "Al-Kahf", "Maryam", "Taha",
        "Al-Anbiya", "Al-Hajj", "Al-Mu'minun", "An-Nur", "Al-Furqan", "Ash-Shu'ara", "An-Naml", "Al-Qasas", "Al-'Ankabut", "Ar-Rum",
        "Luqman", "As-Sajdah", "Al-Ahzab", "Saba", "Fatir", "Ya-Sin", "As-Saffat", "Sad", "Az-Zumar", "Ghafir",
        "Fussilat", "Ash-Shuraa", "Az-Zukhruf", "Ad-Dukhan", "Al-Jathiyah", "Al-Ahqaf", "Muhammad", "Al-Fath", "Al-Hujurat", "Qaf",
        "Adh-Dhariyat", "At-Tur", "An-Najm", "Al-Qamar", "Ar-Rahman", "Al-Waqi'ah", "Al-Hadid", "Al-Mujadila", "Al-Hashr", "Al-Mumtahanah",
        "As-Saff", "Al-Jumu'ah", "Al-Munafiqun", "At-Taghabun", "At-Talaq", "At-Tahrim", "Al-Mulk", "Al-Qalam", "Al-Haqqah", "Al-Ma'arij",
        "Nuh", "Al-Jinn", "Al-Muzzammil", "Al-Muddaththir", "Al-Qiyamah", "Al-Insan", "Al-Mursalat", "An-Naba", "An-Nazi'at", "'Abasa",
        "At-Takwir", "Al-Infitar", "Al-Mutaffifin", "Al-Inshiqaq", "Al-Buruj", "At-Tariq", "Al-A'la", "Al-Ghashiyah", "Al-Fajr", "Al-Balad",
        "Ash-Shams", "Al-Lail", "Ad-Duhaa", "Ash-Sharh", "At-Tin", "Al-'Alaq", "Al-Qadr", "Al-Bayyinah", "Az-Zalzalah", "Al-'Adiyat",
        "Al-Qari'ah", "At-Takathur", "Al-'Asr", "Al-Humazah", "Al-Fil", "Quraysh", "Al-Ma'un", "Al-Kawthar", "Al-Kafirun", "An-Nasr",
        "Al-Masad", "Al-Ikhlas", "Al-Falaq", "An-Nas"
    )

    val SURAH_AYAH_COUNTS = intArrayOf(
        7, 286, 200, 176, 120, 165, 206, 75, 129, 109,
        123, 111, 43, 52, 99, 128, 111, 110, 98, 135,
        112, 78, 118, 64, 77, 227, 93, 88, 69, 60,
        34, 30, 73, 54, 45, 83, 182, 88, 75, 85,
        54, 53, 89, 59, 37, 35, 38, 29, 18, 45,
        60, 49, 62, 55, 78, 96, 29, 22, 24, 13,
        14, 11, 11, 18, 12, 12, 30, 52, 52, 44,
        28, 28, 20, 56, 40, 31, 50, 40, 46, 42,
        29, 19, 36, 25, 22, 17, 19, 26, 30, 20,
        15, 21, 11, 8, 8, 19, 5, 8, 8, 11,
        11, 8, 3, 9, 5, 4, 7, 3, 6, 3,
        5, 4, 5, 6
    )

    fun getPageForSurah(surahNumber: Int): Int {
        val clamped = surahNumber.coerceIn(1, 114)
        return SURAH_START_PAGES[clamped - 1]
    }

    fun getPageInfo(pageNumber: Int): MushafPageInfo {
        val page = pageNumber.coerceIn(1, TOTAL_PAGES)

        // Find Surah
        var surahIdx = 0
        for (i in SURAH_START_PAGES.indices) {
            if (page >= SURAH_START_PAGES[i]) {
                surahIdx = i
            } else {
                break
            }
        }

        // Find Juz
        var juzIdx = 0
        for (j in JUZ_START_PAGES.indices) {
            if (page >= JUZ_START_PAGES[j]) {
                juzIdx = j
            } else {
                break
            }
        }

        return MushafPageInfo(
            pageNumber = page,
            surahNumber = surahIdx + 1,
            surahNameArabic = SURAH_NAMES_ARABIC[surahIdx],
            surahNameEnglish = SURAH_NAMES_ENGLISH[surahIdx],
            juzNumber = juzIdx + 1,
            juzNameArabic = JUZ_NAMES_ARABIC[juzIdx],
            juzNameEnglish = JUZ_NAMES_ENGLISH.getOrElse(juzIdx) { "Juz ${juzIdx + 1}" }
        )
    }

    fun toArabicDigits(number: Int): String {
        val latin = number.toString()
        val arabicDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val sb = StringBuilder()
        for (ch in latin) {
            if (ch in '0'..'9') {
                sb.append(arabicDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Resolves 16-line Colour-Coded Tajweed Quran page sources with strict offline priority:
     * 1. App internal storage (downloaded in mushaf_tajweed_pages)
     * 2. Authentic 16-Line Colour Coded Tajweed Mushaf Mirrors (CDN & GitHub raw)
     */
    fun get16LinePageImageUrls(context: android.content.Context?, pageNumber: Int): List<String> {
        val clampedPage = pageNumber.coerceIn(1, TOTAL_PAGES)
        val padded3 = "%03d".format(clampedPage)

        val list = mutableListOf<String>()

        if (context != null) {
            val localDir = java.io.File(context.filesDir, "mushaf_tajweed_pages")
            val localExtensions = listOf("jpg", "webp", "png")
            for (ext in localExtensions) {
                val f1 = java.io.File(localDir, "page_$padded3.$ext")
                if (f1.exists() && f1.length() > 1000) {
                    list.add("file://${f1.absolutePath}")
                }
                val f2 = java.io.File(localDir, "page_$clampedPage.$ext")
                if (f2.exists() && f2.length() > 1000) {
                    list.add("file://${f2.absolutePath}")
                }
            }
        }

        // 30 Juz pages lengths in 16-line standard mushaf (549 total pages)
        val juzLengths = intArrayOf(
            20, 18, 18, 18, 18, 18, 18, 18, 18, 18,
            18, 18, 18, 18, 18, 18, 18, 18, 18, 18,
            18, 18, 18, 18, 18, 18, 18, 20, 20, 21
        )

        var accumulated = 0
        var selectedJuz = 1
        var pageInJuz = 1

        for (j in juzLengths.indices) {
            val count = juzLengths[j]
            if (clampedPage <= accumulated + count) {
                selectedJuz = j + 1
                pageInJuz = clampedPage - accumulated
                break
            }
            accumulated += count
        }

        if (clampedPage >= 549) {
            selectedJuz = 30
            pageInJuz = 21
        }

        val formattedJuz = "%02d".format(selectedJuz)

        // Exclusively 16-Line Colour Coded Tajweed Mushaf Mirrors
        list.add("https://raw.githubusercontent.com/Sachal2508/AL-Quran-App-Quran-Images/main/quran_16line/Colour%20Coded%20Quran%20Juz%20$formattedJuz/page_$pageInJuz.jpg")
        list.add("https://cdn.jsdelivr.net/gh/Sachal2508/AL-Quran-App-Quran-Images@main/quran_16line/Colour%20Coded%20Quran%20Juz%20$formattedJuz/page_$pageInJuz.jpg")
        list.add("https://raw.githack.com/Sachal2508/AL-Quran-App-Quran-Images/main/quran_16line/Colour%20Coded%20Quran%20Juz%20$formattedJuz/page_$pageInJuz.jpg")
        list.add("https://fastly.jsdelivr.net/gh/Sachal2508/AL-Quran-App-Quran-Images@main/quran_16line/Colour%20Coded%20Quran%20Juz%20$formattedJuz/page_$pageInJuz.jpg")

        return list.distinct()
    }
}
