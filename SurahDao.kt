package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SurahDao {
    @Query("SELECT * FROM cached_surahs ORDER BY number ASC")
    fun getAllSurahs(): Flow<List<SurahEntity>>

    @Query("SELECT * FROM cached_surahs ORDER BY number ASC")
    suspend fun getAllSurahsSync(): List<SurahEntity>

    @Query("SELECT * FROM cached_surahs WHERE number = :surahNumber")
    suspend fun getSurahByNumber(surahNumber: Int): SurahEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSurahs(surahs: List<SurahEntity>)

    @Query("UPDATE cached_surahs SET isDownloaded = :isDownloaded WHERE number = :surahNumber")
    suspend fun updateDownloadStatus(surahNumber: Int, isDownloaded: Boolean)
}
