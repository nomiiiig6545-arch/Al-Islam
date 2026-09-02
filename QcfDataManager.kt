package com.example.data.mushaf

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

data class QcfWord(
    val code: Int?,
    val char: String,
    val font: String?,
    val text: String?,
    val type: String, // "surah_header", "bismillah", "word", "end"
    val verseKey: String?,
    val position: Int?,
    val sura: Int?
)

data class QcfLine(
    val line: Int,
    val words: List<QcfWord>
)

data class QcfSurahHeader(
    val id: Int,
    val name: String,
    val nameArabic: String,
    val verseStart: Int,
    val verseEnd: Int
)

data class QcfPageData(
    val page: Int,
    val font: String?,
    val surahs: List<QcfSurahHeader>,
    val lines: List<QcfLine>
)

object QcfDataManager {
    private const val TAG = "QcfDataManager"
    private const val CDN_BASE_PAGES = "https://raw.githubusercontent.com/MohamadHajjRabee/quran-qcf4/main/pages"
    private const val CDN_BASE_FONTS = "https://raw.githubusercontent.com/MohamadHajjRabee/quran-qcf4/main/fonts"

    private val typefaceCache = ConcurrentHashMap<String, Typeface>()
    private val fontFamilyCache = ConcurrentHashMap<String, FontFamily>()
    private val pageDataCache = ConcurrentHashMap<Int, QcfPageData>()

    /**
     * Get or load FontFamily for a given QCF font name (e.g. "QCF4_Hafs_01", "QCF4_QBSML")
     */
    fun getFontFamily(context: Context, fontName: String?): FontFamily {
        if (fontName.isNullOrBlank()) return FontFamily.Default
        
        fontFamilyCache[fontName]?.let { return it }

        val tf = getTypeface(context, fontName)
        return if (tf != null) {
            val ff = FontFamily(tf)
            fontFamilyCache[fontName] = ff
            ff
        } else {
            FontFamily.Default
        }
    }

    /**
     * Get Typeface for a given font name, loading from assets or local disk cache
     */
    fun getTypeface(context: Context, fontName: String): Typeface? {
        typefaceCache[fontName]?.let { return it }

        val fontFileName = if (fontName.endsWith("_W") || fontName == "QCF4_QBSML") {
            "$fontName.ttf"
        } else {
            "${fontName}_W.ttf"
        }

        // 1. Try reading from APK assets
        try {
            val assetPath = "qcf/fonts/$fontFileName"
            val tf = Typeface.createFromAsset(context.assets, assetPath)
            typefaceCache[fontName] = tf
            return tf
        } catch (e: Exception) {
            // Not in assets, fallback to local storage
        }

        // 2. Try reading from app internal storage
        val fontsDir = File(context.filesDir, "qcf_fonts")
        val fontFile = File(fontsDir, fontFileName)
        if (fontFile.exists() && fontFile.length() > 0) {
            try {
                val tf = Typeface.createFromFile(fontFile)
                typefaceCache[fontName] = tf
                return tf
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load cached font: ${fontFile.absolutePath}", e)
            }
        }

        return null
    }

    /**
     * Ensure a font file is downloaded and ready in local cache
     */
    suspend fun ensureFontAvailable(context: Context, fontName: String?): Boolean = withContext(Dispatchers.IO) {
        if (fontName.isNullOrBlank()) return@withContext false
        if (getTypeface(context, fontName) != null) return@withContext true

        val fontFileName = if (fontName.endsWith("_W") || fontName == "QCF4_QBSML") {
            "$fontName.ttf"
        } else {
            "${fontName}_W.ttf"
        }

        try {
            val fontsDir = File(context.filesDir, "qcf_fonts").apply { mkdirs() }
            val fontFile = File(fontsDir, fontFileName)
            
            if (fontFile.exists()) {
                val tf = Typeface.createFromFile(fontFile)
                typefaceCache[fontName] = tf
                fontFamilyCache[fontName] = FontFamily(tf)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download font $fontName", e)
            false
        }
    }

    /**
     * Load QCF page data (from memory, assets, disk cache, or network)
     */
    suspend fun getPageData(context: Context, pageNumber: Int): QcfPageData? = withContext(Dispatchers.IO) {
        pageDataCache[pageNumber]?.let { return@withContext it }

        val padded = String.format("%03d", pageNumber)
        val jsonString: String? = try {
            // 1. Try reading from assets
            context.assets.open("qcf/pages/$padded.json").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            // 2. Try reading from disk cache
            val pagesDir = File(context.filesDir, "qcf_pages").apply { mkdirs() }
            val pageFile = File(pagesDir, "$padded.json")
            if (pageFile.exists()) {
                pageFile.readText()
            } else {
                // 3. Download from CDN
                try {
                    val url = "$CDN_BASE_PAGES/$padded.json"
                    val downloadedText = URL(url).readText()
                    pageFile.writeText(downloadedText)
                    downloadedText
                } catch (e2: Exception) {
                    Log.e(TAG, "Failed to fetch page data for page $pageNumber", e2)
                    null
                }
            }
        }

        if (jsonString.isNullOrBlank()) return@withContext null

        try {
            val root = JSONObject(jsonString)
            val page = root.optInt("page", pageNumber)
            val font = if (root.has("font")) root.optString("font") else null

            val surahsList = mutableListOf<QcfSurahHeader>()
            val surahsArray = root.optJSONArray("surahs")
            if (surahsArray != null) {
                for (i in 0 until surahsArray.length()) {
                    val sObj = surahsArray.getJSONObject(i)
                    surahsList.add(
                        QcfSurahHeader(
                            id = sObj.optInt("id"),
                            name = sObj.optString("name"),
                            nameArabic = sObj.optString("name_arabic"),
                            verseStart = sObj.optInt("verse_start"),
                            verseEnd = sObj.optInt("verse_end")
                        )
                    )
                }
            }

            val linesList = mutableListOf<QcfLine>()
            val linesArray = root.optJSONArray("lines")
            if (linesArray != null) {
                for (i in 0 until linesArray.length()) {
                    val lObj = linesArray.getJSONObject(i)
                    val lineNum = lObj.optInt("line", i + 1)
                    val wordsArray = lObj.optJSONArray("words")
                    val wordsList = mutableListOf<QcfWord>()

                    if (wordsArray != null) {
                        for (w in 0 until wordsArray.length()) {
                            val wObj = wordsArray.getJSONObject(w)
                            val code = if (wObj.has("code") && !wObj.isNull("code")) wObj.optInt("code") else null
                            val position = if (wObj.has("position") && !wObj.isNull("position")) wObj.optInt("position") else null
                            val sura = if (wObj.has("sura") && !wObj.isNull("sura")) wObj.optInt("sura") else null
                            val text = if (wObj.has("text") && !wObj.isNull("text")) wObj.optString("text") else null
                            val verseKey = if (wObj.has("verse_key") && !wObj.isNull("verse_key")) wObj.optString("verse_key") else null

                            wordsList.add(
                                QcfWord(
                                    code = code,
                                    char = wObj.optString("char", ""),
                                    font = wObj.optString("font", font),
                                    text = text,
                                    type = wObj.optString("type", "word"),
                                    verseKey = verseKey,
                                    position = position,
                                    sura = sura
                                )
                            )
                        }
                    }
                    linesList.add(QcfLine(line = lineNum, words = wordsList))
                }
            }

            val pageData = QcfPageData(
                page = page,
                font = font,
                surahs = surahsList,
                lines = linesList
            )

            // Pre-load required fonts for this page in background
            val fontsNeeded = mutableSetOf<String>()
            if (!font.isNullOrBlank()) fontsNeeded.add(font)
            fontsNeeded.add("QCF4_QBSML")
            linesList.forEach { line ->
                line.words.forEach { w ->
                    if (!w.font.isNullOrBlank()) fontsNeeded.add(w.font)
                }
            }

            fontsNeeded.forEach { fName ->
                ensureFontAvailable(context, fName)
            }

            pageDataCache[pageNumber] = pageData
            pageData
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse page data JSON for page $pageNumber", e)
            null
        }
    }
}
