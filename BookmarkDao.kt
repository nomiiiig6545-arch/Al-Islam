package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE pageNumber > 0 ORDER BY timestamp DESC")
    fun getPageBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE surahNumber = :surahNumber AND ayahNumberInSurah = :ayahNumberInSurah AND pageNumber = 0 LIMIT 1")
    suspend fun getBookmark(surahNumber: Int, ayahNumberInSurah: Int): BookmarkEntity?

    @Query("SELECT * FROM bookmarks WHERE pageNumber = :pageNumber LIMIT 1")
    suspend fun getPageBookmark(pageNumber: Int): BookmarkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: Int)

    @Query("DELETE FROM bookmarks WHERE surahNumber = :surahNumber AND ayahNumberInSurah = :ayahNumberInSurah AND pageNumber = 0")
    suspend fun deleteBookmarkByAyah(surahNumber: Int, ayahNumberInSurah: Int)

    @Query("DELETE FROM bookmarks WHERE pageNumber = :pageNumber")
    suspend fun deleteBookmarkByPage(pageNumber: Int)
}
