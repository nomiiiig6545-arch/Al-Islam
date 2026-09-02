package com.example.data.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

object TafseerFetcher {
    private val client = OkHttpClient()

    // Map tafseerId to the FawazAhmed edition name
    private fun getEditionName(tafseerId: String): String? {
        return when (tafseerId) {
            "usmani" -> "urd-muhammadtaqiusm"
            "ibn_kaseer" -> "urd-muhammadjunagar"
            "jalalayn" -> "ara-jalaladdinalmah"
            else -> null
        }
    }

    /**
     * Fetches the entire chapter of a specific Tafseer edition.
     * Returns a map of Ayah number -> Tafseer text.
     */
    suspend fun fetchChapterTafseer(surahNum: Int, tafseerId: String): Map<Int, String> = withContext(Dispatchers.IO) {
        val edition = getEditionName(tafseerId) ?: return@withContext emptyMap<Int, String>()
        val url = "https://cdn.jsdelivr.net/gh/fawazahmed0/quran-api@1/editions/$edition/$surahNum.json"
        
        try {
            Log.d("TafseerFetcher", "Fetching Tafseer from URL: $url")
            val request = Request.Builder()
                .url(url)
                .build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("TafseerFetcher", "Failed to fetch Tafseer: ${response.code} ${response.message}")
                    return@withContext emptyMap()
                }
                
                val bodyString = response.body?.string() ?: return@withContext emptyMap()
                val jsonObject = JSONObject(bodyString)
                val chapterArray = jsonObject.optJSONArray("chapter") ?: return@withContext emptyMap()
                
                val result = mutableMapOf<Int, String>()
                for (i in 0 until chapterArray.length()) {
                    val verseObj = chapterArray.getJSONObject(i)
                    val verseNum = verseObj.optInt("verse", i + 1)
                    val text = verseObj.optString("text", "")
                    if (text.isNotBlank()) {
                        result[verseNum] = text
                    }
                }
                Log.d("TafseerFetcher", "Successfully fetched ${result.size} verses of Tafseer for Surah $surahNum")
                return@withContext result
            }
        } catch (e: Exception) {
            Log.e("TafseerFetcher", "Error fetching chapter Tafseer", e)
            return@withContext emptyMap()
        }
    }
}
