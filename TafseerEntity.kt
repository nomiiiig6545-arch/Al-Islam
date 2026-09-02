package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room database entity representing Tafseer Ibn Kaseer commentary for an individual Ayah.
 */
@Entity(
    tableName = "tafseer_ibn_kaseer",
    indices = [Index(value = ["surah_id", "ayah_number"], unique = true)]
)
data class TafseerIbnKaseer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "surah_id")
    val surahId: Int,
    @ColumnInfo(name = "ayah_number")
    val ayahNumber: Int,
    @ColumnInfo(name = "arabic_text")
    val arabicText: String,
    @ColumnInfo(name = "urdu_translation")
    val urduTranslation: String,
    @ColumnInfo(name = "tafseer_content")
    val tafseerContent: String
)

typealias TafseerEntity = TafseerIbnKaseer
