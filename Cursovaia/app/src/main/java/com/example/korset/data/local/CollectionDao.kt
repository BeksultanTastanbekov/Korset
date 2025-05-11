package com.example.korset.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {

    // --- Операции с Коллекциями (CollectionEntity) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: CollectionEntity): Long // Возвращает ID вставленной коллекции

    @Update
    suspend fun updateCollection(collection: CollectionEntity)

    @Delete
    suspend fun deleteCollection(collection: CollectionEntity)

    @Query("SELECT * FROM collections ORDER BY createdAt DESC")
    fun getAllCollectionsFlow(): Flow<List<CollectionEntity>> // Получить все коллекции

    @Query("SELECT * FROM collections WHERE collectionId = :collectionId")
    suspend fun getCollectionById(collectionId: Int): CollectionEntity?


    // --- Операции со связью Фильм-Коллекция (CollectionMovieCrossRef) ---
    @Insert(onConflict = OnConflictStrategy.IGNORE) // Игнорировать, если связь уже есть
    suspend fun addMovieToCollection(crossRef: CollectionMovieCrossRef)

    @Delete
    suspend fun removeMovieFromCollection(crossRef: CollectionMovieCrossRef)

    // Проверить, есть ли фильм в конкретной коллекции
    @Query("SELECT EXISTS(SELECT 1 FROM collection_movie_cross_ref WHERE collectionId = :collectionId AND movieId = :movieId LIMIT 1)")
    suspend fun isMovieInCollection(collectionId: Int, movieId: Int): Boolean

    // --- НОВЫЙ МЕТОД: Удалить все связи для коллекции ---
    @Query("DELETE FROM collection_movie_cross_ref WHERE collectionId = :collectionId")
    suspend fun deleteCrossRefsForCollection(collectionId: Int)
    // ----------------------------------------------------

    // --- Транзакция для полного удаления коллекции ---
    @Transaction
    suspend fun deleteCollectionAndRefs(collection: CollectionEntity) {
        // Сначала удаляем все связи
        deleteCrossRefsForCollection(collection.collectionId)
        // Затем удаляем саму коллекцию
        deleteCollection(collection)
    }
    // -----------------------------------------------

    // --- Получение Коллекции со списком ее Фильмов ---
    @Transaction // Гарантирует атомарность операции
    @Query("SELECT * FROM collections WHERE collectionId = :collectionId")
    suspend fun getCollectionWithMovies(collectionId: Int): CollectionWithMovies?

    // Получить ВСЕ коллекции со всеми их фильмами (для отображения списка коллекций и их содержимого)
    @Transaction
    @Query("SELECT * FROM collections ORDER BY createdAt DESC")
    fun getAllCollectionsWithMoviesFlow(): Flow<List<CollectionWithMovies>>
}