package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.TafseerDao
import com.example.data.TafseerIbnKaseer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BookmarkEntity::class,
        RecentSurahEntity::class,
        SurahEntity::class,
        AyahEntity::class,
        TafseerIbnKaseer::class,
        NoteEntity::class,
        MushafPageEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun recentSurahDao(): RecentSurahDao
    abstract fun surahDao(): SurahDao
    abstract fun ayahDao(): AyahDao
    abstract fun tafseerDao(): TafseerDao
    abstract fun noteDao(): NoteDao
    abstract fun mushafPageDao(): MushafPageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quran_database"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { database ->
                                DatabaseSeeder.seedDatabase(database, context.applicationContext)
                            }
                        }
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { database ->
                                DatabaseSeeder.seedIfEmpty(database, context.applicationContext)
                            }
                        }
                    }
                })
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
