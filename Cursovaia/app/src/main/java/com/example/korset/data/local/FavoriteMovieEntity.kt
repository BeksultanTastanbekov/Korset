package com.example.korset.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Таблица для хранения фильмов, добавленных в "Избранное"
@Entity(tableName = "favorite_movies")
data class FavoriteMovieEntity(
    @PrimaryKey val movieId: Int, // ID фильма из TMDB будет первичным ключом
    val title: String?,
    val posterPath: String?,
    val backdropPath: String?, // На всякий случай, если захочешь отображать по-другому
    val overview: String?,
    val releaseDate: String?,
    val voteAverage: Double?,
    val timestamp: Long = System.currentTimeMillis() // Время добавления в избранное
)