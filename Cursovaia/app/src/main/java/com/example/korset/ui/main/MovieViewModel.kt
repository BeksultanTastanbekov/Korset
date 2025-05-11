package com.example.korset.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.korset.data.model.Movie // Убедись, что модель импортирована
import com.example.korset.data.paging.MoviePagingSource // Импорт твоего PagingSource
import com.example.korset.data.repository.MovieRepository // Импорт репозитория
import com.example.korset.data.network.ApiService // Для API_KEY
import kotlinx.coroutines.flow.Flow

// Этот ViewModel будет отвечать ТОЛЬКО за пагинированные списки
class MovieViewModel : ViewModel() {

    private val repository = MovieRepository()
    private val apiKey = ApiService.API_KEY // Берем ключ из ApiService

    init {
        Log.d("MovieViewModel", "Initialized")
        if (apiKey.isBlank()) {
            Log.e("MovieViewModel", "API Key is missing!")
        }
    }

    // Функция для получения пагинированного Flow с возможностью фильтрации
    fun getMoviesPager(
        category: String, // "popular", "upcoming", "year_filter", "search", "genre" и т.д.
        query: String? = null, // Для поиска
        year: String? = null, // Для фильтра по году
        genreId: String? = null // Для фильтра по жанру
    ): Flow<PagingData<Movie>> {
        Log.d("MovieViewModel", "Creating Pager for category: $category, year: $year, query: $query")
        if (category == "search" && query.isNullOrBlank()) {
            throw IllegalArgumentException("Пустой поисковый запрос")
        }
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = {
                // Передаем все параметры в PagingSource
                MoviePagingSource(
                    apiService = repository.getApiServiceInstance(), // Получаем сервис через репозиторий
                    apiKey = apiKey,
                    category = category,
                    query = query,
                    year = year,
                    genreId = genreId
                )
            }
        ).flow.cachedIn(viewModelScope) // Кэшируем во ViewModelScope
    }
    init {
        Log.d("MovieViewModel", "Initialized, API Key: $apiKey")
        if (apiKey.isBlank()) {
            Log.e("MovieViewModel", "API Key is missing!")
        }
    }
}