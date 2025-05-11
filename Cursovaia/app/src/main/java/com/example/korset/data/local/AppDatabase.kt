package com.example.korset.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// --- ИЗМЕНЕНИЕ: Добавляем новые Entity и УВЕЛИЧИВАЕМ ВЕРСИЮ ---
@Database(
    entities = [
        TicketEntity::class,
        FavoriteMovieEntity::class,
        CollectionEntity::class,
        CollectionMovieCrossRef::class,
        MovieCacheEntity::class // <<<=== ДОБАВЛЕНО
    ],
    version = 4, // <<<=== ВЕРСИЯ УВЕЛИЧЕНА
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun ticketDao(): TicketDao
    abstract fun favoriteMovieDao(): FavoriteMovieDao
    abstract fun collectionDao(): CollectionDao
    // --- ДОБАВЛЯЕМ МЕТОД ДЛЯ MovieCacheDao ---
    abstract fun movieCacheDao(): MovieCacheDao // <<<=== ДОБАВЛЕНО
    // ---------------------------------------

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "korset_database"
                )
                    .fallbackToDestructiveMigration() // Все еще используем это для простоты
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}