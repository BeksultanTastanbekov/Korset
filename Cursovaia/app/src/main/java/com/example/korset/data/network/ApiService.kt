package com.example.korset.data.network

import com.example.korset.data.model.MovieListResponse
import com.example.korset.data.model.MovieDetails
import com.example.korset.data.model.Movie
// УДАЛЯЕМ: import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiService {

    companion object {
        const val BASE_URL = "https://api.themoviedb.org/3/"
        const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"
        const val POSTER_SIZE_W500 = "w500"
        const val API_KEY = "0ee2685af8f3bea1afeafc4b9dd977fd"

    }

    // --- ИЗМЕНЕНИЕ: Все методы теперь возвращают MovieListResponse напрямую ---

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ru-RU",
        @Query("page") page: Int // Страница обязательна для пагинации
    ): MovieListResponse // <-- НЕ Response<...>

    @GET("discover/movie")
    suspend fun getMoviesByDate(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ru-RU",
        @Query("page") page: Int,
        @Query("sort_by") sortBy: String = "primary_release_date.desc",
        @Query("primary_release_date.gte") gte: String,
        @Query("primary_release_date.lte") lte: String
    ): MovieListResponse // <-- НЕ Response<...>

    @GET("tv/popular")
    suspend fun getPopularTvShows(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ru-RU",
        @Query("page") page: Int
    ): MovieListResponse // <-- НЕ Response<...>. ВАЖНО: Модель все еще Movie!

    @GET("discover/movie")
    suspend fun getMoviesByGenre(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ru-RU",
        @Query("page") page: Int,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("with_genres") genreId: String
    ): MovieListResponse // <-- НЕ Response<...>

    // Удалите getUpcomingMovies, если не используется
    @GET("discover/movie")
    suspend fun getMoviesByYear(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ru-RU",
        @Query("page") page: Int,
        @Query("sort_by") sortBy: String = "popularity.desc", // Или другая сортировка
        @Query("primary_release_year") year: String // <-- Фильтр по году
    ): MovieListResponse

    // --- ДОБАВЛЯЕМ МЕТОД ДЛЯ ПОИСКА ---
    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ru-RU", // Правильный Query
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("include_adult") includeAdult: Boolean = false
    ): MovieListResponse
    // ----------------------------------

    // --- ДОБАВЛЯЕМ МЕТОД ДЛЯ ДЕТАЛЕЙ ФИЛЬМА ---
    @GET("movie/{movie_id}") // Путь к эндпоинту деталей
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int, // Передаем ID как часть пути
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ru-RU",
        @Query("append_to_response") appendToResponse: String? = "videos,credits" // Запрашиваем доп. данные (видео, актеры)
    ): MovieDetails

    @GET("movie/now_playing") // Эндпоинт для "Сейчас в кино"
    suspend fun getNowPlayingMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ru-RU",
        @Query("page") page: Int
    ): MovieListResponse

    @GET("movie/upcoming") // Эндпоинт для "Скоро в прокате"
    suspend fun getUpcomingMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ru-RU",
        @Query("page") page: Int
    ): MovieListResponse
}