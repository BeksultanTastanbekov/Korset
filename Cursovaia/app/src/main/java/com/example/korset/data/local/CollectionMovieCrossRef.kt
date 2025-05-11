package com.example.korset.data.local

import androidx.room.Entity

// Связующая таблица для отношения многие-ко-многим
// между коллекциями и фильмами (используем movieId из FavoriteMovieEntity)
@Entity(tableName = "collection_movie_cross_ref", primaryKeys = ["collectionId", "movieId"])
data class CollectionMovieCrossRef(
    val collectionId: Int,
    val movieId: Int // ID фильма, такой же как в FavoriteMovieEntity
)