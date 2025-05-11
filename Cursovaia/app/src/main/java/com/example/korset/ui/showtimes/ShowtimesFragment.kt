package com.example.korset.ui.showtimes

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.korset.R // Убедитесь, что это ваш R-файл
import com.example.korset.data.model.Movie
import com.example.korset.databinding.FragmentShowtimesBinding
import com.example.korset.ui.adapters.MovieAdapter // Предполагаем, что этот адаптер существует и подходит
import com.example.korset.ui.dialogs.CitySelectionBottomSheetFragment
import com.example.korset.ui.main.MainViewModel
// Если ShowtimesFragmentDirections сгенерировался, импорт будет примерно таким:
// import com.example.korset.ui.showtimes.ShowtimesFragmentDirections
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import kotlinx.coroutines.launch


class ShowtimesFragment : Fragment() {

    private var _binding: FragmentShowtimesBinding? = null
    private val binding get() = _binding!!

    private val showtimesViewModel: ShowtimesViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    private lateinit var movieAdapter: MovieAdapter

    // Для UI элементов
    private var tvCurrentCityInHeader: TextView? = null
    private var citySelectorButtonInHeader: View? = null // Используем View для общности
    private var ivMyTicketsInHeader: ImageView? = null
    private var searchViewComponent: SearchView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShowtimesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUIReferences()
        setupHeaderListeners()
        setupSearch()
        setupRecyclerView()
        setupFilterButtons()
        observeViewModel()
    }

    private fun setupUIReferences() {
        // Шапка
        val headerRootView = binding.mainHeaderShowtimes.root
        citySelectorButtonInHeader = headerRootView.findViewById(R.id.citySelectorButton_header)
        // Текст города находится внутри MaterialButton, так что tvCurrentCityInHeader - это та же кнопка
        tvCurrentCityInHeader = citySelectorButtonInHeader as? TextView
        ivMyTicketsInHeader = headerRootView.findViewById(R.id.iv_my_tickets_header)

        // Поиск
        val searchBarRootView = binding.searchBarShowtimes.root
        searchViewComponent = searchBarRootView.findViewById(R.id.search_view_reusable) // Используем правильный ID
    }

    private fun setupHeaderListeners() {
        tvCurrentCityInHeader?.text = mainViewModel.selectedCity.value?.name ?: getString(R.string.city_placeholder) // Замените на вашу строку

        citySelectorButtonInHeader?.setOnClickListener {
            CitySelectionBottomSheetFragment.newInstance().show(childFragmentManager, CitySelectionBottomSheetFragment.TAG)
        }

        ivMyTicketsInHeader?.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_global_to_myTicketsFragment)
            } catch (e: Exception) {
                Toast.makeText(context, getString(R.string.error_opening_my_tickets), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSearch(){
        searchViewComponent?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (it.isNotBlank()) {
                        showtimesViewModel.searchMovies(it)
                    }
                }
                searchViewComponent?.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    // Даем пользователю закончить ввод перед очисткой
                }
                return true
            }
        })

        val closeButton: ImageView? = searchViewComponent?.findViewById(androidx.appcompat.R.id.search_close_btn)
        closeButton?.setOnClickListener {
            searchViewComponent?.setQuery("", false)
            searchViewComponent?.clearFocus()
            showtimesViewModel.clearSearch()
        }
    }


    private fun setupRecyclerView() {
        movieAdapter = MovieAdapter { movie ->
            // Навигация
            try {
                // После Rebuild Project, если action есть в nav_graph, этот класс должен быть доступен
                val action = ShowtimesFragmentDirections.actionNavShowtimesToMovieDetailFragment(movie.id)
                findNavController().navigate(action)
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка навигации: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("ShowtimesFragment", "Navigation error", e)
            }
        }
        binding.rvShowtimesMovies.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = movieAdapter
        }
    }

    private fun setupFilterButtons() {
        binding.buttonNowPlaying.setOnClickListener {
            showtimesViewModel.loadMovies(ShowtimesCategory.NOW_PLAYING)
            // updateFilterButtonsSelection(it) // Пока закомментируем, чтобы не отвлекаться
        }
        binding.buttonUpcoming.setOnClickListener {
            showtimesViewModel.loadMovies(ShowtimesCategory.UPCOMING)
            // updateFilterButtonsSelection(it)
        }
        binding.buttonForKids.setOnClickListener {
            showtimesViewModel.loadMovies(ShowtimesCategory.FOR_KIDS)
            // updateFilterButtonsSelection(it)
        }
        // updateFilterButtonsSelection(binding.buttonNowPlaying) // Пока закомментируем
    }

    private fun observeViewModel() {
        showtimesViewModel.movies.observe(viewLifecycleOwner) { moviesList ->
            // Обновляем PagingDataAdapter новым списком
            // Важно делать это в корутине, привязанной к жизненному циклу фрагмента
            if (moviesList != null) {
                viewLifecycleOwner.lifecycleScope.launch { // Используем viewLifecycleOwner.lifecycleScope
                    movieAdapter.submitData(PagingData.from(moviesList))
                }
            }

            val isLoading = showtimesViewModel.isLoading.value ?: false
            binding.tvNoMoviesShowtimes.isVisible = moviesList.isNullOrEmpty() && !isLoading
            binding.rvShowtimesMovies.isVisible = !moviesList.isNullOrEmpty()
        }

        showtimesViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarShowtimes.isVisible = isLoading
            if (isLoading) {
                binding.rvShowtimesMovies.isVisible = false
                // tvNoMoviesShowtimes не трогаем здесь, чтобы не перекрывать сообщение об ошибке или "нет результатов"
            } else {
                // Если загрузка завершена и список все еще пуст (и нет ошибки), показать "нет фильмов"
                if (showtimesViewModel.movies.value.isNullOrEmpty() && showtimesViewModel.error.value == null) {
                    binding.tvNoMoviesShowtimes.text = "Фильмы не найдены" // Или другая строка
                    binding.tvNoMoviesShowtimes.isVisible = true
                }
            }
        }

        showtimesViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                binding.tvNoMoviesShowtimes.text = it
                binding.tvNoMoviesShowtimes.isVisible = true
                binding.rvShowtimesMovies.isVisible = false
            }
        }

        mainViewModel.selectedCity.observe(viewLifecycleOwner) { city ->
            (tvCurrentCityInHeader as? com.google.android.material.button.MaterialButton)?.text = city.name
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvShowtimesMovies.adapter = null // Важно для избежания утечек
        _binding = null
    }
}