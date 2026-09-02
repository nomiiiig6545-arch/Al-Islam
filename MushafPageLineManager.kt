package com.example.data.mushaf

import com.example.data.db.AyahEntity
import com.example.data.db.SurahEntity

data class MushafLineItem(
    val lineIndex: Int, // 1 to 16
    val text: String,
    val isHeader: Boolean = false,
    val headerTitle: String = "",
    val isBismillah: Boolean = false
)

data class MushafPageContent(
    val pageInfo: MushafPageInfo,
    val lines: List<MushafLineItem>
)

object MushafPageLineManager {

    /**
     * Constructs a 16-line formatted representation of the requested Mushaf page.
     */
    fun build16LinePage(
        pageNumber: Int,
        ayahs: List<AyahEntity>
    ): MushafPageContent {
        val pageInfo = IndoPakMushafData.getPageInfo(pageNumber)

        if (ayahs.isEmpty()) {
            // Fallback placeholder lines while loading
            val emptyLines = (1..16).map { idx ->
                MushafLineItem(lineIndex = idx, text = "جَارِي تَحْمِيلِ الصَّفْحَةِ...")
            }
            return MushafPageContent(pageInfo, emptyLines)
        }

        // Group ayahs by Surah to handle multiple Surahs on a single page
        val ayahsBySurah = ayahs.groupBy { it.surahNumber }
        
        var totalHeaderLines = 0
        ayahsBySurah.forEach { (surahNum, surahAyahs) ->
            if (surahAyahs.any { it.numberInSurah == 1 }) {
                totalHeaderLines += 1 // Surah Name Banner
                if (surahNum != 9) { // Bismillah
                    totalHeaderLines += 1
                }
            }
        }

        val remainingTextLines = (16 - totalHeaderLines).coerceAtLeast(0)
        
        // Calculate word counts for proportional line distribution
        val surahWordCounts = ayahsBySurah.mapValues { (_, surahAyahs) ->
            val fullText = surahAyahs.joinToString(" ") { 
                "${it.arabicText} ۝${IndoPakMushafData.toArabicDigits(it.numberInSurah)}" 
            }.trim()
            fullText.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
        }
        val totalWords = surahWordCounts.values.sum()
        
        // Distribute the remaining text lines among the Surahs
        var textLinesAssigned = 0
        val surahTextLines = mutableMapOf<Int, Int>()
        val surahEntries = ayahsBySurah.entries.toList()

        for (i in surahEntries.indices) {
            val surahNum = surahEntries[i].key
            if (i == surahEntries.size - 1) {
                surahTextLines[surahNum] = remainingTextLines - textLinesAssigned
            } else {
                val count = surahWordCounts[surahNum] ?: 0
                val assigned = if (totalWords > 0) {
                    ((count.toDouble() / totalWords) * remainingTextLines).toInt().coerceAtLeast(if (count > 0) 1 else 0)
                } else 0
                surahTextLines[surahNum] = assigned
                textLinesAssigned += assigned
            }
        }

        val lineItems = mutableListOf<MushafLineItem>()
        var currentLineNum = 1

        for ((surahNum, surahAyahs) in ayahsBySurah) {
            val isStartOfSurah = surahAyahs.any { it.numberInSurah == 1 }
            if (isStartOfSurah) {
                val surahName = IndoPakMushafData.SURAH_NAMES_ARABIC.getOrElse(surahNum - 1) { pageInfo.surahNameArabic }
                lineItems.add(
                    MushafLineItem(
                        lineIndex = currentLineNum++,
                        text = "",
                        isHeader = true,
                        headerTitle = "سُورَةُ $surahName"
                    )
                )
                if (surahNum != 9) {
                    lineItems.add(
                        MushafLineItem(
                            lineIndex = currentLineNum++,
                            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                            isBismillah = true
                        )
                    )
                }
            }

            val fullText = surahAyahs.joinToString(" ") { ayah ->
                var text = ayah.arabicText
                if (ayah.numberInSurah == 1 && surahNum != 1 && surahNum != 9) {
                    // Remove Bismillah from the first ayah's text to prevent duplication, 
                    // since we just added it explicitly as a standalone line.
                    // Check both with and without trailing space
                    val prefixes = listOf(
                        "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ ",
                        "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                        "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ",
                        "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                        "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ",
                        "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"
                    )
                    
                    var stripped = false
                    for (prefix in prefixes) {
                        if (text.startsWith(prefix)) {
                            text = text.removePrefix(prefix).trimStart()
                            stripped = true
                            break
                        } else if (text.startsWith("\ufeff" + prefix)) {
                            text = text.removePrefix("\ufeff" + prefix).trimStart()
                            stripped = true
                            break
                        }
                    }
                    
                    // Fallback for slightly different diacritics: if not stripped but it's ayah 1, 
                    // we can regex replace the first 4 words if they match B-S-M Allah Al-Rahman Al-Raheem roughly.
                    if (!stripped) {
                        val bismillahRegex = Regex("^\\uFEFF?بِسْمِ.*?اللَّهِ.*?الرَّحْمَٰنِ.*?الرَّحِيمِ\\s*")
                        text = text.replaceFirst(bismillahRegex, "").trimStart()
                    }
                }
                "$text ۝${IndoPakMushafData.toArabicDigits(ayah.numberInSurah)}" 
            }.trim()
            val words = fullText.split("\\s+".toRegex()).filter { it.isNotBlank() }

            val linesForThisSurah = surahTextLines[surahNum] ?: 0
            if (linesForThisSurah > 0 && words.isNotEmpty()) {
                val wordsPerLine = (words.size.toDouble() / linesForThisSurah).toInt().coerceAtLeast(1)

                for (i in 0 until linesForThisSurah) {
                    val wordOffset = i * wordsPerLine
                    if (wordOffset < words.size) {
                        val endOffset = if (i == linesForThisSurah - 1) words.size else (wordOffset + wordsPerLine).coerceAtMost(words.size)
                        val lineText = words.subList(wordOffset, endOffset).joinToString(" ")
                        lineItems.add(
                            MushafLineItem(
                                lineIndex = currentLineNum++,
                                text = lineText
                            )
                        )
                    } else {
                        // Empty line to pad
                        lineItems.add(MushafLineItem(lineIndex = currentLineNum++, text = ""))
                    }
                }
            }
        }

        // Ensure exactly 16 lines (pad at the end if needed due to rounding or missing text)
        while (lineItems.size < 16) {
            lineItems.add(MushafLineItem(lineIndex = currentLineNum++, text = ""))
        }

        return MushafPageContent(pageInfo, lineItems.take(16))
    }
}
