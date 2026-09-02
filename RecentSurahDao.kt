package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentSurahDao {
    @Query("SELECT * FROM recent_surahs ORDER BY timestamp DESC LIMIT 20")
    fun getRecentSurahs(): Flow<List<RecentSurahEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentSurah(recentSurah: RecentSurahEntity)
}
