package com.example.korset.ui.details

import android.app.Application
import androidx.lifecycle.*
import com.example.korset.KorsetApp
import com.example.korset.data.local.CollectionDao
import com.example.korset.data.local.CollectionEntity
import com.example.korset.data.local.CollectionMovieCrossRef
import com.example.korset.data.local.CollectionWithMovies // Убедись, что импорт есть
import com.example.korset.data.local.FavoriteMovieDao
import com.example.korset.data.local.FavoriteMovieEntity
import com.example.korset.data.model.MovieDetails
import com.example.korset.data.repository.MovieRepository
import com.example.korset.util.Event
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.lang.Exception
import com.example.korset.data.local.MovieCacheDao // <<<=== ИМПОРТ
import com.example.korset.data.local.MovieCacheEntity // <<<=== ИМПОРТ

class MovieDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val movieRepository = MovieRepository()
    private val favoriteMovieDao: FavoriteMovieDao
    private val collectionDao: CollectionDao // Объявляем

    // LiveData для деталей
    private val _movieDetails = MutableLiveData<MovieDetails?>()
    val movieDetails: LiveData<MovieDetails?> = _movieDetails

    // LiveData для коллекций со списком фильмов (для CollectionsFragment)
    val userCollectionsWithMovies: LiveData<List<CollectionWithMovies>>

    // LiveData для статуса операций с коллекциями
    private val _collectionOperationStatus = MutableLiveData<Event<String>>()
    val collectionOperationStatus: LiveData<Event<String>> = _collectionOperationStatus

    // LiveData для загрузки
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData для ошибок
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // LiveData для статуса избранного
    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: LiveData<Boolean> = _isFavorite

    // LiveData для списка избранных (для FavoriteMoviesFragment)
    val favoriteMovies: LiveData<List<FavoriteMovieEntity>>

    // --- НОВОЕ: LiveData для ДЕТАЛЕЙ ОДНОЙ КОЛЛЕКЦИИ ---
    private val _selectedCollectionDetails = MutableLiveData<CollectionWithMovies?>()
    val selectedCollectionDetails: LiveData<CollectionWithMovies?> = _selectedCollectionDetails
    // --------------------------------------------------

    // --- ДОБАВЛЯЕМ MovieCacheDao ---
    private val movieCacheDao: MovieCacheDao
    // ------------------------------


    // --- ОДИН ЕДИНСТВЕННЫЙ init БЛОК ---
    init {
        println("MovieDetailViewModel: Initializing...")
        val database = (application as KorsetApp).database
        // Инициализируем ОБА DAO
        favoriteMovieDao = database.favoriteMovieDao()
        collectionDao = database.collectionDao()
        movieCacheDao = database.movieCacheDao() // <<<=== ИНИЦИАЛИЗИРУЕМ
        println("MovieDetailViewModel: DAOs Initialized.")

        // Инициализируем ОБА Flow -> LiveData
        userCollectionsWithMovies = collectionDao.getAllCollectionsWithMoviesFlow().asLiveData()
        favoriteMovies = favoriteMovieDao.getAllFavoriteMoviesFlow().asLiveData()
        println("MovieDetailViewModel: LiveData for collections and favorites initialized.")
    }
    // --- КОНЕЦ init БЛОКА ---

    // --- Функции для Коллекций ---
    fun createNewCollection(name: String, description: String? = null) {
        if (name.isBlank()) {
            _collectionOperationStatus.value = Event("Название коллекции не может быть пустым")
            return
        }
        viewModelScope.launch {
            try {
                val newCollection = CollectionEntity(name = name, description = description)
                val newCollectionId = collectionDao.insertCollection(newCollection)
                _collectionOperationStatus.value = Event("Коллекция '$name' создана") // Убрал ID для простоты сообщения
                println("MovieDetailViewModel: New collection '$name' created.")
            } catch (e: Exception) {
                _collectionOperationStatus.value = Event("Ошибка создания коллекции: ${e.message}")
                println("MovieDetailViewModel: Error creating collection: ${e.message}")
            }
        }
    }

    // --- ИЗМЕНЯЕМ addCurrentMovieToCollection ---
    fun addCurrentMovieToCollection(collectionId: Int) {
        val currentDetails = _movieDetails.value ?: return // Проверка на null
        val movieId = currentDetails.id

        viewModelScope.launch {
            try {
                // 1. Проверяем, есть ли фильм в КЭШЕ (movie_cache)
                if (!movieCacheDao.movieExists(movieId)) {
                    // Если нет - создаем и вставляем MovieCacheEntity
                    val movieToCache = MovieCacheEntity(
                        movieId = movieId, title = currentDetails.title,
                        posterPath = currentDetails.posterPath, backdropPath = currentDetails.backdropPath,
                        overview = currentDetails.overview, releaseDate = currentDetails.releaseDate,
                        voteAverage = currentDetails.voteAverage
                    )
                    movieCacheDao.insertMovie(movieToCache) // <<<=== Вставляем в кэш
                    println("MovieDetailViewModel: Movie ID $movieId cached.")
                }

                // 2. Добавляем связь фильм-коллекция (без изменений)
                collectionDao.addMovieToCollection(CollectionMovieCrossRef(collectionId, movieId))
                val collection = collectionDao.getCollectionById(collectionId)
                _collectionOperationStatus.value = Event("Фильм добавлен в коллекцию '${collection?.name}'")
                println("MovieDetailViewModel: Movie ID $movieId added to collection ID $collectionId.")

            } catch (e: Exception) {
                _collectionOperationStatus.value = Event("Ошибка добавления в коллекцию: ${e.message}")
                println("MovieDetailViewModel: Error adding movie to collection: ${e.message}")
            }
        }
    }
    // --- КОНЕЦ ИЗМЕНЕНИЯ ---

    // --- Функции для Избранного и Деталей ---
    // --- Функции для Избранного и Деталей ---
    fun fetchMovieDetails(movieId: Int) {
        if (movieId == -1) {
            _error.value = "Неверный ID фильма"
            return
        }
        _isLoading.value = true
        _error.value = null
        println("MovieDetailViewModel: Fetching details for ID $movieId")
        viewModelScope.launch {
            try {
                val details = movieRepository.getMovieDetails(movieId)
                if (details != null) {
                    _movieDetails.value = details
                    checkIfFavorite(movieId) // Проверяем избранное после загрузки деталей
                } else {
                    _error.value = "Не удалось загрузить детали для ID $movieId"
                    _movieDetails.value = null
                }
                println("MovieDetailViewModel: Loaded details for ID $movieId. Success: ${details != null}")
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки деталей: ${e.message}"
                _movieDetails.value = null
                println("MovieDetailViewModel: Error loading details for ID $movieId: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun checkIfFavorite(movieId: Int) {
        viewModelScope.launch {
            _isFavorite.value = favoriteMovieDao.isFavorite(movieId)
            println("MovieDetailViewModel: Movie ID $movieId isFavorite: ${_isFavorite.value}")
        }
    }

    fun toggleFavoriteStatus() {
        val currentDetails = _movieDetails.value ?: return
        val movieId = currentDetails.id
        viewModelScope.launch {
            val isCurrentlyFavorite = _isFavorite.value ?: favoriteMovieDao.isFavorite(movieId) // Перепроверяем на всякий случай
            if (isCurrentlyFavorite) {
                favoriteMovieDao.removeFromFavorites(movieId)
                _isFavorite.value = false
                println("MovieDetailViewModel: Removed from favorites ID $movieId")
            } else {
                val favoriteMovie = FavoriteMovieEntity(
                    movieId = movieId, title = currentDetails.title, posterPath = currentDetails.posterPath,
                    backdropPath = currentDetails.backdropPath, overview = currentDetails.overview,
                    releaseDate = currentDetails.releaseDate, voteAverage = currentDetails.voteAverage
                )
                favoriteMovieDao.addToFavorites(favoriteMovie)
                _isFavorite.value = true
                println("MovieDetailViewModel: Added to favorites ID $movieId")
            }
        }
    }
    // --- Конец функций для Избранного и Деталей ---

    // --- НОВАЯ ФУНКЦИЯ: Загрузка деталей коллекции ---
    fun loadCollectionDetails(collectionId: Int) {
        viewModelScope.launch {
            println("MovieDetailViewModel: Loading details for collection ID $collectionId")
            try {
                // Используем метод DAO, который мы уже создали
                val details = collectionDao.getCollectionWithMovies(collectionId)
                _selectedCollectionDetails.value = details
                println("MovieDetailViewModel: Collection details loaded. Movie count: ${details?.movies?.size}")
            } catch (e: Exception) {
                println("MovieDetailViewModel: Error loading collection details: ${e.message}")
                _selectedCollectionDetails.value = null
                // TODO: Обработать ошибку (передать в LiveData _error?)
            }
        }
    }
    // ----------------------------------------------

    // При очистке ViewModel обнуляем выбранную коллекцию
    override fun onCleared() {
        super.onCleared()
        _selectedCollectionDetails.value = null
    }

    fun clearSelectedCollectionDetails() {
        _selectedCollectionDetails.value = null
    }

    // --- НОВАЯ ФУНКЦИЯ: Удаление коллекции ---
    fun deleteCollection(collection: CollectionEntity) {
        viewModelScope.launch {
            try {
                collectionDao.deleteCollectionAndRefs(collection)
                _collectionOperationStatus.value = Event("Коллекция '${collection.name}' удалена")
                println("MovieDetailViewModel: Collection deleted: ${collection.name}")
                // LiveData userCollectionsWithMovies обновится автоматически
            } catch (e: Exception) {
                _collectionOperationStatus.value = Event("Ошибка удаления коллекции: ${e.message}")
                println("MovieDetailViewModel: Error deleting collection: ${e.message}")
            }
        }
    }
    // ----------------------------------------


}