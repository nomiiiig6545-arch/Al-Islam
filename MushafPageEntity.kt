package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database Entity for local tracking and offline storage of the 549 Mushaf pages.
 * Ensures instant airplane-mode loading without network calls.
 */
@Entity(tableName = "mushaf_pages")
data class MushafPageEntity(
    @PrimaryKey val pageNumber: Int,
    val surahNumber: Int,
    val surahNameArabic: String,
    val surahNameEnglish: String,
    val juzNumber: Int,
    val juzNameArabic: String,
    val localFilePath: String? = null,
    val isDownloaded: Boolean = false,
    val fileSize: Long = 0L,
    val lastAccessedTimestamp: Long = System.currentTimeMillis()
)
