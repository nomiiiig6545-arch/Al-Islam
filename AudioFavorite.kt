package com.example.data.model

data class AudioFavorite(
    val reciterId: String,
    val surahNumber: Int,
    val surahNameArabic: String = "",
    val surahNameEnglish: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
