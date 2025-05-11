package com.example.korset.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MovieCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieCacheEntity)

    @Query("SELECT * FROM movie_cache WHERE movieId = :movieId")
    suspend fun getMovieById(movieId: Int): MovieCacheEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM movie_cache WHERE movieId = :movieId LIMIT 1)")
    suspend fun movieExists(movieId: Int): Boolean

    // Метод удаления не обязателен, если используем REPLACE, но может пригодиться
    // @Query("DELETE FROM movie_cache WHERE movieId = :movieId")
    // suspend fun deleteMovieById(movieId: Int)
}