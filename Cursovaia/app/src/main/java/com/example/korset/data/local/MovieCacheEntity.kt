package com.example.korset.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Таблица для кэширования информации о фильмах (для коллекций и, возможно, истории просмотров)
@Entity(tableName = "movie_cache")
data class MovieCacheEntity(
    @PrimaryKey val movieId: Int, // ID из TMDB
    val title: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val overview: String?,
    val releaseDate: String?,
    val voteAverage: Double?,
    // Добавь другие поля, если нужно будет отображать в списках коллекций
    val addedTimestamp: Long = System.currentTimeMillis() // Время добавления в кэш
)