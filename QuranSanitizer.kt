package com.example.util

/**
 * Utility for formatting and sanitizing Quranic texts.
 * Ensures that "Bismillah ir-Rahman ir-Rahim" is NOT merged or embedded into
 * Ayah 1 of any Surah (except Surah 1 Al-Fatiha where it is verse 1),
 * allowing Bismillah to be presented separately as a dedicated header.
 */
object QuranSanitizer {

    const val BISMILLAH_ARABIC = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ"
    const val BISMILLAH_URDU = "شروع اللہ کے نام سے جو بڑا مہربان نہایت رحم والا ہے۔"
    const val BISMILLAH_ENGLISH = "In the name of Allah, the Entirely Merciful, the Especially Merciful."

    private val BISMILLAH_ARABIC_REGEX = Regex(
        "^([\\s\\n]*(?:بِسْمِ|بِسمِ)\\s+(?:ٱللَّهِ|ٱللَّهِ|اللَّهِ|اللَّهِ|اللهِ|اللّٰهِ)\\s+(?:ٱلرَّحْمَـٰنِ|ٱلرَّحْمَٰنِ|الرَّحْمَٰنِ|الرَّحْمٰنِ|الرَّحْمَنِ|الرَّحْمٰنِ|الرَّحْمَنِ)\\s+(?:ٱلرَّحِيمِ|ٱلرَّحِيمِ|الرَّحِيمِ|الرَّحِيْمِ|الرَّحِيْمِ)[\\s\\n۝]*)+",
        RegexOption.IGNORE_CASE
    )

    private val BISMILLAH_URDU_REGEX = Regex(
        "^([\\s\\n]*(?:شروع\\s+اللہ\\s+(?:کے\\s+نام\\s+سے|کا\\s+نام\\s+لے\\s+کر)\\s+جو\\s+بڑا\\s+مہربان\\s+نہایت\\s+رحم\\s+والا\\s+ہے۔?|اللہ\\s+کے\\s+نام\\s+سے\\s+جو\\s+رحمان\\s+اور\\s+رحیم\\s+ہے۔?)[\\s\\n]*)",
        RegexOption.IGNORE_CASE
    )

    /**
     * Cleans leading Bismillah from Arabic Ayah text if it's not Surah Al-Fatiha (Surah 1).
     */
    fun cleanAyahArabic(text: String, surahNumber: Int, ayahNumber: Int): String {
        if (surahNumber == 1) {
            return text.trim()
        }
        if (ayahNumber == 1) {
            val stripped = BISMILLAH_ARABIC_REGEX.replace(text.trim(), "").trim()
            if (stripped.isNotBlank()) {
                return stripped
            }
        }
        return text.trim()
    }

    /**
     * Cleans leading Bismillah from Urdu translation if it's not Surah Al-Fatiha.
     */
    fun cleanAyahUrdu(text: String, surahNumber: Int, ayahNumber: Int): String {
        if (surahNumber == 1) {
            return text.trim()
        }
        if (ayahNumber == 1) {
            val stripped = BISMILLAH_URDU_REGEX.replace(text.trim(), "").trim()
            if (stripped.isNotBlank()) {
                return stripped
            }
        }
        return text.trim()
    }
}
