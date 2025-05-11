package com.example.korset.data.repository

import com.example.korset.data.model.Movie
import com.example.korset.data.network.ApiService
import com.example.korset.data.network.RetrofitClient
import com.example.korset.data.model.MovieDetails
import java.lang.Exception
import com.example.korset.data.model.MovieListResponse // <<<=== ДОБАВЬТЕ ЭТОТ ИМПОРТ

class MovieRepository {

    // apiService ОСТАЕТСЯ private!
    private val apiService: ApiService = RetrofitClient.instance
    private val apiKey = ApiService.API_KEY

    // --- Функция для получения списка фильмов для карусели ---
    // Эта функция ПУБЛИЧНАЯ (нет private)
    suspend fun getTrendingMoviesForCarousel(page: Int = 1): List<Movie> {
        if (apiKey.isBlank()) {
            println("MovieRepository: API Key is missing!")
            return emptyList()
        }
        return try {
            val response = apiService.getPopularMovies(apiKey = apiKey, page = page)
            // Обрабатываем случай, если API не вернет 'results'
            val movies = response.results ?: emptyList()
            println("MovieRepository: API Response for Carousel: ${movies.size} movies")
            movies
        } catch (e: Exception) {
            println("MovieRepository: Error fetching carousel movies: ${e.message}")
            emptyList()
        }
    }
    // -----------------------------------------

    // --- ДОБАВЛЯЕМ ФУНКЦИЮ ДЛЯ ПОЛУЧЕНИЯ ДЕТАЛЕЙ ---
    suspend fun getMovieDetails(movieId: Int): MovieDetails? { // Может вернуть null при ошибке
        if (apiKey.isBlank()) {
            println("MovieRepository: API Key is missing!")
            return null
        }
        return try {
            println("MovieRepository: Fetching details for ID $movieId...")
            // Вызываем новый метод ApiService
            apiService.getMovieDetails(movieId = movieId, apiKey = apiKey)
        } catch (e: Exception) {
            println("MovieRepository: Error fetching details for ID $movieId: ${e.message}")
            null // Возвращаем null при ошибке
        }
    }
    // -------------------------------------------

    // --- НОВЫЙ МЕТОД: Сейчас в кино ---
    suspend fun getNowPlayingMovies(page: Int = 1): MovieListResponse? {
        if (apiKey.isBlank()) {
            println("MovieRepository: API Key is missing!")
            return null
        }
        return try {
            println("MovieRepository: Fetching now playing movies, page $page...")
            apiService.getNowPlayingMovies(apiKey = apiKey, page = page)
        } catch (e: Exception) {
            println("MovieRepository: Error fetching now playing movies: ${e.message}")
            null
        }
    }
    // ----------------------------------

    // --- НОВЫЙ МЕТОД: Скоро в прокате ---
    suspend fun getUpcomingMovies(page: Int = 1): MovieListResponse? {
        if (apiKey.isBlank()) {
            println("MovieRepository: API Key is missing!")
            return null
        }
        return try {
            println("MovieRepository: Fetching upcoming movies, page $page...")
            apiService.getUpcomingMovies(apiKey = apiKey, page = page)
        } catch (e: Exception) {
            println("MovieRepository: Error fetching upcoming movies: ${e.message}")
            null
        }
    }
    // ------------------------------------

    // --- МЕТОД ДЛЯ ПОИСКА (если его нет, добавьте; если есть, проверьте) ---
    suspend fun searchMovies(query: String, page: Int = 1): MovieListResponse? {
        if (apiKey.isBlank()) {
            println("MovieRepository: API Key is missing!")
            return null
        }
        return try {
            println("MovieRepository: Searching movies with query '$query', page $page...")
            apiService.searchMovies(apiKey = apiKey, query = query, page = page)
        } catch (e: Exception) {
            println("MovieRepository: Error searching movies: ${e.message}")
            null
        }
    }
    // ------------------------------------------------------------------

    // Эта функция нужна будет ПОЗЖЕ для PagingSource в MovieViewModel
    // Пока она просто возвращает сервис.
    fun getApiServiceInstance(): ApiService {
        return apiService
    }
}