package com.example.data.db

import androidx.room.Entity

@Entity(tableName = "cached_ayahs", primaryKeys = ["surahNumber", "numberInSurah"])
data class AyahEntity(
    val surahNumber: Int,
    val numberInSurah: Int,
    val overallNumber: Int,
    val arabicText: String,
    val urduText: String,
    val audioUrl: String? = null,
    val urduAudioUrl: String? = null
)
