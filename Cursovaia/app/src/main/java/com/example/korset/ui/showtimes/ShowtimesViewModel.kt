// ShowtimesViewModel.kt
package com.example.korset.ui.showtimes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.korset.data.model.Movie
import com.example.korset.data.repository.MovieRepository
import kotlinx.coroutines.launch

enum class ShowtimesCategory {
    NOW_PLAYING, UPCOMING, FOR_KIDS // FOR_KIDS пока может дублировать UPCOMING или NOW_PLAYING
}

class ShowtimesViewModel : ViewModel() {

    private val movieRepository = MovieRepository() // Предполагается, что конструктор по умолчанию

    private val _movies = MutableLiveData<List<Movie>>()
    val movies: LiveData<List<Movie>> = _movies

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var currentCategory = ShowtimesCategory.NOW_PLAYING
    private var currentPage = 1 // Для пагинации, если будете добавлять
    private var currentQuery: String? = null // Для поиска

    init {
        loadMovies(currentCategory)
    }

    fun loadMovies(category: ShowtimesCategory, query: String? = null, page: Int = 1) {
        currentCategory = category
        currentPage = page
        currentQuery = query
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val result = when (category) {
                    ShowtimesCategory.NOW_PLAYING -> movieRepository.getNowPlayingMovies(page) // Предполагаем, что есть такой метод
                    ShowtimesCategory.UPCOMING -> movieRepository.getUpcomingMovies(page)     // Предполагаем, что есть такой метод
                    ShowtimesCategory.FOR_KIDS -> movieRepository.getUpcomingMovies(page) // ЗАГЛУШКА: пока используем upcoming
                    // TODO: Для FOR_KIDS нужна более сложная логика (фильтрация по жанрам/рейтингам или отдельный endpoint)
                }
                // Если есть поиск, фильтруем или делаем другой запрос
                if (!query.isNullOrBlank()) {
                    //val searchResult = movieRepository.searchMovies(query, page)
                    //_movies.value = searchResult?.results ?: emptyList()
                    // Пока оставим фильтрацию по уже загруженным, если нет отдельного search для категорий
                    _movies.value = result?.results?.filter { movie -> // <<<--- Явно указываем имя переменной movie
                        movie.title?.contains(query, ignoreCase = true) == true
                    } ?: emptyList()
                } else {
                    _movies.value = result?.results ?: emptyList()
                }

            } catch (e: Exception) {
                _error.value = "Ошибка загрузки: ${e.message}"
                _movies.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchMovies(query: String) {
        loadMovies(currentCategory, query, 1) // Ищем по текущей категории с первой страницы
    }

    fun clearSearch() {
        loadMovies(currentCategory, null, 1) // Загружаем категорию без поискового запроса
    }
}