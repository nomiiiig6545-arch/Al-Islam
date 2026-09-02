package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.data.model.AudioFavorite
import com.example.data.model.PlayerThemeId
import org.json.JSONArray
import org.json.JSONObject

data class RecentSurah(
    val reciterId: String,
    val surahNumber: Int,
    val timestamp: Long
)

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(context: Context) {
    private val appContext = context.applicationContext

    companion object {
        val AUDIO_MODE = androidx.datastore.preferences.core.stringPreferencesKey("audio_mode")
        val THEME_PREFERENCE = androidx.datastore.preferences.core.intPreferencesKey("theme_preference")
        val READING_MODE = androidx.datastore.preferences.core.stringPreferencesKey("reading_mode")
        val QURAN_SCRIPT = androidx.datastore.preferences.core.stringPreferencesKey("quran_script")
        val SELECTED_RECITER = androidx.datastore.preferences.core.stringPreferencesKey("selected_reciter")
        val LAST_READ_MUSHAF_PAGE = androidx.datastore.preferences.core.intPreferencesKey("last_read_mushaf_page")
        val BRIGHTNESS_LEVEL = androidx.datastore.preferences.core.floatPreferencesKey("brightness_level")
        val KEEP_SCREEN_ON = androidx.datastore.preferences.core.booleanPreferencesKey("keep_screen_on")
        val AUTO_NEXT_SURAH = androidx.datastore.preferences.core.booleanPreferencesKey("auto_next_surah")
        val NEXT_SURAH_TRANSITION_MODE = androidx.datastore.preferences.core.stringPreferencesKey("next_surah_transition_mode")
        val NEXT_SURAH_DELAY_SECONDS = androidx.datastore.preferences.core.intPreferencesKey("next_surah_delay_seconds")
        val SKIP_SILENCE_INTERVAL = androidx.datastore.preferences.core.floatPreferencesKey("skip_silence_interval")
        val SKIP_INTRO_SILENCE = androidx.datastore.preferences.core.booleanPreferencesKey("skip_intro_silence")
        val SEEK_INTERVAL_SECONDS = androidx.datastore.preferences.core.intPreferencesKey("seek_interval_seconds")
        val AUDIO_STREAM_QUALITY = androidx.datastore.preferences.core.stringPreferencesKey("audio_stream_quality")
        val RECENTLY_PLAYED = androidx.datastore.preferences.core.stringPreferencesKey("recently_played_surah_reciter_v2")
        val PLAYER_THEME_ID = androidx.datastore.preferences.core.stringPreferencesKey("player_theme_id_v2")
        val AUDIO_FAVORITES = androidx.datastore.preferences.core.stringPreferencesKey("audio_favorites_list_v2")
    }

    val audioStreamQuality: Flow<String> = appContext.dataStore.data
        .map { preferences ->
            preferences[AUDIO_STREAM_QUALITY] ?: "ULTRA_LOW" // "ULTRA_LOW" (32kbps / micro-byte stream), "STANDARD" (64kbps), "HIGH" (128kbps)
        }

    suspend fun setAudioStreamQuality(quality: String) {
        appContext.dataStore.edit { preferences ->
            preferences[AUDIO_STREAM_QUALITY] = quality
        }
    }

    val autoNextSurah: Flow<Boolean> = appContext.dataStore.data
        .map { preferences ->
            preferences[AUTO_NEXT_SURAH] ?: true
        }

    val nextSurahTransitionMode: Flow<String> = appContext.dataStore.data
        .map { preferences ->
            preferences[NEXT_SURAH_TRANSITION_MODE] ?: "CONTINUOUS" // "CONTINUOUS", "REPEAT_ONE", "STOP", "COUNTDOWN"
        }

    val nextSurahDelaySeconds: Flow<Int> = appContext.dataStore.data
        .map { preferences ->
            preferences[NEXT_SURAH_DELAY_SECONDS] ?: 0 // 0s, 3s, 5s, 10s
        }

    val skipSilenceInterval: Flow<Float> = appContext.dataStore.data
        .map { preferences ->
            preferences[SKIP_SILENCE_INTERVAL] ?: 0.5f // 0.0f (Off), 0.5s, 1.0s, 2.0s, 3.0s
        }

    val skipIntroSilence: Flow<Boolean> = appContext.dataStore.data
        .map { preferences ->
            preferences[SKIP_INTRO_SILENCE] ?: false
        }

    val seekIntervalSeconds: Flow<Int> = appContext.dataStore.data
        .map { preferences ->
            preferences[SEEK_INTERVAL_SECONDS] ?: 10 // 5s, 10s, 15s, 30s
        }

    suspend fun setAutoNextSurah(enabled: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[AUTO_NEXT_SURAH] = enabled
        }
    }

    suspend fun setNextSurahTransitionMode(mode: String) {
        appContext.dataStore.edit { preferences ->
            preferences[NEXT_SURAH_TRANSITION_MODE] = mode
        }
    }

    suspend fun setNextSurahDelaySeconds(seconds: Int) {
        appContext.dataStore.edit { preferences ->
            preferences[NEXT_SURAH_DELAY_SECONDS] = seconds
        }
    }

    suspend fun setSkipSilenceInterval(seconds: Float) {
        appContext.dataStore.edit { preferences ->
            preferences[SKIP_SILENCE_INTERVAL] = seconds
        }
    }

    suspend fun setSkipIntroSilence(enabled: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[SKIP_INTRO_SILENCE] = enabled
        }
    }

    suspend fun setSeekIntervalSeconds(seconds: Int) {
        appContext.dataStore.edit { preferences ->
            preferences[SEEK_INTERVAL_SECONDS] = seconds
        }
    }

    val brightnessLevel: Flow<Float> = appContext.dataStore.data
        .map { preferences ->
            preferences[BRIGHTNESS_LEVEL] ?: -1f // -1f means System / Auto Brightness
        }

    val keepScreenOn: Flow<Boolean> = appContext.dataStore.data
        .map { preferences ->
            preferences[KEEP_SCREEN_ON] ?: false
        }

    suspend fun setBrightnessLevel(level: Float) {
        appContext.dataStore.edit { preferences ->
            preferences[BRIGHTNESS_LEVEL] = level
        }
    }

    suspend fun setKeepScreenOn(keepOn: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[KEEP_SCREEN_ON] = keepOn
        }
    }

    val lastReadMushafPage: Flow<Int> = appContext.dataStore.data
        .map { preferences ->
            preferences[LAST_READ_MUSHAF_PAGE] ?: 1
        }

    suspend fun setLastReadMushafPage(pageNumber: Int) {
        appContext.dataStore.edit { preferences ->
            preferences[LAST_READ_MUSHAF_PAGE] = pageNumber
        }
    }

    val selectedReciterId: Flow<String> = appContext.dataStore.data
        .map { preferences ->
            preferences[SELECTED_RECITER] ?: "ar.alajamy" // Default: Ahmed Ibn Ali Al-Ajmy
        }

    suspend fun setSelectedReciter(reciterId: String) {
        appContext.dataStore.edit { preferences ->
            preferences[SELECTED_RECITER] = reciterId
        }
    }

    val audioMode: Flow<String> = appContext.dataStore.data
        .map { preferences ->
            preferences[AUDIO_MODE] ?: "BOTH" // Default: "BOTH" (Arabic + Urdu)
        }

    val themePreference: Flow<Int> = appContext.dataStore.data
        .map { preferences ->
            preferences[THEME_PREFERENCE] ?: 1 // Default: 1 (Simple Light)
        }

    val readingMode: Flow<String> = appContext.dataStore.data
        .map { preferences ->
            preferences[READING_MODE] ?: "PARALLEL" // Default: "PARALLEL" ("PARALLEL", "ARABIC_ONLY", "URDU_ONLY")
        }

    val quranScript: Flow<String> = appContext.dataStore.data
        .map { preferences ->
            preferences[QURAN_SCRIPT] ?: "INDO_PAK" // "USMANI" or "INDO_PAK"
        }

    suspend fun setQuranScript(script: String) {
        appContext.dataStore.edit { preferences ->
            preferences[QURAN_SCRIPT] = script
        }
    }

    suspend fun setAudioMode(mode: String) {
        appContext.dataStore.edit { preferences ->
            preferences[AUDIO_MODE] = mode
        }
    }

    suspend fun setThemePreference(theme: Int) {
        appContext.dataStore.edit { preferences ->
            preferences[THEME_PREFERENCE] = theme
        }
    }

    suspend fun setReadingMode(mode: String) {
        appContext.dataStore.edit { preferences ->
            preferences[READING_MODE] = mode
        }
    }

    val recentlyPlayed: Flow<List<RecentSurah>> = appContext.dataStore.data
        .map { preferences ->
            val raw = preferences[RECENTLY_PLAYED] ?: ""
            if (raw.isBlank()) {
                emptyList()
            } else {
                raw.split(",")
                    .mapNotNull { item ->
                        val parts = item.split(":")
                        if (parts.size >= 3) {
                            RecentSurah(
                                reciterId = parts[0],
                                surahNumber = parts[1].toIntOrNull() ?: return@mapNotNull null,
                                timestamp = parts[2].toLongOrNull() ?: 0L
                            )
                        } else if (parts.size == 2) {
                            RecentSurah(
                                reciterId = parts[0],
                                surahNumber = parts[1].toIntOrNull() ?: return@mapNotNull null,
                                timestamp = System.currentTimeMillis()
                            )
                        } else {
                            null
                        }
                    }
                    .sortedByDescending { it.timestamp }
                    .take(5)
            }
        }

    suspend fun addRecentlyPlayed(reciterId: String, surahNumber: Int) {
        appContext.dataStore.edit { preferences ->
            val raw = preferences[RECENTLY_PLAYED] ?: ""
            val list = if (raw.isBlank()) {
                mutableListOf()
            } else {
                raw.split(",")
                    .mapNotNull { item ->
                        val parts = item.split(":")
                        if (parts.size >= 2) {
                            val rId = parts[0]
                            val sNum = parts[1].toIntOrNull() ?: return@mapNotNull null
                            val ts = parts.getOrNull(2)?.toLongOrNull() ?: System.currentTimeMillis()
                            RecentSurah(rId, sNum, ts)
                        } else {
                            null
                        }
                    }
                    .toMutableList()
            }

            list.removeAll { it.reciterId == reciterId && it.surahNumber == surahNumber }
            list.add(0, RecentSurah(reciterId, surahNumber, System.currentTimeMillis()))
            val trimmedList = list.take(5)
            val serialized = trimmedList.joinToString(",") { "${it.reciterId}:${it.surahNumber}:${it.timestamp}" }
            preferences[RECENTLY_PLAYED] = serialized
        }
    }

    val playerThemeId: Flow<String> = appContext.dataStore.data
        .map { preferences ->
            preferences[PLAYER_THEME_ID] ?: PlayerThemeId.MIDNIGHT_DARK.id
        }

    suspend fun setPlayerThemeId(id: String) {
        appContext.dataStore.edit { preferences ->
            preferences[PLAYER_THEME_ID] = id
        }
    }

    val audioFavorites: Flow<List<AudioFavorite>> = appContext.dataStore.data
        .map { preferences ->
            val raw = preferences[AUDIO_FAVORITES] ?: ""
            if (raw.isBlank()) {
                emptyList()
            } else {
                try {
                    val array = JSONArray(raw)
                    val list = mutableListOf<AudioFavorite>()
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        val rId = obj.optString("rId", "")
                        val sNum = obj.optInt("sNum", -1)
                        if (rId.isNotBlank() && sNum > 0) {
                            list.add(
                                AudioFavorite(
                                    reciterId = rId,
                                    surahNumber = sNum,
                                    surahNameArabic = obj.optString("sAr", ""),
                                    surahNameEnglish = obj.optString("sEn", ""),
                                    timestamp = obj.optLong("ts", System.currentTimeMillis())
                                )
                            )
                        }
                    }
                    list.sortedByDescending { it.timestamp }
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }

    suspend fun toggleAudioFavorite(
        reciterId: String,
        surahNumber: Int,
        surahNameArabic: String = "",
        surahNameEnglish: String = ""
    ): Boolean {
        var added = false
        appContext.dataStore.edit { preferences ->
            val raw = preferences[AUDIO_FAVORITES] ?: ""
            val list = mutableListOf<AudioFavorite>()
            if (raw.isNotBlank()) {
                try {
                    val array = JSONArray(raw)
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        val rId = obj.optString("rId", "")
                        val sNum = obj.optInt("sNum", -1)
                        if (rId.isNotBlank() && sNum > 0) {
                            list.add(
                                AudioFavorite(
                                    reciterId = rId,
                                    surahNumber = sNum,
                                    surahNameArabic = obj.optString("sAr", ""),
                                    surahNameEnglish = obj.optString("sEn", ""),
                                    timestamp = obj.optLong("ts", System.currentTimeMillis())
                                )
                            )
                        }
                    }
                } catch (_: Exception) {}
            }

            val existingIndex = list.indexOfFirst { it.reciterId == reciterId && it.surahNumber == surahNumber }
            if (existingIndex >= 0) {
                list.removeAt(existingIndex)
                added = false
            } else {
                list.add(
                    0,
                    AudioFavorite(
                        reciterId = reciterId,
                        surahNumber = surahNumber,
                        surahNameArabic = surahNameArabic,
                        surahNameEnglish = surahNameEnglish,
                        timestamp = System.currentTimeMillis()
                    )
                )
                added = true
            }

            val jsonArray = JSONArray()
            list.forEach { fav ->
                val obj = JSONObject().apply {
                    put("rId", fav.reciterId)
                    put("sNum", fav.surahNumber)
                    put("sAr", fav.surahNameArabic)
                    put("sEn", fav.surahNameEnglish)
                    put("ts", fav.timestamp)
                }
                jsonArray.put(obj)
            }
            preferences[AUDIO_FAVORITES] = jsonArray.toString()
        }
        return added
    }

    suspend fun removeAudioFavorite(reciterId: String, surahNumber: Int) {
        appContext.dataStore.edit { preferences ->
            val raw = preferences[AUDIO_FAVORITES] ?: ""
            if (raw.isNotBlank()) {
                try {
                    val array = JSONArray(raw)
                    val jsonArray = JSONArray()
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        val rId = obj.optString("rId", "")
                        val sNum = obj.optInt("sNum", -1)
                        if (!(rId == reciterId && sNum == surahNumber)) {
                            jsonArray.put(obj)
                        }
                    }
                    preferences[AUDIO_FAVORITES] = jsonArray.toString()
                } catch (_: Exception) {}
            }
        }
    }
}
