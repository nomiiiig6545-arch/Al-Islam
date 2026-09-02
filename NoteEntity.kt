package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tafseer_notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val surahNumber: Int,
    val surahName: String,
    val ayahNumber: Int,
    val noteText: String,
    val timestamp: Long = System.currentTimeMillis()
)
