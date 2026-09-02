package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class TafseerBookmark(
    val id: String = UUID.randomUUID().toString(),
    val tafseerId: String,
    val surahNumber: Int,
    val surahName: String,
    val ayahNumberInSurah: Int = 0, // 0 indicates full Surah bookmark
    val urduTranslation: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

object TafseerBookmarkManager {

    private val flowMap = mutableMapOf<String, MutableStateFlow<List<TafseerBookmark>>>()

    private fun getPrefs(context: Context, tafseerId: String): SharedPreferences {
        return context.getSharedPreferences("tafseer_bookmarks_$tafseerId", Context.MODE_PRIVATE)
    }

    private fun getFlow(context: Context, tafseerId: String): MutableStateFlow<List<TafseerBookmark>> {
        synchronized(flowMap) {
            return flowMap.getOrPut(tafseerId) {
                val list = loadBookmarksFromPrefs(context, tafseerId)
                MutableStateFlow(list)
            }
        }
    }

    fun getBookmarksFlow(context: Context, tafseerId: String): StateFlow<List<TafseerBookmark>> {
        return getFlow(context, tafseerId).asStateFlow()
    }

    private fun loadBookmarksFromPrefs(context: Context, tafseerId: String): List<TafseerBookmark> {
        val prefs = getPrefs(context, tafseerId)
        val rawJson = prefs.getString("bookmarks_json", "[]") ?: "[]"
        val list = mutableListOf<TafseerBookmark>()
        try {
            val jsonArray = JSONArray(rawJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    TafseerBookmark(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        tafseerId = obj.optString("tafseerId", tafseerId),
                        surahNumber = obj.optInt("surahNumber", 1),
                        surahName = obj.optString("surahName", ""),
                        ayahNumberInSurah = obj.optInt("ayahNumberInSurah", 0),
                        urduTranslation = obj.optString("urduTranslation", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list.sortedByDescending { it.timestamp }
    }

    private fun saveBookmarksToPrefs(context: Context, tafseerId: String, bookmarks: List<TafseerBookmark>) {
        val prefs = getPrefs(context, tafseerId)
        val jsonArray = JSONArray()
        for (bm in bookmarks) {
            val obj = JSONObject().apply {
                put("id", bm.id)
                put("tafseerId", bm.tafseerId)
                put("surahNumber", bm.surahNumber)
                put("surahName", bm.surahName)
                put("ayahNumberInSurah", bm.ayahNumberInSurah)
                put("urduTranslation", bm.urduTranslation)
                put("timestamp", bm.timestamp)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("bookmarks_json", jsonArray.toString()).apply()
        getFlow(context, tafseerId).value = bookmarks
    }

    fun isSurahBookmarked(context: Context, tafseerId: String, surahNumber: Int): Boolean {
        val current = getFlow(context, tafseerId).value
        return current.any { it.surahNumber == surahNumber && it.ayahNumberInSurah == 0 }
    }

    fun isAyahBookmarked(context: Context, tafseerId: String, surahNumber: Int, ayahNumber: Int): Boolean {
        val current = getFlow(context, tafseerId).value
        return current.any { it.surahNumber == surahNumber && it.ayahNumberInSurah == ayahNumber }
    }

    fun toggleSurahBookmark(
        context: Context,
        tafseerId: String,
        surahNumber: Int,
        surahName: String,
        urduSubtitle: String
    ): Boolean {
        val current = getFlow(context, tafseerId).value.toMutableList()
        val existingIndex = current.indexOfFirst { it.surahNumber == surahNumber && it.ayahNumberInSurah == 0 }
        val isNowBookmarked: Boolean

        if (existingIndex >= 0) {
            current.removeAt(existingIndex)
            isNowBookmarked = false
        } else {
            val newBm = TafseerBookmark(
                id = UUID.randomUUID().toString(),
                tafseerId = tafseerId,
                surahNumber = surahNumber,
                surahName = surahName,
                ayahNumberInSurah = 0,
                urduTranslation = urduSubtitle,
                timestamp = System.currentTimeMillis()
            )
            current.add(0, newBm)
            isNowBookmarked = true
        }

        saveBookmarksToPrefs(context, tafseerId, current)
        return isNowBookmarked
    }

    fun toggleAyahBookmark(
        context: Context,
        tafseerId: String,
        surahNumber: Int,
        surahName: String,
        ayahNumber: Int,
        urduTranslation: String
    ): Boolean {
        val current = getFlow(context, tafseerId).value.toMutableList()
        val existingIndex = current.indexOfFirst { it.surahNumber == surahNumber && it.ayahNumberInSurah == ayahNumber }
        val isNowBookmarked: Boolean

        if (existingIndex >= 0) {
            current.removeAt(existingIndex)
            isNowBookmarked = false
        } else {
            val newBm = TafseerBookmark(
                id = UUID.randomUUID().toString(),
                tafseerId = tafseerId,
                surahNumber = surahNumber,
                surahName = surahName,
                ayahNumberInSurah = ayahNumber,
                urduTranslation = urduTranslation,
                timestamp = System.currentTimeMillis()
            )
            current.add(0, newBm)
            isNowBookmarked = true
        }

        saveBookmarksToPrefs(context, tafseerId, current)
        return isNowBookmarked
    }

    fun deleteBookmark(context: Context, tafseerId: String, bookmarkId: String) {
        val current = getFlow(context, tafseerId).value.toMutableList()
        current.removeAll { it.id == bookmarkId }
        saveBookmarksToPrefs(context, tafseerId, current)
    }
}
