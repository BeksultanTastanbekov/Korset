package com.example.korset.data.local

import androidx.room.* // Импортируем все из Room для удобства
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteMovieDao {

    // Вставить фильм в избранное. Если уже есть, заменяем (на случай обновления данных)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToFavorites(movie: FavoriteMovieEntity)

    // Удалить фильм из избранного по его ID
    @Query("DELETE FROM favorite_movies WHERE movieId = :movieId")
    suspend fun removeFromFavorites(movieId: Int)

    // Получить все фильмы из избранного, отсортированные по времени добавления (сначала новые)
    @Query("SELECT * FROM favorite_movies ORDER BY timestamp DESC")
    fun getAllFavoriteMoviesFlow(): Flow<List<FavoriteMovieEntity>>

    // Проверить, есть ли фильм в избранном
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_movies WHERE movieId = :movieId LIMIT 1)")
    suspend fun isFavorite(movieId: Int): Boolean

    // Получить один фильм из избранного по ID (может понадобиться)
    @Query("SELECT * FROM favorite_movies WHERE movieId = :movieId")
    suspend fun getFavoriteMovieById(movieId: Int): FavoriteMovieEntity?
}