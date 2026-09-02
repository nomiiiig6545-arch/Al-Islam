package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM tafseer_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM tafseer_notes WHERE surahNumber = :surahNumber AND ayahNumber = :ayahNumber ORDER BY timestamp DESC")
    fun getNotesForAyah(surahNumber: Int, ayahNumber: Int): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Query("DELETE FROM tafseer_notes WHERE id = :noteId")
    suspend fun deleteNote(noteId: Int)
}
