package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Room Data Access Object (DAO) for Tafseer Ibn Kaseer.
 * Provides methods to insert tafseer data and query tafseer content by surahId and ayahNumber.
 */
@Dao
interface TafseerDao {
    @Query("SELECT * FROM tafseer_ibn_kaseer WHERE surah_id = :surahId AND ayah_number = :ayahNumber LIMIT 1")
    fun getTafseerBySurahAndAyah(surahId: Int, ayahNumber: Int): Flow<TafseerIbnKaseer?>

    @Query("SELECT * FROM tafseer_ibn_kaseer WHERE surah_id = :surahId AND ayah_number = :ayahNumber LIMIT 1")
    suspend fun getTafseerDirect(surahId: Int, ayahNumber: Int): TafseerIbnKaseer?

    @Query("SELECT * FROM tafseer_ibn_kaseer WHERE surah_id = :surahId ORDER BY ayah_number ASC")
    fun getTafseerForSurah(surahId: Int): Flow<List<TafseerIbnKaseer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTafseer(tafseer: TafseerIbnKaseer)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tafseers: List<TafseerIbnKaseer>)

    @Query("SELECT COUNT(*) FROM tafseer_ibn_kaseer")
    suspend fun getCount(): Int
}
