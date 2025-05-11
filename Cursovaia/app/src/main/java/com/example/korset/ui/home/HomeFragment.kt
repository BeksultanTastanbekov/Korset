package com.example.korset.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.example.korset.R
import com.example.korset.databinding.FragmentHomeBinding
import com.example.korset.ui.adapters.CarouselAdapter
import com.example.korset.ui.adapters.MovieAdapter
import com.example.korset.ui.dialogs.CitySelectionBottomSheetFragment
import com.example.korset.ui.main.MainViewModel
import com.example.korset.ui.main.MovieViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs
import androidx.navigation.fragment.findNavController
import com.example.korset.ui.home.HomeFragmentDirections

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var movieViewModel: MovieViewModel

    private lateinit var carouselAdapter: CarouselAdapter
    private lateinit var movieAdapter: MovieAdapter

    private val autoScrollHandler = Handler(Looper.getMainLooper())
    private var autoScrollRunnable: Runnable? = null
    private val SCROLL_DELAY = 5000L

    private var movieListJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        movieViewModel = ViewModelProvider(this).get(MovieViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        setupCitySelectionClick()
        observeSelectedCity()
        setupCarousel()
        observeCarouselMovies()
        observeCarouselLoadingState()
        setupRecyclerViewMovies()
        setupPagingLoadStateListener()
        setupHeaderActions()
        setupSearch()

        if (savedInstanceState == null) {
            mainViewModel.fetchCarouselMovies()
            startObservingMovieList(category = "year_filter", year = "2025")
        }
    }

    // --- НОВАЯ ФУНКЦИЯ ДЛЯ НАСТРОЙКИ ДЕЙСТВИЙ В ШАПКЕ ---
    private fun setupHeaderActions() {
        // Клик по иконке "Мои билеты"
        binding.mainHeader.ivMyTicketsHeader.setOnClickListener {
            try {
                // Используем глобальный Action
                findNavController().navigate(R.id.action_global_to_myTicketsFragment)
            } catch (e: Exception) {
                Log.e("HomeFragmentNav", "Ошибка навигации на Мои Билеты", e)
                Toast.makeText(context, "Не удалось открыть Мои билеты", Toast.LENGTH_SHORT).show()
            }
        }

        // Клик по кнопке выбора города (у тебя уже есть)
        binding.mainHeader.citySelectorButtonHeader.setOnClickListener {
            CitySelectionBottomSheetFragment.newInstance().show(childFragmentManager, CitySelectionBottomSheetFragment.TAG)
        }
    }
// --------------------------------------------------

//    private fun setupCitySelectionClick() {
//        binding.mainHeader.citySelectorButtonHeader.setOnClickListener {
//            CitySelectionBottomSheetFragment.newInstance().show(childFragmentManager, CitySelectionBottomSheetFragment.TAG)
//        }
//    }

    private fun observeSelectedCity() {
        mainViewModel.selectedCity.observe(viewLifecycleOwner) { city ->
            binding.mainHeader.citySelectorButtonHeader.text = city?.name ?: "Город?"
        }
    }

    private fun setupCarousel() {
        carouselAdapter = CarouselAdapter { movie ->
            Toast.makeText(requireContext(), "Карусель: ${movie.title ?: movie.name}", Toast.LENGTH_SHORT).show()
        }
        binding.moviesCarousel.adapter = carouselAdapter
        binding.moviesCarousel.offscreenPageLimit = 3
        binding.moviesCarousel.clipToPadding = false
        binding.moviesCarousel.clipChildren = false
        binding.moviesCarousel.getChildAt(0)?.overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        val transformer = CompositePageTransformer()
        transformer.addTransformer(MarginPageTransformer(resources.getDimensionPixelOffset(R.dimen.viewpager_page_margin)))
        transformer.addTransformer { page, position ->
            val r = 1 - abs(position)
            page.scaleY = 0.85f + r * 0.15f
            page.scaleX = 0.85f + r * 0.15f
        }
        binding.moviesCarousel.setPageTransformer(transformer)
    }

    private fun observeCarouselMovies() {
        mainViewModel.carouselMovies.observe(viewLifecycleOwner) { movies ->
            val hasMovies = !movies.isNullOrEmpty()
            binding.moviesCarousel.isVisible = hasMovies
            if (hasMovies) {
                carouselAdapter.submitList(movies)
                startAutoScroll(movies.size)
            } else {
                carouselAdapter.submitList(emptyList())
                stopAutoScroll()
            }
        }
    }

    private fun observeCarouselLoadingState() {
        mainViewModel.isLoadingCarousel.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarCarousel.isVisible = isLoading
            if (isLoading) {
                stopAutoScroll()
                binding.moviesCarousel.isVisible = false
            } else {
                binding.moviesCarousel.isVisible = !mainViewModel.carouselMovies.value.isNullOrEmpty()
            }
        }
    }

    private fun startAutoScroll(itemCount: Int) {
        if (itemCount <= 1 || autoScrollRunnable != null) return
        autoScrollRunnable = object : Runnable {
            override fun run() {
                if (_binding != null && binding.moviesCarousel.isVisible) {
                    val currentItem = binding.moviesCarousel.currentItem
                    val nextItem = (currentItem + 1) % itemCount
                    binding.moviesCarousel.setCurrentItem(nextItem, true)
                    autoScrollHandler.postDelayed(this, SCROLL_DELAY)
                } else stopAutoScroll()
            }
        }
        autoScrollHandler.postDelayed(autoScrollRunnable!!, SCROLL_DELAY)
    }

    private fun stopAutoScroll() {
        autoScrollRunnable?.let { autoScrollHandler.removeCallbacks(it) }
        autoScrollRunnable = null
    }

    private fun setupRecyclerViewMovies() {
        movieAdapter = MovieAdapter { movie ->
            Toast.makeText(requireContext(), "Переход к ${movie.title ?: movie.name}", Toast.LENGTH_SHORT).show()

            // --- ПЕРЕХОД С ПОМОЩЬЮ NAVIGATION COMPONENT (Safe Args) ---
            try {
                // Создаем Action с аргументом movieId, используя сгенерированный класс
                // Убедись, что имя action... совпадает с ID Action в nav_graph.xml
                val action = HomeFragmentDirections.actionNavHomeToMovieDetailFragment(movieId = movie.id)
                // Находим NavController для текущего фрагмента и выполняем переход
                findNavController().navigate(action) // Теперь должно работать после импорта
            } catch (e: Exception) {
                // Обработка возможной ошибки (например, если findNavController не найден)
                Log.e("HomeFragmentNav", "Ошибка навигации", e)
                // Используй requireContext() для Toast во фрагменте
                Toast.makeText(requireContext(), "Не удалось открыть детали фильма", Toast.LENGTH_SHORT).show()
            }
            // ---------------------------------------------------------
        }
        // Устанавливаем адаптер и LayoutManager (этот код у тебя уже есть)
        binding.moviesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.moviesRecyclerView.adapter = movieAdapter
        println("HomeFragment: Paging RecyclerView setup done.")
    }


    private fun startObservingMovieList(category: String, query: String? = null, year: String? = null) {
        movieListJob?.cancel()
        movieListJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                movieViewModel.getMoviesPager(category = category, query = query, year = year)
                    .collectLatest { movieAdapter.submitData(it) }
            } catch (e: Exception) {
                handlePagingError(e)
            }
        }
    }

    private fun setupPagingLoadStateListener() {
        movieAdapter.addLoadStateListener { loadState ->
            val refreshState = loadState.refresh
            val appendState = loadState.append // Состояние загрузки следующей страницы

            // --- Управление ProgressBar для списка ---
            // Показываем ProgressBar только при ПЕРВИЧНОЙ загрузке
            // binding.progressBarList.isVisible = refreshState is LoadState.Loading

            // --- Управление текстом ошибки ---
            val errorState = refreshState as? LoadState.Error ?: loadState.source.append as? LoadState.Error
            // binding.errorTextList.isVisible = errorState != null
            // binding.errorTextList.text = errorState?.error?.localizedMessage ?: "Ошибка загрузки"

            // --- Управление видимостью списка и сообщения "Ничего не найдено" ---
            // Список пуст, если первичная загрузка завершилась без ошибок, ИЛИ если произошла ошибка при пустом списке И пагинация закончилась
            val listIsEmpty = (refreshState is LoadState.NotLoading && movieAdapter.itemCount == 0 && appendState.endOfPaginationReached)
                    || (refreshState is LoadState.Error && movieAdapter.itemCount == 0)


            // Показываем RecyclerView, если НЕ идет первичная загрузка И список НЕ пуст
            binding.moviesRecyclerView.isVisible = refreshState !is LoadState.Loading && !listIsEmpty
            // Показываем сообщение "Ничего не найдено", если список пуст и НЕ идет загрузка
            binding.tvNoResults.isVisible = listIsEmpty && refreshState !is LoadState.Loading

            // Показываем/скрываем заголовок "Кино" только если НЕ идет поиск
            val currentQuery = binding.searchBar.searchViewReusable.query?.toString()
            val isSearching = !currentQuery.isNullOrBlank()
            if (!isSearching) {
                binding.labelMovies.isVisible = !listIsEmpty && refreshState !is LoadState.Loading
            } else {
                binding.labelMovies.isVisible = false // Скрываем заголовок при поиске
            }

            println("HomeFragment: Paging LoadState changed. Is list empty: $listIsEmpty. Refresh state: $refreshState. Is searching: $isSearching")
        }
    }


    private fun setupSearch() {
        val searchView = binding.searchBar.searchViewReusable
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val trimmedQuery = query?.trim()
                if (!trimmedQuery.isNullOrBlank()) {
                    startObservingMovieList("search", trimmedQuery)
                } else {
                    startObservingMovieList("year_filter", year = "2025")
                }
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    startObservingMovieList("year_filter", year = "2025")
                }
                return true
            }
        })

        searchView.setOnCloseListener {
            startObservingMovieList("year_filter", year = "2025")
            searchView.clearFocus()
            false
        }
    }

    private fun showMainContent(show: Boolean) {
        binding.moviesCarousel.isVisible = show
        binding.labelMovies.isVisible = show
    }

    private fun handlePagingError(e: Exception) {
        movieListJob?.cancel()
        Log.e("PagingError", "Ошибка Paging", e) // ← Печатаем stack trace


        when (e) {
            is IllegalArgumentException -> {
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            }
            is IllegalStateException -> {
                Toast.makeText(requireContext(), "Ошибка: API ключ не найден", Toast.LENGTH_LONG).show()
            }
            else -> {
                Toast.makeText(requireContext(), "Неизвестная ошибка загрузки", Toast.LENGTH_SHORT).show()
            }
        }

        showMainContent(false)
        binding.moviesRecyclerView.isVisible = false
    }


    override fun onPause() {
        super.onPause()
        stopAutoScroll()
    }

    override fun onResume() {
        super.onResume()
        if (mainViewModel.isLoadingCarousel.value == false) {
            mainViewModel.carouselMovies.value?.let { if (it.isNotEmpty()) startAutoScroll(it.size) }
        }
    }

    override fun onDestroyView() {
        movieListJob?.cancel()
        stopAutoScroll()

        // Очищаем адаптеры
        binding.moviesCarousel.adapter = null
        if (::movieAdapter.isInitialized) {
            binding.moviesRecyclerView.adapter = null
        }

        _binding = null

        super.onDestroyView() // ВАЖНО: вызываем в конце
    }

}
