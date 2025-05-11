package com.example.korset.data.local

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class CollectionWithMovies(
    @Embedded val collection: CollectionEntity,
    @Relation(
        parentColumn = "collectionId",
        entity = MovieCacheEntity::class, // <<<=== СВЯЗЬ ТЕПЕРЬ С MovieCacheEntity
        entityColumn = "movieId",
        associateBy = Junction(CollectionMovieCrossRef::class)
    )
    val movies: List<MovieCacheEntity> // <<<=== Список теперь MovieCacheEntity
)