package com.example.data.repository

import com.example.data.api.Ayah
import com.example.data.api.AyahEdition
import com.example.data.api.Edition
import com.example.data.api.QuranApi
import com.example.data.api.Surah
import com.example.data.api.SurahEdition
import com.example.data.db.AyahDao
import com.example.data.db.AyahEntity
import com.example.data.db.BookmarkDao
import com.example.data.db.BookmarkEntity
import com.example.data.db.RecentSurahDao
import com.example.data.db.RecentSurahEntity
import com.example.data.db.MushafPageDao
import com.example.data.db.MushafPageEntity
import com.example.data.db.SurahDao
import com.example.data.db.SurahEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class QuranRepository(
    private val bookmarkDao: BookmarkDao,
    private val recentSurahDao: RecentSurahDao,
    private val surahDao: SurahDao,
    private val ayahDao: AyahDao,
    private val mushafPageDao: MushafPageDao? = null
) {

    val cachedSurahsFlow: Flow<List<SurahEntity>> = surahDao.getAllSurahs()
    val allMushafPagesFlow: Flow<List<MushafPageEntity>>? = mushafPageDao?.getAllPagesFlow()
    val downloadedMushafPagesCountFlow: Flow<Int>? = mushafPageDao?.getDownloadedPagesCountFlow()
    val totalOfflineStorageBytesFlow: Flow<Long?>? = mushafPageDao?.getTotalOfflineStorageBytesFlow()

    suspend fun getMushafPage(pageNumber: Int): MushafPageEntity? = withContext(Dispatchers.IO) {
        mushafPageDao?.getPageByNumber(pageNumber)
    }

    suspend fun getDownloadedMushafPagesCount(): Int = withContext(Dispatchers.IO) {
        mushafPageDao?.getDownloadedPagesCount() ?: 0
    }

    suspend fun updateMushafPageDownload(pageNumber: Int, isDownloaded: Boolean, filePath: String?, fileSize: Long) = withContext(Dispatchers.IO) {
        mushafPageDao?.updateDownloadStatus(pageNumber, isDownloaded, filePath, fileSize)
    }

    suspend fun getSurahs(): List<Surah> = withContext(Dispatchers.IO) {
        val cached = try { surahDao.getAllSurahsSync() } catch (e: Exception) { emptyList() }
        if (cached.isNotEmpty()) {
            return@withContext cached.map { entity ->
                Surah(
                    number = entity.number,
                    name = entity.name,
                    englishName = entity.englishName,
                    englishNameTranslation = entity.englishNameTranslation,
                    numberOfAyahs = entity.numberOfAyahs,
                    revelationType = entity.revelationType
                )
            }
        }

        val offlineList = com.example.data.mushaf.OfflineQuranDataProvider.getOfflineSurahsList()
        try {
            surahDao.insertSurahs(com.example.data.mushaf.OfflineQuranDataProvider.getOfflineSurahEntities())
        } catch (e: Exception) {
            // Log or ignore insert error
        }
        offlineList
    }

    suspend fun getSurahDetail(surahNumber: Int): List<SurahEdition> = withContext(Dispatchers.IO) {
        try {
            val editions = QuranApi.service.getSurahWithTranslation(surahNumber).data
            if (editions.isNotEmpty()) {
                cacheSurahEditions(surahNumber, editions)
            }
            editions
        } catch (e: Exception) {
            val cachedAyahs = ayahDao.getAyahsForSurahSync(surahNumber)
            if (cachedAyahs.isNotEmpty() && cachedAyahs.none { it.arabicText.startsWith("آيَةُ") || it.urduText.contains("کا مستند اردو ترجمہ") }) {
                buildSurahEditionsFromCache(surahNumber, cachedAyahs)
            } else {
                val offlineAyahs = com.example.data.mushaf.OfflineQuranDataProvider.getOfflineAyahsForSurah(surahNumber)
                ayahDao.insertAyahs(offlineAyahs)
                buildSurahEditionsFromCache(surahNumber, offlineAyahs)
            }
        }
    }

    private suspend fun cacheSurahEditions(surahNumber: Int, editions: List<SurahEdition>) {
        val arabicEdition = editions.find { it.edition?.identifier == "quran-uthmani" }
        val urduEdition = editions.find { it.edition?.identifier == "ur.jalandhry" }
        val audioEdition = editions.find { it.edition?.identifier == "ar.alafasy" }
        val urduAudioEdition = editions.find { it.edition?.identifier == "ur.khan" }

        if (arabicEdition != null && urduEdition != null) {
            val ayahsToCache = arabicEdition.ayahs.mapIndexed { index, arabicAyah ->
                val rawUrdu = urduEdition.ayahs.getOrNull(index)?.text ?: ""
                val cleanArabic = com.example.util.QuranSanitizer.cleanAyahArabic(
                    arabicAyah.text,
                    surahNumber,
                    arabicAyah.numberInSurah
                )
                val cleanUrdu = com.example.util.QuranSanitizer.cleanAyahUrdu(
                    rawUrdu,
                    surahNumber,
                    arabicAyah.numberInSurah
                )
                val audioUrl = audioEdition?.ayahs?.getOrNull(index)?.audio
                val urduAudioUrl = urduAudioEdition?.ayahs?.getOrNull(index)?.audio
                AyahEntity(
                    surahNumber = surahNumber,
                    numberInSurah = arabicAyah.numberInSurah,
                    overallNumber = arabicAyah.number,
                    arabicText = cleanArabic,
                    urduText = cleanUrdu,
                    audioUrl = audioUrl,
                    urduAudioUrl = urduAudioUrl
                )
            }
            ayahDao.insertAyahs(ayahsToCache)
        }
    }

    private suspend fun buildSurahEditionsFromCache(surahNumber: Int, cachedAyahs: List<AyahEntity>): List<SurahEdition> {
        val cachedSurah = surahDao.getSurahByNumber(surahNumber)
        val defaultEnglishName = if (surahNumber in 1..114) "Surah $surahNumber" else "Surah"
        val surahInfo = Surah(
            number = surahNumber,
            name = cachedSurah?.name?.ifEmpty { null } ?: defaultEnglishName,
            englishName = cachedSurah?.englishName?.ifEmpty { null } ?: defaultEnglishName,
            englishNameTranslation = cachedSurah?.englishNameTranslation ?: "",
            numberOfAyahs = cachedSurah?.numberOfAyahs ?: cachedAyahs.size,
            revelationType = cachedSurah?.revelationType ?: "Meccan"
        )

        val arabicAyahs = cachedAyahs.map {
            Ayah(
                number = it.overallNumber,
                text = it.arabicText,
                numberInSurah = it.numberInSurah,
                audio = it.audioUrl
            )
        }

        val urduAyahs = cachedAyahs.map {
            Ayah(
                number = it.overallNumber,
                text = it.urduText,
                numberInSurah = it.numberInSurah,
                audio = it.audioUrl
            )
        }

        val audioAyahs = cachedAyahs.map {
            Ayah(
                number = it.overallNumber,
                text = it.arabicText,
                numberInSurah = it.numberInSurah,
                audio = it.audioUrl
            )
        }

        val urduAudioAyahs = cachedAyahs.map {
            Ayah(
                number = it.overallNumber,
                text = it.urduText,
                numberInSurah = it.numberInSurah,
                audio = it.urduAudioUrl
            )
        }

        return listOf(
            SurahEdition(
                number = surahNumber,
                name = surahInfo.name,
                englishName = surahInfo.englishName,
                englishNameTranslation = surahInfo.englishNameTranslation,
                revelationType = surahInfo.revelationType,
                numberOfAyahs = surahInfo.numberOfAyahs,
                ayahs = arabicAyahs,
                edition = Edition(identifier = "quran-uthmani", name = "Uthmani")
            ),
            SurahEdition(
                number = surahNumber,
                name = surahInfo.name,
                englishName = surahInfo.englishName,
                englishNameTranslation = surahInfo.englishNameTranslation,
                revelationType = surahInfo.revelationType,
                numberOfAyahs = surahInfo.numberOfAyahs,
                ayahs = urduAyahs,
                edition = Edition(identifier = "ur.jalandhry", name = "Fateh Muhammad Jalandhari")
            ),
            SurahEdition(
                number = surahNumber,
                name = surahInfo.name,
                englishName = surahInfo.englishName,
                englishNameTranslation = surahInfo.englishNameTranslation,
                revelationType = surahInfo.revelationType,
                numberOfAyahs = surahInfo.numberOfAyahs,
                ayahs = audioAyahs,
                edition = Edition(identifier = "ar.alafasy", name = "Alafasy Audio")
            ),
            SurahEdition(
                number = surahNumber,
                name = surahInfo.name,
                englishName = surahInfo.englishName,
                englishNameTranslation = surahInfo.englishNameTranslation,
                revelationType = surahInfo.revelationType,
                numberOfAyahs = surahInfo.numberOfAyahs,
                ayahs = urduAudioAyahs,
                edition = Edition(identifier = "ur.khan", name = "Urdu Audio Translation")
            )
        )
    }

    suspend fun isSurahCached(surahNumber: Int): Boolean {
        return ayahDao.getAyahsForSurahSync(surahNumber).isNotEmpty()
    }

    suspend fun getPageAyahs(pageNumber: Int): List<AyahEntity> = withContext(Dispatchers.IO) {
        try {
            // First check if all ayahs for the page are already cached
            val pageInfo = com.example.data.mushaf.IndoPakMushafData.getPageInfo(pageNumber)
            
            // To be accurate, we can just fetch from API to get the correct ayahs for the page
            val response = com.example.data.api.QuranApi.service.getPage(pageNumber)
            val ayahs = response.data?.ayahs ?: emptyList()
            
            // Convert to AyahEntity format for consistency in MushafPageLineManager
            ayahs.map { apiAyah ->
                // The API provides the surah object inside the ayah, or we can infer from our mapping
                val surahNumber = apiAyah.surah?.number ?: pageInfo.surahNumber
                AyahEntity(
                    surahNumber = surahNumber,
                    numberInSurah = apiAyah.numberInSurah,
                    overallNumber = apiAyah.number,
                    arabicText = apiAyah.text,
                    urduText = ""
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun getAyahsForPage(pageNumber: Int): List<AyahEntity> = withContext(Dispatchers.IO) {
        val pageInfo = com.example.data.mushaf.IndoPakMushafData.getPageInfo(pageNumber)
        var cached = ayahDao.getAyahsForSurahSync(pageInfo.surahNumber)
        if (cached.isEmpty()) {
            try {
                getSurahDetail(pageInfo.surahNumber)
                cached = ayahDao.getAyahsForSurahSync(pageInfo.surahNumber)
            } catch (e: Exception) {
                // Return empty list if offline and not cached yet
            }
        }
        cached
    }

    suspend fun downloadSurahForOffline(surahNumber: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val editions = QuranApi.service.getSurahWithTranslation(surahNumber).data
            if (editions.isNotEmpty()) {
                cacheSurahEditions(surahNumber, editions)
                surahDao.updateDownloadStatus(surahNumber, true)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getDailyAyah(): List<AyahEdition> = withContext(Dispatchers.IO) {
        try {
            val currentDay = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
            val dailyAyahNumber = (currentDay % 6236).toInt() + 1
            QuranApi.service.getAyahWithTranslation(dailyAyahNumber).data
        } catch (e: Exception) {
            emptyList()
        }
    }

    val allBookmarks: Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()

    suspend fun addBookmark(bookmark: BookmarkEntity) {
        bookmarkDao.insertBookmark(bookmark)
    }

    suspend fun removeBookmark(surahNumber: Int, ayahNumberInSurah: Int) {
        bookmarkDao.deleteBookmarkByAyah(surahNumber, ayahNumberInSurah)
    }

    suspend fun removeBookmarkById(id: Int) {
        bookmarkDao.deleteBookmarkById(id)
    }

    suspend fun removePageBookmark(pageNumber: Int) {
        bookmarkDao.deleteBookmarkByPage(pageNumber)
    }

    suspend fun getBookmark(surahNumber: Int, ayahNumberInSurah: Int): BookmarkEntity? {
        return bookmarkDao.getBookmark(surahNumber, ayahNumberInSurah)
    }

    suspend fun isPageBookmarked(pageNumber: Int): Boolean {
        return bookmarkDao.getPageBookmark(pageNumber) != null
    }

    val recentSurahs: Flow<List<RecentSurahEntity>> = recentSurahDao.getRecentSurahs()

    suspend fun addRecentSurah(surahNumber: Int, surahName: String) {
        recentSurahDao.insertRecentSurah(RecentSurahEntity(surahNumber, surahName))
    }
}
