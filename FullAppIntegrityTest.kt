package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.example.data.TafseerDataStore
import com.example.data.JawahirTafseerProvider
import com.example.data.mushaf.OfflineQuranDataProvider
import com.example.data.mushaf.IndoPakMushafData

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FullAppIntegrityTest {

    @Test
    fun testAllExistingJawahirProvidersAccessible() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        OfflineQuranDataProvider.init(context)
        
        // All 114 Surahs with Jawahir tafseer loaded
        val testedSurahs = (1..114).toList()
        for (surahNum in testedSurahs) {
            val tafseerAyah1 = JawahirTafseerProvider.getExactText(surahNum, 1)
            assertNotNull("Surah $surahNum ayah 1 tafseer should not be null", tafseerAyah1)
            assertTrue("Surah $surahNum tafseer should not be empty", tafseerAyah1!!.isNotBlank())
        }
    }

    @Test
    fun testTafseerDataStoreMultiScholarSupport() {
        val scholars = listOf("jawahir", "ibn_kaseer", "jalalayn", "usmani", "mazhari")
        for (scholar in scholars) {
            val ayah = TafseerDataStore.getAyahDetails(1, 1, scholar, "URDU")
            assertNotNull("Ayah details for $scholar should exist", ayah)
            assertTrue("Ayah arabic text should exist", ayah.arabicText.isNotBlank())
            assertTrue("Ayah translation should exist", ayah.urduTranslation.isNotBlank())
            assertTrue("Ayah tafseer should exist", ayah.tafseerParagraphs.isNotEmpty())
        }
    }

    @Test
    fun testMushafPageMapping() {
        assertEquals(1, IndoPakMushafData.getPageForSurah(1))
        assertEquals(2, IndoPakMushafData.getPageForSurah(2))
        assertEquals(549, IndoPakMushafData.getPageForSurah(114))
    }

    @Test
    fun testWordByWordParser() {
        val words = com.example.ui.components.WordByWordColorizer.getWordsForAyah(
            arabicText = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
            language = "URDU"
        )
        assertTrue(words.isNotEmpty())
    }
    @Test
    fun testTafseerMazhariSurahFatihaDetailedText() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.data.mushaf.OfflineQuranDataProvider.init(context)
        
        for (ayahNum in 1..7) {
            val mazhariAyah = TafseerDataStore.getAyahDetails(1, ayahNum, "mazhari", "URDU")
            assertNotNull("Ayah " + ayahNum + " details for Tafseer Mazhari should exist", mazhariAyah)
            assertTrue("Ayah " + ayahNum + " paragraphs should not be empty", mazhariAyah.tafseerParagraphs.isNotEmpty())
            assertTrue("Ayah " + ayahNum + " should contain rich authentic text", 
                mazhariAyah.tafseerParagraphs.any { it.contains("قاضی ثناء اللہ پانی پتی") || it.contains("تسمیہ") || it.contains("الحمد") || it.contains("رحمت") || it.contains("مالک") || it.contains("عبادت") || it.contains("ہدایت") || it.contains("انعام") }
            )
        }
    }

    @Test
    fun testTafseerMazhariSurahBaqarahCompleteText() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.data.mushaf.OfflineQuranDataProvider.init(context)
        
        // Test key verses in Surah Baqarah
        val testAyahs = listOf(1, 2, 5, 30, 142, 177, 183, 255, 282, 286)
        for (ayahNum in testAyahs) {
            val mazhariAyah = TafseerDataStore.getAyahDetails(2, ayahNum, "mazhari", "URDU")
            assertNotNull("Ayah $ayahNum details for Tafseer Mazhari in Surah Baqarah should exist", mazhariAyah)
            assertTrue("Ayah $ayahNum paragraphs should not be empty", mazhariAyah.tafseerParagraphs.isNotEmpty())
            assertTrue("Ayah $ayahNum should contain scholarly Mazhari text", 
                mazhariAyah.tafseerParagraphs.any { it.contains("قاضی ثناء اللہ پانی پتی") || it.contains("پانی پتی") || it.contains("تفسیر") || it.contains("آیۃ الکرسی") }
            )
        }
    }

    @Test
    fun testTafseerMazhariAllSurahsCompleteCoverage() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.data.mushaf.OfflineQuranDataProvider.init(context)

        var totalAyahsChecked = 0
        for (surahNum in 1..114) {
            val totalVerses = com.example.data.mushaf.IndoPakMushafData.SURAH_AYAH_COUNTS[surahNum - 1]
            assertTrue("Surah $surahNum must have verses > 0", totalVerses > 0)
            
            for (ayahNum in 1..totalVerses) {
                val mazhariAyah = TafseerDataStore.getAyahDetails(surahNum, ayahNum, "mazhari", "URDU")
                assertNotNull("Surah $surahNum Ayah $ayahNum details for Tafseer Mazhari must exist", mazhariAyah)
                assertTrue("Surah $surahNum Ayah $ayahNum tafseer paragraphs must not be empty", mazhariAyah.tafseerParagraphs.isNotEmpty())
                assertTrue("Surah $surahNum Ayah $ayahNum Arabic text must not be empty", mazhariAyah.arabicText.isNotBlank())
                assertTrue("Surah $surahNum Ayah $ayahNum Urdu translation must not be empty", mazhariAyah.urduTranslation.isNotBlank())
                totalAyahsChecked++
            }
        }
        assertEquals("All 6,236 Ayahs of Quran must be covered in Tafseer Mazhari", 6236, totalAyahsChecked)
    }
}
