package com.example.korset.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.korset.data.model.Movie
import com.example.korset.data.network.ApiService
import retrofit2.HttpException
import java.io.IOException

class MoviePagingSource(
    private val apiService: ApiService,
    private val apiKey: String,
    private val category: String,
    private val query: String? = null,
    private val year: String? = null,
    private val genreId: String? = null // Оставим на будущее
) : PagingSource<Int, Movie>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val page = params.key ?: 1 // Стартовая страница = 1

        return try {
            println("PagingSource: Loading page $page for category '$category', query '$query', year '$year'") // Лог

            val response = when (category) {
                "popular" -> apiService.getPopularMovies(apiKey, page = page)
                "year_filter" -> {
                    if (year != null) {
                        apiService.getMoviesByYear(apiKey = apiKey, page = page, year = year)
                    } else {
                        println("PagingSource: Error - Year is null for 'year_filter'")
                        throw IllegalArgumentException("Year must be provided for year_filter category")
                    }
                }
                // --- ДОБАВЛЕНА ВЕТКА ДЛЯ ПОИСКА ---
                "search" -> {
                    if (!query.isNullOrBlank()) {
                        apiService.searchMovies(apiKey = apiKey, query = query, page = page)
                    } else {
                        println("PagingSource: Error - Query is null or blank for 'search'")
                        // Возвращаем пустую страницу, а не ошибку, чтобы список просто очистился
                        // throw IllegalArgumentException("Query must be provided for search category")
                        return LoadResult.Page(data = emptyList(), prevKey = null, nextKey = null)
                    }
                }
                // -------------------------------
                else -> {
                    println("PagingSource: Warning - Unknown category '$category', loading popular.")
                    apiService.getPopularMovies(apiKey, page = page) // По умолчанию
                }
            }

            val movies = response.results ?: emptyList()
            println("PagingSource: Loaded ${movies.size} movies for page $page.") // Лог

            val nextKey = if (movies.isEmpty() || page >= response.totalPages) null else page + 1
            val prevKey = if (page == 1) null else page - 1

            LoadResult.Page(
                data = movies,
                prevKey = prevKey,
                nextKey = nextKey
            )

        } catch (exception: IOException) {
            println("PagingSource: IOException - ${exception.message}")
            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            println("PagingSource: HttpException - ${exception.code()} ${exception.message}")
            LoadResult.Error(exception)
        } catch (exception: IllegalArgumentException) { // Ловим ошибки проверок параметров
            println("PagingSource: IllegalArgumentException - ${exception.message}")
            LoadResult.Error(exception)
        } catch (exception: Exception) { // Ловим остальные ошибки
            println("PagingSource: General Exception - ${exception.message}")
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}