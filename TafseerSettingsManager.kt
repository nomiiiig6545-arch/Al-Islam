package com.example.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Encapsulates the complete visual, typographical, and linguistic settings
 * for a specific Tafseer edition.
 */
data class TafseerSettings(
    val fontSizeSp: Float = 32f,
    val lineHeightMultiplier: Float = 1.2f,
    val translationLanguage: String = "URDU", // "URDU", "ENGLISH", "ARABIC", "HINDI"
    val isDarkMode: Boolean = false,
    val wordByWordEnabled: Boolean = false,
    val scriptStyle: String = "UTHMANI" // "UTHMANI"
)

/**
 * Manages independent, per-Tafseer persistent settings.
 * 
 * Each Tafseer (e.g. Tafseer Ibn Kaseer, Tafseer Al-Jalalayn, Tafseer Usmani)
 * maintains its own independent SharedPreferences file so that changing the
 * language, font size, script, or theme in one Tafseer does NOT affect the others.
 */
object TafseerSettingsManager {

    private const val PREF_PREFIX = "tafseer_prefs_"

    const val KEY_FONT_SIZE = "arabic_font_size"
    const val KEY_LINE_SPACING = "line_spacing"
    const val KEY_TRANSLATION_LANG = "translation_language"
    const val KEY_DARK_MODE = "is_dark_mode"
    const val KEY_WORD_BY_WORD = "word_by_word_enabled"
    const val KEY_SCRIPT_STYLE = "script_style"

    fun getPrefs(context: Context, tafseerId: String): SharedPreferences {
        return context.getSharedPreferences("${PREF_PREFIX}$tafseerId", Context.MODE_PRIVATE)
    }

    fun getSettings(context: Context, tafseerId: String): TafseerSettings {
        val prefs = getPrefs(context, tafseerId)
        return TafseerSettings(
            fontSizeSp = prefs.getFloat(KEY_FONT_SIZE, 32f),
            lineHeightMultiplier = prefs.getFloat(KEY_LINE_SPACING, 1.2f),
            translationLanguage = prefs.getString(KEY_TRANSLATION_LANG, "URDU") ?: "URDU",
            isDarkMode = prefs.getBoolean(KEY_DARK_MODE, false),
            wordByWordEnabled = prefs.getBoolean(KEY_WORD_BY_WORD, false),
            scriptStyle = prefs.getString(KEY_SCRIPT_STYLE, "UTHMANI") ?: "UTHMANI"
        )
    }

    fun setFontSize(context: Context, tafseerId: String, size: Float) {
        getPrefs(context, tafseerId).edit().putFloat(KEY_FONT_SIZE, size).apply()
    }

    fun setLineSpacing(context: Context, tafseerId: String, spacing: Float) {
        getPrefs(context, tafseerId).edit().putFloat(KEY_LINE_SPACING, spacing).apply()
    }

    fun setTranslationLanguage(context: Context, tafseerId: String, language: String) {
        getPrefs(context, tafseerId).edit().putString(KEY_TRANSLATION_LANG, language).apply()
    }

    fun setDarkMode(context: Context, tafseerId: String, isDark: Boolean) {
        getPrefs(context, tafseerId).edit().putBoolean(KEY_DARK_MODE, isDark).apply()
    }

    fun setWordByWordEnabled(context: Context, tafseerId: String, enabled: Boolean) {
        getPrefs(context, tafseerId).edit().putBoolean(KEY_WORD_BY_WORD, enabled).apply()
    }

    fun setScriptStyle(context: Context, tafseerId: String, style: String) {
        getPrefs(context, tafseerId).edit().putString(KEY_SCRIPT_STYLE, style).apply()
    }
}
