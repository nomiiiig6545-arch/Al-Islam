package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val surahNumber: Int = 1,
    val surahName: String = "",
    val ayahNumberInSurah: Int = 0,
    val arabicText: String = "",
    val urduTranslation: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val pageNumber: Int = 0 // > 0 indicates a Mushaf 16-line Page Bookmark
)

