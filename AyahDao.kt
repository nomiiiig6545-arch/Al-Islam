package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AyahDao {
    @Query("SELECT * FROM cached_ayahs WHERE surahNumber = :surahNumber ORDER BY numberInSurah ASC")
    fun getAyahsForSurah(surahNumber: Int): Flow<List<AyahEntity>>

    @Query("SELECT * FROM cached_ayahs WHERE surahNumber = :surahNumber ORDER BY numberInSurah ASC")
    suspend fun getAyahsForSurahSync(surahNumber: Int): List<AyahEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAyahs(ayahs: List<AyahEntity>)

    @Query("DELETE FROM cached_ayahs WHERE surahNumber = :surahNumber")
    suspend fun deleteAyahsForSurah(surahNumber: Int)
}
