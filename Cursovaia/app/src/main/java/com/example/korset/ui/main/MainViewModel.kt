package com.example.korset.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel // Добавь этот импорт, если Android Studio просит
import androidx.lifecycle.viewModelScope // Добавь этот импорт
import com.example.korset.data.local.CitiesDataSource
import com.example.korset.data.model.City
import com.example.korset.data.model.Movie // Добавь импорт Movie
import com.example.korset.data.repository.MovieRepository // Добавь импорт Repository
import kotlinx.coroutines.launch // Добавь импорт launch
import java.lang.Exception // Добавь импорт Exception

class MainViewModel(application: Application) : AndroidViewModel(application) {

    // --- КОД ДЛЯ ГОРОДА (ОСТАЕТСЯ БЕЗ ИЗМЕНЕНИЙ) ---
    private val citiesDataSource = CitiesDataSource()
    private val sharedPreferences = application.getSharedPreferences("korset_prefs", Application.MODE_PRIVATE)
    private companion object { const val KEY_SELECTED_CITY_ID = "selected_city_id" }
    private val _selectedCity = MutableLiveData<City>()
    val selectedCity: LiveData<City> = _selectedCity

    init { // Загружаем город при старте
        loadInitialCity()
    }

    private fun loadInitialCity() {
        val savedCityId = sharedPreferences.getInt(KEY_SELECTED_CITY_ID, citiesDataSource.getDefaultCity().id)
        val cityToLoad = citiesDataSource.getCities().find { it.id == savedCityId } ?: citiesDataSource.getDefaultCity()
        _selectedCity.value = cityToLoad
        println("MainViewModel: Initial city loaded: ${cityToLoad.name}") // Лог
    }

    fun setSelectedCity(city: City) {
        _selectedCity.value = city
        sharedPreferences.edit().putInt(KEY_SELECTED_CITY_ID, city.id).apply()
        println("MainViewModel: City selected and saved: ${city.name}") // Лог
    }
    // --- КОНЕЦ КОДА ДЛЯ ГОРОДА ---


    // --- КОД ДЛЯ КАРУСЕЛИ (ДОБАВЛЯЕМ) ---

    // Создаем репозиторий для фильмов
    private val movieRepository = MovieRepository()

    // LiveData для хранения списка фильмов карусели
    private val _carouselMovies = MutableLiveData<List<Movie>>()
    val carouselMovies: LiveData<List<Movie>> = _carouselMovies

    // LiveData для состояния загрузки карусели
    private val _isLoadingCarousel = MutableLiveData<Boolean>()
    val isLoadingCarousel: LiveData<Boolean> = _isLoadingCarousel

    // Функция для запроса фильмов для карусели
    fun fetchCarouselMovies() {
        // Проверяем, не идет ли уже загрузка
        if (_isLoadingCarousel.value == true) return

        _isLoadingCarousel.value = true
        println("MainViewModel: Fetching carousel movies...") // Лог
        viewModelScope.launch {
            try {
                // Вызываем ПУБЛИЧНУЮ функцию репозитория
                val movies = movieRepository.getTrendingMoviesForCarousel(1)
                // Берем не больше 5 фильмов
                _carouselMovies.value = movies.take(5)
                println("MainViewModel: Fetched ${movies.take(5).size} movies for carousel") // Лог
            } catch (e: Exception) {
                _carouselMovies.value = emptyList() // При ошибке - пустой список
                println("MainViewModel: Error fetching carousel movies: ${e.message}") // Лог
                // TODO: Показать сообщение об ошибке пользователю
            } finally {
                _isLoadingCarousel.value = false // Завершаем загрузку
            }
        }
    }
    // --- КОНЕЦ КОДА ДЛЯ КАРУСЕЛИ ---

    // --- ДОБАВЛЯЕМ КОД ДЛЯ ВЕРТИКАЛЬНОГО СПИСКА ---

    // LiveData для хранения отфильтрованного списка
    private val _movieList = MutableLiveData<List<Movie>>()
    val movieList: LiveData<List<Movie>> = _movieList

    // LiveData для состояния загрузки списка (опционально)
    private val _isLoadingList = MutableLiveData<Boolean>()
    val isLoadingList: LiveData<Boolean> = _isLoadingList

    // Функция для загрузки и ФИЛЬТРАЦИИ фильмов для списка
    fun fetchMoviesForList(targetYear: String = "2025") { // Год можно передать параметром
        if (_isLoadingList.value == true) return // Не загружать, если уже грузится
        _isLoadingList.value = true
        println("MainViewModel: Fetching movies for list (target year: $targetYear)...")
        viewModelScope.launch {
            try {
                // Загружаем, например, популярные фильмы (1 страницу)
                // ИСПОЛЬЗУЕМ ТУ ЖЕ ФУНКЦИЮ, ЧТО ДЛЯ КАРУСЕЛИ, но обработаем иначе
                val allMovies = movieRepository.getTrendingMoviesForCarousel(1) // Получаем популярных

                // --- Фильтруем по году ---
                val filteredMovies = allMovies.filter { movie ->
                    // Используем дату релиза фильма ИЛИ дату первого эфира сериала
                    val releaseDate = movie.releaseDate ?: movie.firstAirDate
                    // Проверяем, что дата начинается с нужного года
                    releaseDate?.startsWith(targetYear) == true
                }
                // -----------------------

                _movieList.value = filteredMovies // Обновляем LiveData ОТФИЛЬТРОВАННЫМ списком
                println("MainViewModel: Fetched list. Found ${filteredMovies.size} movies for year $targetYear")

            } catch (e: Exception) {
                _movieList.value = emptyList() // Пустой список при ошибке
                println("MainViewModel: Error fetching movie list: ${e.message}")
                // TODO: Обработка ошибки
            } finally {
                _isLoadingList.value = false // Завершаем загрузку
            }
        }
    }
    // --- КОНЕЦ КОДА ДЛЯ ВЕРТИКАЛЬНОГО СПИСКА ---

}