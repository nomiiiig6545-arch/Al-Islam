package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Room Data Access Object (DAO) for local Mushaf page management.
 * Provides reactive flows and suspend queries for offline storage tracking.
 */
@Dao
interface MushafPageDao {

    @Query("SELECT * FROM mushaf_pages ORDER BY pageNumber ASC")
    fun getAllPagesFlow(): Flow<List<MushafPageEntity>>

    @Query("SELECT * FROM mushaf_pages ORDER BY pageNumber ASC")
    suspend fun getAllPages(): List<MushafPageEntity>

    @Query("SELECT * FROM mushaf_pages WHERE pageNumber = :pageNumber LIMIT 1")
    suspend fun getPageByNumber(pageNumber: Int): MushafPageEntity?

    @Query("SELECT * FROM mushaf_pages WHERE isDownloaded = 1 ORDER BY pageNumber ASC")
    fun getDownloadedPagesFlow(): Flow<List<MushafPageEntity>>

    @Query("SELECT COUNT(*) FROM mushaf_pages WHERE isDownloaded = 1")
    fun getDownloadedPagesCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM mushaf_pages WHERE isDownloaded = 1")
    suspend fun getDownloadedPagesCount(): Int

    @Query("SELECT * FROM mushaf_pages WHERE juzNumber = :juzNumber ORDER BY pageNumber ASC")
    suspend fun getPagesForJuz(juzNumber: Int): List<MushafPageEntity>

    @Query("SELECT COUNT(*) FROM mushaf_pages WHERE juzNumber = :juzNumber AND isDownloaded = 1")
    suspend fun getDownloadedCountForJuz(juzNumber: Int): Int

    @Query("SELECT COUNT(*) FROM mushaf_pages WHERE juzNumber = :juzNumber")
    suspend fun getTotalCountForJuz(juzNumber: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPages(pages: List<MushafPageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPage(page: MushafPageEntity)

    @Query("UPDATE mushaf_pages SET isDownloaded = :isDownloaded, localFilePath = :filePath, fileSize = :fileSize WHERE pageNumber = :pageNumber")
    suspend fun updateDownloadStatus(pageNumber: Int, isDownloaded: Boolean, filePath: String?, fileSize: Long)

    @Query("UPDATE mushaf_pages SET lastAccessedTimestamp = :timestamp WHERE pageNumber = :pageNumber")
    suspend fun updateLastAccessed(pageNumber: Int, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM mushaf_pages")
    suspend fun getTotalPagesCount(): Int

    @Query("SELECT SUM(fileSize) FROM mushaf_pages WHERE isDownloaded = 1")
    fun getTotalOfflineStorageBytesFlow(): Flow<Long?>
}
