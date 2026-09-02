package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Al-Quran Majeed", appName)
  }

  @Test
  fun `test word by word accuracy for yashuroon`() {
    val words = com.example.ui.components.WordByWordColorizer.getWordsForAyah(
        arabicText = "وَمَا يَشْعُرُونَ",
        language = "URDU"
    )
    val yashuroonWord = words.find { it.arabic.contains("يَشْعُرُونَ") || it.arabic.contains("يشعرون") }
    org.junit.Assert.assertNotNull(yashuroonWord)
    org.junit.Assert.assertTrue(
        yashuroonWord?.urdu?.contains("شعور") == true || yashuroonWord?.urdu?.contains("سمجھتے") == true
    )
  }

  @Test
  fun `test independent settings per tafseer`() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    // Configure Ibn Kaseer settings
    com.example.data.TafseerSettingsManager.setTranslationLanguage(context, "ibn_kaseer", "ENGLISH")
    com.example.data.TafseerSettingsManager.setFontSize(context, "ibn_kaseer", 34f)
    com.example.data.TafseerSettingsManager.setDarkMode(context, "ibn_kaseer", true)
    com.example.data.TafseerSettingsManager.setScriptStyle(context, "ibn_kaseer", "UTHMANI")

    // Configure Al-Jalalayn settings
    com.example.data.TafseerSettingsManager.setTranslationLanguage(context, "jalalayn", "ARABIC")
    com.example.data.TafseerSettingsManager.setFontSize(context, "jalalayn", 24f)
    com.example.data.TafseerSettingsManager.setDarkMode(context, "jalalayn", false)

    // Verify Ibn Kaseer settings
    val ibnKaseerSettings = com.example.data.TafseerSettingsManager.getSettings(context, "ibn_kaseer")
    assertEquals("ENGLISH", ibnKaseerSettings.translationLanguage)
    assertEquals(34f, ibnKaseerSettings.fontSizeSp, 0.01f)
    assertEquals(true, ibnKaseerSettings.isDarkMode)
    assertEquals("UTHMANI", ibnKaseerSettings.scriptStyle)

    // Verify Al-Jalalayn settings
    val jalalaynSettings = com.example.data.TafseerSettingsManager.getSettings(context, "jalalayn")
    assertEquals("ARABIC", jalalaynSettings.translationLanguage)
    assertEquals(24f, jalalaynSettings.fontSizeSp, 0.01f)
    assertEquals(false, jalalaynSettings.isDarkMode)
    assertEquals("UTHMANI", jalalaynSettings.scriptStyle)

    // Verify Tafseer Usmani maintains untouched default settings
    val usmaniSettings = com.example.data.TafseerSettingsManager.getSettings(context, "usmani")
    assertEquals("URDU", usmaniSettings.translationLanguage)
    assertEquals(28f, usmaniSettings.fontSizeSp, 0.01f)
    assertEquals(false, usmaniSettings.isDarkMode)
    assertEquals("UTHMANI", usmaniSettings.scriptStyle)
  }

  @Test
  fun `test default language is always URDU on fresh install for all tafseers`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val tafseerIds = listOf("ibn_kaseer_fresh", "jalalayn_fresh", "usmani_fresh")
    for (id in tafseerIds) {
      val settings = com.example.data.TafseerSettingsManager.getSettings(context, id)
      assertEquals("URDU", settings.translationLanguage)
      assertEquals("UTHMANI", settings.scriptStyle)
      assertEquals(false, settings.isDarkMode)
      assertEquals(false, settings.wordByWordEnabled)
    }
  }

  @Test
  fun `test unique tafseer details and word-by-word consistency across tafseers`() {
    val ayah1IbnKaseer = com.example.data.TafseerDataStore.getAyahDetails(2, 6, "ibn_kaseer", "URDU")
    val ayah1Jalalayn = com.example.data.TafseerDataStore.getAyahDetails(2, 6, "jalalayn", "URDU")
    val ayah1Usmani = com.example.data.TafseerDataStore.getAyahDetails(2, 6, "usmani", "URDU")

    // Full translations and words are consistent
    assertEquals(ayah1IbnKaseer.arabicText, ayah1Jalalayn.arabicText)
    assertEquals(ayah1IbnKaseer.urduTranslation, ayah1Usmani.urduTranslation)
    org.junit.Assert.assertTrue(ayah1IbnKaseer.words.isNotEmpty())
    assertEquals(ayah1IbnKaseer.words.size, ayah1Jalalayn.words.size)

    // Scholarly commentaries are distinct
    org.junit.Assert.assertNotEquals(ayah1IbnKaseer.tafseerParagraphs[0], ayah1Jalalayn.tafseerParagraphs[0])
    org.junit.Assert.assertNotEquals(ayah1IbnKaseer.tafseerParagraphs[0], ayah1Usmani.tafseerParagraphs[0])

    // Jalalayn has 3 comprehensive paragraphs
    assertEquals(3, ayah1Jalalayn.tafseerParagraphs.size)
    assertEquals(3, ayah1IbnKaseer.tafseerParagraphs.size)
    assertEquals(3, ayah1Usmani.tafseerParagraphs.size)
  }
}

