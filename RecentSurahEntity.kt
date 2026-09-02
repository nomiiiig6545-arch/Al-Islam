package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_surahs")
data class RecentSurahEntity(
    @PrimaryKey val surahNumber: Int,
    val surahName: String,
    val timestamp: Long = System.currentTimeMillis()
)
